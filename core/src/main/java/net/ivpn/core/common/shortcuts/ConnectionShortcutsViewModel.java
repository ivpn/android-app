package net.ivpn.core.common.shortcuts;

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

import net.ivpn.core.common.prefs.EncryptedUserPreference;
import net.ivpn.core.vpn.controller.VpnBehaviorController;

import javax.inject.Inject;

public class ConnectionShortcutsViewModel {

    private EncryptedUserPreference userPreference;
    private VpnBehaviorController vpnBehaviorController;

    @Inject
    ConnectionShortcutsViewModel(EncryptedUserPreference userPreference, VpnBehaviorController vpnBehaviorController) {
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
