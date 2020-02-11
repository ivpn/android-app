package net.ivpn.client.common.migration;

import android.util.Log;

import net.ivpn.client.common.prefs.NetworkProtectionPreference;
import net.ivpn.client.common.prefs.Preference;
import net.ivpn.client.common.prefs.SettingsPreference;

import java.util.Set;

public class UF0T1 {

    private static final String TAG = UF0T1.class.getSimpleName();

    private SettingsPreference settingsPreference;
    private NetworkProtectionPreference networkPreference;

    UF0T1(SettingsPreference settingsPreference, NetworkProtectionPreference networkPreference) {
        this.settingsPreference = settingsPreference;
        this.networkPreference = networkPreference;
    }

    public void update() {
        Log.d(TAG, "update: ");
        boolean isNetworkRulesExist = settingsPreference.isNetworkRuleSettingsExist();
        Log.d(TAG, "update: isNetworkRulesExist = " + isNetworkRulesExist);
        if (isNetworkRulesExist) {
            return;
        }

        Log.d(TAG, "update: preference.isDefaultBehaviourExist() = " + networkPreference.isDefaultBehaviourExist());
        Log.d(TAG, "update: preference.isMobileBehaviourExist() = " + networkPreference.isMobileBehaviourExist());
        if (networkPreference.isDefaultBehaviourExist() || networkPreference.isMobileBehaviourExist()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }

        Set<String> trustedSsid = networkPreference.getTrustedWifiList();
        Set<String> untrustedSsid = networkPreference.getUntrustedWifiList();
        Log.d(TAG, "update: trustedSsid = " + trustedSsid);
        if (trustedSsid != null) {
            Log.d(TAG, "update: trustedSsid.size() = " + trustedSsid.size());
        }

        Log.d(TAG, "update: untrustedSsid = " + untrustedSsid);
        if (untrustedSsid != null) {
            Log.d(TAG, "update: untrustedSsid.size() = " + untrustedSsid.size());
        }

        if (trustedSsid != null && !trustedSsid.isEmpty()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }
        if (untrustedSsid != null && !untrustedSsid.isEmpty()) {
            settingsPreference.putSettingsNetworkRules(true);
            return;
        }

        settingsPreference.putSettingsNetworkRules(false);
    }
}
