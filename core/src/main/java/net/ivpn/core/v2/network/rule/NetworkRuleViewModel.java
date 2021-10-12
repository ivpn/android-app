package net.ivpn.core.v2.network.rule;

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

import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.databinding.ObservableBoolean;

import net.ivpn.core.common.prefs.EncryptedSettingsPreference;
import net.ivpn.core.vpn.local.NetworkController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NetworkRuleViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRuleViewModel.class);

    public final ObservableBoolean isConnectToVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isEnableKillSwitchRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisconnectFromVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisableKillSwitchRuleApplied = new ObservableBoolean();

    private EncryptedSettingsPreference settingsPreference;
    private NetworkController networkController;

    public final OnCheckedChangeListener connectToVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable connect to VPN rule: " + isChecked);
        networkController.changeConnectToVpnRule(isChecked);
    };
    public final OnCheckedChangeListener disconnectFromVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable disconnect from VPN rule: " + isChecked);
        networkController.changeDisconnectFromVpnRule(isChecked);
    };

    @Inject
    NetworkRuleViewModel(EncryptedSettingsPreference settingsPreference, NetworkController networkController) {
        this.settingsPreference = settingsPreference;
        this.networkController = networkController;

        init();
    }

    private void init() {
        isConnectToVpnRuleApplied.set(settingsPreference.getRuleConnectToVpn());
        isDisconnectFromVpnRuleApplied.set(settingsPreference.getRuleDisconnectFromVpn());
    }
}
