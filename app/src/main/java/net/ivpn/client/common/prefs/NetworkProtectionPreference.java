package net.ivpn.client.common.prefs;

import android.content.SharedPreferences;

import net.ivpn.client.vpn.model.NetworkState;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class NetworkProtectionPreference {

    private static final String SETTINGS_TRUSTED_WIFI_LIST = "SETTINGS_TRUSTED_WIFI_LIST";
    private static final String SETTINGS_UNTRUSTED_WIFI_LIST = "SETTINGS_UNTRUSTED_WIFI_LIST";
    private static final String SETTINGS_NONE_WIFI_LIST = "SETTINGS_NONE_WIFI_LIST";

    private static final String SETTINGS_DEFAULT_NETWORK_STATE = "SETTINGS_DEFAULT_NETWORK_STATE";
    private static final String SETTINGS_MOBILE_DATE_NETWORK_STATE = "SETTINGS_MOBILE_DATE_NETWORK_STATE";

    private Preference preference;

    @Inject
    public NetworkProtectionPreference(Preference preference) {
        this.preference = preference;
    }

    public void markWifiAsTrusted(String wifiSsid) {
        Set<String> trustedWifi = getTrustedWifiList();
        if (wifiSsid == null || trustedWifi.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(trustedWifi);
        newTrustedWifiList.add(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_TRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public void removeMarkWifiAsTrusted(String wifiSsid) {
        Set<String> trustedWifi = getTrustedWifiList();
        if (wifiSsid == null || !trustedWifi.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(trustedWifi);
        newTrustedWifiList.remove(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_TRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public Set<String> getTrustedWifiList() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getStringSet(SETTINGS_TRUSTED_WIFI_LIST, new HashSet<String>());
    }

    public void markWifiAsUntrusted(String wifiSsid) {
        Set<String> untrustedWifi = getUntrustedWifiList();
        if (wifiSsid == null || untrustedWifi.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(untrustedWifi);
        newTrustedWifiList.add(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public void removeMarkWifiAsUntrusted(String wifiSsid) {
        Set<String> untrustedWifi = getUntrustedWifiList();
        if (wifiSsid == null || !untrustedWifi.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(untrustedWifi);
        newTrustedWifiList.remove(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public Set<String> getUntrustedWifiList() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getStringSet(SETTINGS_UNTRUSTED_WIFI_LIST, new HashSet<String>());
    }

    public void markWifiAsNone(String wifiSsid) {
        Set<String> noneWifis = getNoneWifiList();
        if (wifiSsid == null || noneWifis.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(noneWifis);
        newTrustedWifiList.add(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_NONE_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public void removeMarkWifiAsNone(String wifiSsid) {
        Set<String> noneWifis = getNoneWifiList();
        if (wifiSsid == null || !noneWifis.contains(wifiSsid)) {
            return;
        }
        Set<String> newTrustedWifiList = new HashSet<>(noneWifis);
        newTrustedWifiList.remove(wifiSsid);

        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putStringSet(SETTINGS_NONE_WIFI_LIST, newTrustedWifiList)
                .apply();
    }

    public Set<String> getNoneWifiList() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getStringSet(SETTINGS_NONE_WIFI_LIST, new HashSet<>());
    }

    public void setDefaultNetworkState(NetworkState defaultNetworkState) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_DEFAULT_NETWORK_STATE, defaultNetworkState.name())
                .apply();
    }

    public NetworkState getDefaultNetworkState() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        String defaultNetworkStateName = sharedPreferences.getString(SETTINGS_DEFAULT_NETWORK_STATE,
                NetworkState.NONE.name());
        return NetworkState.valueOf(defaultNetworkStateName);
    }

    public void setMobileDataNetworkState(NetworkState mobileDataNetworkState) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_MOBILE_DATE_NETWORK_STATE, mobileDataNetworkState.name())
                .apply();
    }

    public NetworkState getMobileDataNetworkState() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        String mobileDataNetworkStateName = sharedPreferences.getString(SETTINGS_MOBILE_DATE_NETWORK_STATE,
                NetworkState.DEFAULT.name());
        return NetworkState.valueOf(mobileDataNetworkStateName);
    }

    public boolean isDefaultBehaviourExist() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.contains(SETTINGS_DEFAULT_NETWORK_STATE);
    }

    public boolean isMobileBehaviourExist() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.contains(SETTINGS_MOBILE_DATE_NETWORK_STATE);
    }
}
