package net.ivpn.client.ui.network.rules;

import androidx.databinding.ObservableBoolean;
import android.widget.CompoundButton.OnCheckedChangeListener;

import net.ivpn.client.common.prefs.SettingsPreference;
import net.ivpn.client.vpn.local.NetworkController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NetworkRuleViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRuleViewModel.class);

    public final ObservableBoolean isConnectToVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isEnableKillSwitchRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisconnectFromVpnRuleApplied = new ObservableBoolean();
    public final ObservableBoolean isDisableKillSwitchRuleApplied = new ObservableBoolean();

    private SettingsPreference settingsPreference;
    private NetworkController networkController;

    public final OnCheckedChangeListener connectToVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable connect to VPN rule: " + isChecked);
        networkController.changeConnectToVpnRule(isChecked);
    };
    public final OnCheckedChangeListener enableKillSwitchRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable kill-switch rule: " + isChecked);
        networkController.changeEnableKillSwitchRule(isChecked);
    };
    public final OnCheckedChangeListener disconnectFromVpnRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Enable disconnect from VPN rule: " + isChecked);
        networkController.changeDisconnectFromVpnRule(isChecked);
    };
    public final OnCheckedChangeListener disableKillSwitchRuleChangeListener = (buttonView, isChecked) -> {
        LOGGER.info("Disable kill-switch rule: " + isChecked);
        networkController.changeDisableKillSwitchRule(isChecked);
    };

    @Inject
    NetworkRuleViewModel(SettingsPreference settingsPreference, NetworkController networkController) {
        this.settingsPreference = settingsPreference;
        this.networkController = networkController;

        init();
    }

    private void init() {
        isConnectToVpnRuleApplied.set(settingsPreference.getRuleConnectToVpn());
        isEnableKillSwitchRuleApplied.set(settingsPreference.getRuleEnableKillSwitch());
        isDisconnectFromVpnRuleApplied.set(settingsPreference.getRuleDisconnectFromVpn());
        isDisableKillSwitchRuleApplied.set(settingsPreference.getRuleDisableKillSwitch());
    }
}
