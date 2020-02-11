package net.ivpn.client.ui.settings;

import android.view.View;

import net.ivpn.client.common.prefs.ServerType;

public interface SettingsNavigator {

    void authenticate();

    void subscribe();

    void logout();

    void splitTunneling();

    void customDNS();

    void antiTracker();

    void chooseServer(ServerType serverType);

    void notifyUser(int msgId, int actionId, View.OnClickListener listener);

    void enableKillSwitch(boolean value, boolean isAdvancedKillSwitchDialogEnabled);
}
