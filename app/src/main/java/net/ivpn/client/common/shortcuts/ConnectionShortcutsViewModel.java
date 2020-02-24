package net.ivpn.client.common.shortcuts;

import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import javax.inject.Inject;

public class ConnectionShortcutsViewModel {

    private UserPreference userPreference;
    private VpnBehaviorController vpnBehaviorController;

    @Inject
    ConnectionShortcutsViewModel(UserPreference userPreference, VpnBehaviorController vpnBehaviorController) {
        this.userPreference = userPreference;
        this.vpnBehaviorController = vpnBehaviorController;
    }

    boolean isCredentialsExist() {
        return !userPreference.getSessionToken().isEmpty();
    }

    void startVpn() {
        vpnBehaviorController.connectActionByRules();
    }

    void stopVpn() {
        vpnBehaviorController.disconnect();
    }

}
