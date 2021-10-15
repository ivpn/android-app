package net.ivpn.core.v2.network

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.databinding.FragmentNetworkBinding
import net.ivpn.core.v2.dialog.DialogBuilder
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.MainActivity
import net.ivpn.core.v2.dialog.DialogBuilderK.openChangeDefaultNetworkStatusDialogue
import net.ivpn.core.v2.dialog.DialogBuilderK.openChangeNetworkStatusDialogue
import net.ivpn.core.v2.network.dialog.NetworkChangeDialogViewModel
import org.slf4j.LoggerFactory
import javax.inject.Inject

class NetworkCommonFragment : Fragment(), NetworkNavigator {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(NetworkCommonFragment::class.java)
        private const val LOCATION_PERMISSION_CODE = 132
    }

    private lateinit var binding: FragmentNetworkBinding

    @Inject
    lateinit var network: NetworkViewModel

    lateinit var adapter: NetworksPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_network, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        initViews()
        initToolbar()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        LOGGER.info("onRequestPermissionsResult requestCode = $requestCode")
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                LOGGER.info("onRequestPermissionsResult grantResults.isEmpty() = ${grantResults.isEmpty()}")
                if (grantResults.isEmpty()) {
                    return
                }
                LOGGER.info("onRequestPermissionsResult grantResults[0] = ${grantResults[0]}")
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    network.applyNetworkFeatureState(true)
                    network.scanWifiNetworks(context)
                    return
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    network.applyNetworkFeatureState(false)
                    return
                }
            }
        }
    }

    override fun onResume() {
        LOGGER.info("onResume")
        super.onResume()
        checkLocationPermission()
    }

    override fun onStart() {
        super.onStart()
        network.initStates()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isBackgroundLocationPermissionGranted()) {
                network.scanWifiNetworks(context)
            }
        } else {
            if (isPermissionGranted()) {
                network.scanWifiNetworks(context)
            }
        }
        activity?.let {
            if (it is MainActivity) {
                it.setContentSecure(true)
            }
        }
    }

    private fun initViews() {
        network.setNavigator(this)
        adapter = NetworksPagerAdapter(requireContext(), childFragmentManager)
        binding.contentLayout.pager.adapter = adapter
        binding.contentLayout.slidingTabs.setupWithViewPager(binding.contentLayout.pager)

        binding.contentLayout.viewmodel = network
        binding.contentLayout.formatter = NetworkStateFormatter(requireContext())
        binding.contentLayout.mobileContentLayout.setOnClickListener {
            network.mobileDataState.get()?.let { state ->
                openChangeNetworkStatusDialogue(
                    requireContext(),
                    object : NetworkChangeDialogViewModel(state) {
                        override fun apply() {
                            network.setMobileNetworkStateAs(selectedState.get())
                        }
                    })
            }
        }

        binding.contentLayout.defaultLayout.setOnClickListener {
            network.defaultState.get()?.let { state ->
                openChangeDefaultNetworkStatusDialogue(
                    requireContext(),
                    object : NetworkChangeDialogViewModel(state) {
                        override fun apply() {
                            network.setDefaultNetworkStateAs(selectedState.get())
                        }
                    })
            }
        }

        binding.contentLayout.rulesAction.setOnClickListener {
            toRules()
        }
    }

    private fun initToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun goToAndroidAppSettings() {
        LOGGER.info("goToAndroidAppSettings")
        val action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri =
            Uri.parse(getString(R.string.settings_package) + IVPNApplication.config.applicationId)
        val intent = Intent(action, uri)
        startActivity(intent)
    }

    private fun checkLocationPermission() {
        val isEnabled: Boolean = network.isNetworkFeatureEnabled.get()
        LOGGER.info("checkLocationPermission isEnabled = $isEnabled")
        if (!isEnabled) {
            if (network.isWaitingForPermission) {
                network.isWaitingForPermission = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (isBackgroundLocationPermissionGranted()) {
                        network.applyNetworkFeatureState(true)
                    }
                }
            }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isBackgroundLocationPermissionGranted()) {
                network.applyNetworkFeatureState(true)
                return
            }

            askPermissionRationale()
        } else {
            val isPermissionGranted: Boolean = isPermissionGranted()
            LOGGER.info("isPermissionGranted = $isPermissionGranted")
            if (isPermissionGranted) {
                network.applyNetworkFeatureState(true)
                return
            }
            askPermissionRationale()
        }
    }

    override fun toRules() {
        openNetworkProtectionRulesScreen()
    }

    override fun shouldAskForLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return askForBackgroundLocationPermission()
        } else {
            askForLocationPermission()
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        if (!isAdded) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return isBackgroundLocationPermissionGranted()
        } else {
            isPermissionGranted()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun askForBackgroundLocationPermission(): Boolean {
        LOGGER.info("askForBackgroundLocationPermission")
        val isBackgroundPermissionGranted: Boolean = isBackgroundLocationPermissionGranted()
        LOGGER.info("isPermissionGranted = $isBackgroundPermissionGranted")
        if (isBackgroundPermissionGranted) {
            return false
        }
        askBackgroundPermissionRationale()

        return true
    }

    private fun askForLocationPermission(): Boolean {
        val isPermissionGranted: Boolean = isPermissionGranted()

        LOGGER.info("isPermissionGranted = $isPermissionGranted")
        if (isPermissionGranted) {
            return false
        }
        val shouldRequestRationale: Boolean = shouldRequestRationale()
        LOGGER.info("shouldRequestRationale = $shouldRequestRationale")
        if (shouldRequestRationale) {
            askPermissionRationale()
        } else {
            showInformationDialog()
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askBackgroundPermissionRationale() {
        LOGGER.info("askBackgroundPermissionRationale")
        DialogBuilder.createNonCancelableDialog(requireActivity(),
            Dialogs.ASK_BACKGROUND_LOCATION_PERMISSION,
            positiveAction = {
                network.isWaitingForPermission = true
                goToAndroidAppSettings()
            },
            cancelAction = { network.applyNetworkFeatureState(false) })
    }

    private fun askPermissionRationale() {
        LOGGER.info("askPermissionRationale")
        DialogBuilder.createNonCancelableDialog(requireActivity(), Dialogs.ASK_LOCATION_PERMISSION,
            positiveAction = {goToAndroidAppSettings()},
            cancelAction = { network.applyNetworkFeatureState(false) })
    }

    private fun showInformationDialog() {
        LOGGER.info("showInformationDialog")
        DialogBuilder.createNonCancelableDialog(requireActivity(), Dialogs.LOCATION_PERMISSION_INFO,
            positiveAction = {}, cancelAction = { askForegroundLocationPermission() })
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundLocationPermissionGranted(): Boolean {
        return (checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun isPermissionGranted(): Boolean {
        return (checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun shouldRequestRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LOGGER.info("Fine location permission ${shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)}")
            LOGGER.info(
                "Background location permission ${
                    shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }"
            )
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun askForegroundLocationPermission() {
        LOGGER.info("Ask Location Permission")
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_CODE
        )
    }

    private fun openNetworkProtectionRulesScreen() {
        val action =
            NetworkCommonFragmentDirections.actionNetworkProtectionFragmentToNetworkProtectionRulesFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }
}