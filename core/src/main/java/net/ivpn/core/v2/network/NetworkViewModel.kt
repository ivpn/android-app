package net.ivpn.core.v2.network

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.CompoundButton
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.NetworkProtectionPreference
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.vpn.local.NetworkController
import net.ivpn.core.vpn.model.NetworkSource
import net.ivpn.core.vpn.model.NetworkState
import net.ivpn.core.vpn.model.WifiItem
import org.slf4j.LoggerFactory
import javax.inject.Inject
import kotlin.collections.ArrayList

@ApplicationScope
class NetworkViewModel @Inject internal constructor(
        context: Context,
        private val networkProtectionPreference: NetworkProtectionPreference,
        private val settings: Settings,
        private val networkController: NetworkController
) : OnNetworkSourceChangedListener {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(NetworkViewModel::class.java)
    }

    val isNetworkFeatureEnabled = ObservableBoolean()
    val defaultState = ObservableField<NetworkState>()
    val mobileDataState = ObservableField<NetworkState>()
    val scannedWifiList = ObservableArrayList<WifiItem>()
    val savedWifiList = ObservableArrayList<WifiItem>()

    //Current network
    val networkSource = ObservableField<NetworkSource>()
    val networkTitle = ObservableField<String>()
    val networkState = ObservableField<NetworkState>()

    var trustedWifiItems: Set<String>? = null
    var unTrustedWifiItems: Set<String>? = null
    var noneWifiItems: Set<String>? = null

    var lastScanResult = ArrayList<ScanResult>()

    var isWaitingForPermission = false

    val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            Log.d("NetworkFeature", "onCheckedChanged: isChecked = $isChecked")
            if (isChecked == isNetworkFeatureEnabled.get()) {
                return
            }
            handleNetworkFeatureState(isChecked)
        }
    }

    private val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var navigator: NetworkNavigator? = null

    var wifiScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            LOGGER.info("Wifi scan receiver, onReceive")
            val success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                // scan failure handling
                scanFailure()
            }
        }
    }

    init {
        networkController.setNetworkSourceChangedListener(this)

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        initStates()
    }

    fun initStates() {
        isNetworkFeatureEnabled.set(settings.isNetworkRulesEnabled)
        defaultState.set(networkProtectionPreference.defaultNetworkState)
        mobileDataState.set(networkProtectionPreference.mobileDataNetworkState)

        updateSavedWifiItems()
    }

    fun setNavigator(navigator: NetworkNavigator?) {
        this.navigator = navigator
    }

    fun updateNetworkSource(context: Context?) {
        networkController.updateNetworkSource(context)
    }

    fun scanWifiNetworks(context: Context?) {
        updateNetworkSource(context)

        val trustedWifiList = networkProtectionPreference.trustedWifiList
        val untrustedWifiList = networkProtectionPreference.untrustedWifiList
        val noneWifiList = networkProtectionPreference.noneWifiList

        val savedWifiItems: MutableList<WifiItem> = ArrayList()

        trustedWifiList?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.TRUSTED))) }
        untrustedWifiList?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.UNTRUSTED))) }
        noneWifiList?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.NONE))) }

        savedWifiItems.sortWith { item1: WifiItem, item2: WifiItem -> item1.title.compareTo(item2.title, ignoreCase = false) }

        savedWifiList.clear()
        savedWifiList.addAll(savedWifiItems)

        wifiManager.startScan()
        scanFailure()
    }

    private fun updateSavedWifiItems() {
        trustedWifiItems = networkProtectionPreference.trustedWifiList
        unTrustedWifiItems = networkProtectionPreference.untrustedWifiList
        noneWifiItems = networkProtectionPreference.noneWifiList

        val savedWifiItems: MutableList<WifiItem> = ArrayList()

        trustedWifiItems?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.TRUSTED))) }
        unTrustedWifiItems?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.UNTRUSTED))) }
        noneWifiItems?.forEach { savedWifiItems.add(WifiItem(it, ObservableField(NetworkState.NONE))) }

        savedWifiItems.sortWith { item1: WifiItem, item2: WifiItem -> item1.title.compareTo(item2.title, ignoreCase = false) }

        savedWifiList.clear()
        savedWifiList.addAll(savedWifiItems)
    }

    private fun updateNetworksRules(results: List<ScanResult>) {
        LOGGER.info("updateNetworksRules size = " + results.size)
        val scannedWifiItems: MutableList<WifiItem> = ArrayList()

        var item: WifiItem
        for (configuration in results) {
            if (configuration.SSID == null || configuration.SSID.isEmpty()) {
                continue
            }
            item = WifiItem(configuration.SSID, ObservableField(NetworkState.DEFAULT))
            trustedWifiItems?.let {
                if (it.contains(configuration.SSID)) {
                    item.networkState.set(NetworkState.TRUSTED)
                }
            }
            unTrustedWifiItems?.let {
                if (it.contains(configuration.SSID)) {
                    item.networkState.set(NetworkState.UNTRUSTED)
                }
            }
            noneWifiItems?.let {
                if (it.contains(configuration.SSID)) {
                    item.networkState.set(NetworkState.NONE)
                }
            }

            scannedWifiItems.add(item)
        }

        scannedWifiItems.sortWith { item1: WifiItem, item2: WifiItem -> item1.title.compareTo(item2.title, ignoreCase = false) }

        scannedWifiList.clear()
        scannedWifiList.addAll(scannedWifiItems)
    }

    private fun handleNetworkFeatureState(isEnabled: Boolean) {
        isNetworkFeatureEnabled.set(isEnabled)
        if (isEnabled) {
            if (!navigator!!.shouldAskForLocationPermission()) {
                applyNetworkFeatureState(true)
            }
        } else {
            applyNetworkFeatureState(false)
        }
    }

    fun applyNetworkFeatureState(isEnabled: Boolean) {
        LOGGER.info("applyNetworkFeatureState: isEnabled = $isEnabled")
        isNetworkFeatureEnabled.set(isEnabled)
        settings.isNetworkRulesEnabled = isEnabled
        if (isEnabled) {
            networkController.enableWifiWatcher()
        } else {
            networkController.disableWifiWatcher()
        }
    }

    override fun onNetworkSourceChanged(source: NetworkSource?) {
        LOGGER.info("onNetworkSourceChanged: source = $source")
        if (source == null) {
            return
        }
        if (source == NetworkSource.WIFI) {
            LOGGER.info("onNetworkSourceChanged: ssid = " + source.ssid)
        }
        networkSource.set(source)
        networkState.set(source.finalState)
        networkTitle.set(source.title)
    }

    override fun onDefaultNetworkStateChanged(defaultState: NetworkState) {
    }

    fun setMobileNetworkStateAs(state: NetworkState?) {
        mobileDataState.set(state)
        networkController.updateMobileDataState(state)
    }

    fun setDefaultNetworkStateAs(state: NetworkState?) {
        defaultState.set(state)
        networkController.updateDefaultNetworkState(state)
    }

    fun setWifiStateAs(wifiItem: WifiItem, newState: NetworkState?) {
        networkController.changeMarkFor(wifiItem.ssid, wifiItem.networkState.get(), newState)

        updateSavedWifiItems()
        updateNetworksRules(lastScanResult)
    }

    private fun scanSuccess() {
        LOGGER.info("Scan success")

        navigator?.let {
            if (it.isLocationPermissionGranted) {
                lastScanResult.clear()
                lastScanResult.addAll(wifiManager.scanResults)
                updateNetworksRules(lastScanResult)
            }
        }
    }

    private fun scanFailure() {
        LOGGER.info("Scan failure")
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        navigator?.let {
            if (it.isLocationPermissionGranted) {
                lastScanResult.clear()
                lastScanResult.addAll(wifiManager.scanResults)
                updateNetworksRules(lastScanResult)
            }
        }
    }

    fun reset() {
        isNetworkFeatureEnabled.set(false)
        networkSource.set(null)
        networkTitle.set(null)
        networkState.set(null)
    }

    fun sessionClean() {
        applyNetworkFeatureState(false)
    }
}