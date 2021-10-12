package net.ivpn.core.vpn.local;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavDeepLinkBuilder;

import net.ivpn.core.IVPNApplication;
import net.ivpn.core.R;
import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.prefs.EncryptedSettingsPreference;
import net.ivpn.core.common.prefs.NetworkProtectionPreference;
import net.ivpn.core.common.utils.NetworkUtil;
import net.ivpn.core.common.utils.StringUtil;
import net.ivpn.core.v2.network.OnNetworkSourceChangedListener;
import net.ivpn.core.vpn.GlobalBehaviorController;
import net.ivpn.core.vpn.ServiceConstants;
import net.ivpn.core.vpn.model.NetworkSource;
import net.ivpn.core.vpn.model.NetworkState;
import net.ivpn.core.vpn.model.VPNRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.inject.Inject;

import static net.ivpn.core.vpn.model.NetworkSource.MOBILE_DATA;
import static net.ivpn.core.vpn.model.NetworkSource.NO_NETWORK;
import static net.ivpn.core.vpn.model.NetworkSource.WIFI;
import static net.ivpn.core.vpn.model.NetworkState.DEFAULT;
import static net.ivpn.core.vpn.model.NetworkState.NONE;
import static net.ivpn.core.vpn.model.NetworkState.TRUSTED;
import static net.ivpn.core.vpn.model.NetworkState.UNTRUSTED;

