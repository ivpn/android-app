package net.ivpn.client.common.prefs;

import android.content.SharedPreferences;

import net.ivpn.client.vpn.Protocol;

import javax.inject.Inject;

public class StickyPreference {

    private static final String CURRENT_PROTOCOL = "CURRENT_PROTOCOL";

    private Preference preference;

    @Inject
    public StickyPreference(Preference preference) {
        this.preference = preference;
    }

    public String getCurrentProtocol() {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        return sharedPreferences.getString(CURRENT_PROTOCOL, Protocol.OPENVPN.name());
    }

    public void putCurrentProtocol(Protocol protocol) {
        SharedPreferences sharedPreferences = preference.getStickySharedPreferences();
        sharedPreferences.edit()
                .putString(CURRENT_PROTOCOL, protocol.name())
                .apply();
    }
}