@ApplicationScope
public class NetworkController implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkController.class);
    private static final String NONE_SSID = "<unknown ssid>";

    private boolean isWifiWatcherSettingEnabled;
    private WifiBroadcastReceiver receiver;
    private NetworkSource source;
    private OnNetworkSourceChangedListener networkSourceChangedListener;
    private NetworkState defaultState;
    private NetworkState mobileState;
    private Set<String> trustedWiFis;
    private Set<String> untrustedWiFis;

    private EncryptedSettingsPreference settingsPreference;
    private NetworkProtectionPreference networkProtectionPreference;
    private GlobalBehaviorController globalBehaviorController;

    @Inject
    public NetworkController(NetworkProtectionPreference networkProtectionPreference,
                             EncryptedSettingsPreference settingsPreference, GlobalBehaviorController globalBehaviorController) {
        this.networkProtectionPreference = networkProtectionPreference;
        this.settingsPreference = settingsPreference;
        this.globalBehaviorController = globalBehaviorController;
    }

    public void init() {
        LOGGER.info("Init");
        initInnerState();
//        isWifiWatcherSettingEnabled = settingsPreference.getSettingNetworkRules();
//        defaultState = networkProtectionPreference.getDefaultNetworkState();
//        mobileState = networkProtectionPreference.getMobileDataNetworkState();
//        trustedWiFis = networkProtectionPreference.getTrustedWifiList();
//        untrustedWiFis = networkProtectionPreference.getUntrustedWifiList();
        registerReceiver();
    }

    private void initInnerState() {
        isWifiWatcherSettingEnabled = settingsPreference.getSettingNetworkRules();
        defaultState = networkProtectionPreference.getDefaultNetworkState();
        mobileState = networkProtectionPreference.getMobileDataNetworkState();
        trustedWiFis = networkProtectionPreference.getTrustedWifiList();
        untrustedWiFis = networkProtectionPreference.getUntrustedWifiList();
    }

    public boolean isWifiWatcherSettingEnabled() {
        return isWifiWatcherSettingEnabled;
    }

    public void tryWifiWatcher() {
        LOGGER.info("tryWifiWatcher");
        if (shouldWifiWatchedBeEnabled()) {
            startWifiWatcherService();
        }
    }

    public void enableWifiWatcher() {
        LOGGER.info("enableWifiWatcher");
        isWifiWatcherSettingEnabled = true;
        if (shouldWifiWatchedBeEnabled()) {
            startWifiWatcherService();
        }
    }

    public void disableWifiWatcher() {
        LOGGER.info("disableWifiWatcher");
        isWifiWatcherSettingEnabled = false;
        stopWifiWatcherService();
    }

    private boolean shouldWifiWatchedBeEnabled() {
        LOGGER.info("shouldWifiWatchedBeEnabled");
        if (!isWifiWatcherSettingEnabled) {
            return false;
        }
        if (!untrustedWiFis.isEmpty()) {
            return true;
        }
        if (!trustedWiFis.isEmpty()) {
            return true;
        }
        if (defaultState.equals(TRUSTED) || defaultState.equals(UNTRUSTED)) {
            return true;
        }
        if (mobileState.equals(TRUSTED) || mobileState.equals(UNTRUSTED)) {
            return true;
        }
        return false;
    }

    public void updateNetworkSource(Context context) {
        LOGGER.info("Trying update network source");
        if (source != null && networkSourceChangedListener != null
                && !(source.equals(WIFI) && source.getSsid().equals(NONE_SSID))) {
            networkSourceChangedListener.onNetworkSourceChanged(source);
            return;
        }
        source = NetworkSource.NO_NETWORK;
        LOGGER.info("Updating network source...");
        NetworkSource source = NetworkUtil.getCurrentSource(context);
        switch (source) {
            case WIFI: {
                String currentWiFiSsid = NetworkUtil.getCurrentWifiSsid(context);
                if (currentWiFiSsid != null) {
                    onWifiChanged(StringUtil.formatWifiSSID(currentWiFiSsid));
                } else {
                    onNoNetwork();
                }
                break;
            }
            case MOBILE_DATA: {
                onMobileData();
                break;
            }
            case NO_NETWORK: {
                onNoNetwork();
                break;
            }
            case UNDEFINED: {
                //do nothing
                break;
            }
        }
        if (networkSourceChangedListener != null) {
            networkSourceChangedListener.onNetworkSourceChanged(source);
        }
    }

    public void updateDefaultNetworkState(NetworkState defaultState) {
        LOGGER.info("Updating default network state with " + defaultState);
        if (this.defaultState.equals(defaultState)) {
            return;
        }
        this.defaultState = defaultState;
        networkProtectionPreference.setDefaultNetworkState(defaultState);
        if (shouldWifiWatchedBeEnabled()) {
            startWifiWatcherService();
        } else {
            stopWifiWatcherService();
        }
        if (source != null && source.getState() != null && source.getState().equals(DEFAULT)) {
            applyNetworkStateBehaviour(defaultState);
            source.setDefaultState(defaultState);
            if (networkSourceChangedListener != null) {
                networkSourceChangedListener.onNetworkSourceChanged(source);
            }
        }
        if (networkSourceChangedListener != null) {
            networkSourceChangedListener.onDefaultNetworkStateChanged(defaultState);
        }
    }

    public void changeMarkFor(String ssid, NetworkState oldState, NetworkState newState) {
        LOGGER.info("changeMarkFor: Wi-Fi oldState = " + oldState + " newState = " + newState);
        if (ssid == null) {
            return;
        }
        if (oldState.equals(newState)) {
            return;
        }
        removeMarkFor(ssid, oldState);
        addMarkFor(ssid, newState);
        if (shouldWifiWatchedBeEnabled()) {
            startWifiWatcherService();
        } else {
            stopWifiWatcherService();
        }
        if (source != null && source == WIFI && ssid.equals(StringUtil.formatWifiSSID(source.getSsid()))) {
            source.setState(newState);
            networkSourceChangedListener.onNetworkSourceChanged(source);
        }
    }

    private void addMarkFor(String ssid, NetworkState newState) {
        LOGGER.info("addMarkFor: Wi-Fi newState = " + newState);

        updateWifiState(ssid, newState);
        switch (newState) {
            case TRUSTED: {
                networkProtectionPreference.markWifiAsTrusted(ssid);
                trustedWiFis.add(ssid);
                break;
            }
            case UNTRUSTED: {
                networkProtectionPreference.markWifiAsUntrusted(ssid);
                untrustedWiFis.add(ssid);
                break;
            }
            case NONE: {
                networkProtectionPreference.markWifiAsNone(ssid);
                break;
            }
        }
    }

    private void removeMarkFor(String ssid, NetworkState oldState) {
        LOGGER.info("remove mark for Wi-Fi old state = " + oldState);
        switch (oldState) {
            case TRUSTED: {
                networkProtectionPreference.removeMarkWifiAsTrusted(ssid);
                trustedWiFis.remove(ssid);
                break;
            }
            case UNTRUSTED: {
                networkProtectionPreference.removeMarkWifiAsUntrusted(ssid);
                untrustedWiFis.remove(ssid);
                break;
            }
            case NONE: {
                networkProtectionPreference.removeMarkWifiAsNone(ssid);
                break;
            }
        }
    }

    private NetworkState getNetworkStateFor(String ssid) {
        Set<String> trustedSsid = networkProtectionPreference.getTrustedWifiList();
        Set<String> untrustedSsid = networkProtectionPreference.getUntrustedWifiList();
        Set<String> noneSsid = networkProtectionPreference.getNoneWifiList();
        NetworkState state = DEFAULT;
        if (trustedSsid.contains(ssid)) {
            state = TRUSTED;
        } else if (untrustedSsid.contains(ssid)) {
            state = UNTRUSTED;
        } else if (noneSsid.contains(ssid)) {
            state = NONE;
        }
        return state;
    }

    public void setNetworkSourceChangedListener(OnNetworkSourceChangedListener listener) {
        networkSourceChangedListener = listener;
        networkSourceChangedListener.onNetworkSourceChanged(source);
    }

    public void removeNetworkSourceListener() {
        networkSourceChangedListener = null;
    }

    private void startWifiWatcherService() {
        LOGGER.info("startWifiWatcherService: ");
        Context context = IVPNApplication.application;

        Intent startIntent = new Intent(context, WifiWatcherService.class);
        startIntent.setAction(START_WIFI_WATCHER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent);
        } else {
            context.startService(startIntent);
        }

        unregisterLocalWiFiWatcherReceiver();
    }

    private void stopWifiWatcherService() {
        LOGGER.info("stopWifiWatcherService");
        if (!WifiWatcherService.isRunning.get()) {
            return;
        }
        Context context = IVPNApplication.application;
        Intent stopIntent = new Intent(context, WifiWatcherService.class);
        stopIntent.setAction(STOP_WIFI_WATCHER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(stopIntent);
        } else {
            context.startService(stopIntent);
        }

        if (isWifiWatcherSettingEnabled) {
            registerLocalWiFiWatcherReceiver();
        }
    }

    private void onWifiChanged(String wifiSsid) {
        if (wifiSsid == null) {
            return;
        }
        if (source != null && source.equals(WIFI) && wifiSsid.equals(source.getSsid())) {
            return;
        }

        source = WIFI;
        source.setSsid(wifiSsid);

        NetworkState state = getNetworkStateFor(wifiSsid);
        source.setState(state);
        source.setDefaultState(networkProtectionPreference.getDefaultNetworkState());
        if (networkSourceChangedListener != null) {
            networkSourceChangedListener.onNetworkSourceChanged(source);
        }
        applyNetworkStateBehaviour(state);
    }

    private void onMobileData() {
        if (source != null && source.equals(MOBILE_DATA)) {
            return;
        }
        LOGGER.info("onMobileData: ");

        source = MOBILE_DATA;
        NetworkState state = networkProtectionPreference.getMobileDataNetworkState();
        source.setState(state);
        source.setDefaultState(networkProtectionPreference.getDefaultNetworkState());
        if (networkSourceChangedListener != null) {
            networkSourceChangedListener.onNetworkSourceChanged(source);
        }
        applyNetworkStateBehaviour(state);
    }

    private void onNoNetwork() {
        if (source != null && source.equals(NO_NETWORK)) {
            return;
        }
        LOGGER.info("onNoNetwork: ");
        source = NO_NETWORK;
        if (networkSourceChangedListener != null) {
            networkSourceChangedListener.onNetworkSourceChanged(source);
        }
    }

    private void applyNetworkStateBehaviour(NetworkState state) {
        LOGGER.info("applyNetworkStateBehaviour: state = " + state);
        switch (state) {
            case TRUSTED: {
                applyTrustedBehaviour();
                break;
            }
            case UNTRUSTED: {
                applyUntrustedBehaviour();
                break;
            }
            case NONE: {
                applyNoneBehaviour();
                break;
            }
            case DEFAULT: {
                NetworkState defaultNetworkState = networkProtectionPreference.getDefaultNetworkState();
                applyNetworkStateBehaviour(defaultNetworkState);
                break;
            }
        }
    }

    private void updateWifiState(String wifiSsid, NetworkState networkState) {
        if (source == null || !source.equals(WIFI)) {
            return;
        }
        if (wifiSsid.equals(source.getSsid())) {
            source.setState(networkState);
            applyNetworkStateBehaviour(networkState);
        }
    }

    public void updateMobileDataState(NetworkState networkState) {
        if (mobileState != null && mobileState.equals(networkState)) {
            return;
        }
        mobileState = networkState;
        networkProtectionPreference.setMobileDataNetworkState(networkState);
        if (shouldWifiWatchedBeEnabled()) {
            startWifiWatcherService();
        } else {
            stopWifiWatcherService();
        }
        if (source == null || !source.equals(MOBILE_DATA)) {
            return;
        }
        LOGGER.info("updateMobileDataState: networkState = " + networkState);
        source.setState(networkState);
        applyNetworkStateBehaviour(networkState);
        networkSourceChangedListener.onNetworkSourceChanged(source);
    }

    private void applyTrustedBehaviour() {
        LOGGER.info("Apply trusted behaviour");
        boolean shouldDisconnectFromVPN = settingsPreference.getRuleDisconnectFromVpn();

        VPNRule vpnRule = VPNRule.NOTHING;
        if (shouldDisconnectFromVPN) {
            vpnRule = VPNRule.DISCONNECT;
        }
        if (isWifiWatcherSettingEnabled) {
            globalBehaviorController.applyNetworkRules(vpnRule);
        }
    }

    private void applyUntrustedBehaviour() {
        LOGGER.info("Apply untrusted behaviour");
        boolean shouldConnectToVpn = settingsPreference.getRuleConnectToVpn();

        VPNRule vpnRule = VPNRule.NOTHING;

        if (shouldConnectToVpn) {
            vpnRule = VPNRule.CONNECT;
        }
        if (isWifiWatcherSettingEnabled) {
            globalBehaviorController.applyNetworkRules(vpnRule);
        }
    }

    private void applyNoneBehaviour() {
        LOGGER.info("Apply none behaviour");
        if (isWifiWatcherSettingEnabled) {
            globalBehaviorController.applyNetworkRules(VPNRule.NOTHING);
        }
    }

    private void applyWifiWatcherAction(Intent intent) {
        LOGGER.info("applyWifiWatcherAction");
        String action = intent.getStringExtra(WIFI_WATCHER_ACTION_EXTRA);
        if (action == null) return;

        switch (action) {
            case APP_SETTINGS_ACTION:
                openSettings();
                break;
            case WIFI_CHANGED_ACTION:
                onWifiChanged(StringUtil.formatWifiSSID(intent.getStringExtra(WIFI_WATCHER_ACTION_VALUE)));
                break;
            case ON_MOBILE_DATA_ACTION:
                onMobileData();
                break;
            case NO_NETWORK_ACTION:
                onNoNetwork();
                break;
        }
    }

    private void openSettings() {
        new NavDeepLinkBuilder(IVPNApplication.application)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.networkProtectionFragment).createTaskStackBuilder().startActivities();
    }

    private void registerReceiver() {
        BroadcastReceiver wifiWatcherBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                applyWifiWatcherAction(intent);
            }
        };

        LocalBroadcastManager.getInstance(IVPNApplication.application).registerReceiver(wifiWatcherBroadcastReceiver,
                new IntentFilter(WIFI_WATCHER_ACTION));

        if (isWifiWatcherSettingEnabled && !shouldWifiWatchedBeEnabled()) {
            registerLocalWiFiWatcherReceiver();
        }
    }

    private void registerLocalWiFiWatcherReceiver() {
        if (receiver != null) {
            return;
        }
        receiver = new WifiBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        IVPNApplication.application.registerReceiver(receiver, intentFilter);
    }

    private void unregisterLocalWiFiWatcherReceiver() {
        if (receiver == null) {
            return;
        }

        IVPNApplication.application.unregisterReceiver(receiver);
        receiver = null;
    }

    public void finishAll() {
        LOGGER.info("finishAll");
        stopWifiWatcherService();
        source = null;
        initInnerState();
    }

    public void changeConnectToVpnRule(boolean isEnabled) {
        LOGGER.info("Change connect to vpn rule: " + isEnabled);
        settingsPreference.putRuleConnectToVpn(isEnabled);
        if (source == null || source.equals(NO_NETWORK)) {
            return;
        }
        if (source.getState().equals(UNTRUSTED)
                || (source.getState().equals(DEFAULT) && source.getDefaultState().equals(UNTRUSTED))) {
            globalBehaviorController.applyVpnRule(isEnabled ? VPNRule.CONNECT : VPNRule.NOTHING);
        }
    }

    public void changeDisconnectFromVpnRule(boolean isEnabled) {
        LOGGER.info("changeDisconnectFromVpnRule: " + isEnabled);
        settingsPreference.putRuleDisconnectFromVpn(isEnabled);
        if (source == null || source.equals(NO_NETWORK)) {
            return;
        }
        if (source.getState().equals(TRUSTED)
                || (source.getState().equals(DEFAULT) && source.getDefaultState().equals(TRUSTED))) {
            globalBehaviorController.applyVpnRule(isEnabled ? VPNRule.DISCONNECT : VPNRule.NOTHING);
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                NetworkSource source = NetworkUtil.getCurrentSource(context);
                LOGGER.info("onReceive: source = " + source);
                switch (source) {
                    case WIFI: {
                        String currentWiFiSsid = NetworkUtil.getCurrentWifiSsid(context);
                        if (currentWiFiSsid != null) {
                            onWifiChanged(StringUtil.formatWifiSSID(currentWiFiSsid));
                        } else {
                            onNoNetwork();
                        }
                        break;
                    }
                    case MOBILE_DATA: {
                        onMobileData();
                        break;
                    }
                    case NO_NETWORK: {
                        onNoNetwork();
                        break;
                    }
                    case UNDEFINED: {
                        //do nothing
                        break;
                    }
                }
            }
        }
    }
}