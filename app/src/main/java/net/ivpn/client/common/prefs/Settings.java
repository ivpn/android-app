package net.ivpn.client.common.prefs;

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

import android.util.Log;

import com.wireguard.android.crypto.Keypair;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.BuildController;
import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.alarm.GlobalWireGuardAlarm;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.nightmode.NightMode;
import net.ivpn.client.common.utils.LogUtil;
import net.ivpn.client.ui.protocol.port.Port;
import net.ivpn.client.v2.serverlist.dialog.Filters;
import net.ivpn.client.vpn.Protocol;

import java.util.LinkedList;

import javax.inject.Inject;

@ApplicationScope
public class Settings {

    private static final String TAG = Settings.class.getSimpleName();
    private EncryptedSettingsPreference settingsPreference;
    private StickyPreference stickyPreference;
    private BuildController buildController;

    @Inject
    Settings(EncryptedSettingsPreference settingsPreference, StickyPreference stickyPreference,
             BuildController buildController) {
        this.settingsPreference = settingsPreference;
        this.stickyPreference = stickyPreference;
        this.buildController = buildController;
    }

    public void enableSentry(boolean value) {
        settingsPreference.enableSentry(value);
    }

    public void enableCustomDNS(boolean value) {
        settingsPreference.putSettingCustomDNS(value);
    }

    public void enableMultiHop(boolean value) {
        settingsPreference.putSettingMultiHop(value);
    }

    public void enableKillSwitch(boolean value) {
        settingsPreference.putSettingKillSwitch(value);
    }

    public void enableStartOnBoot(boolean value) {
        settingsPreference.putSettingStartOnBoot(value);
    }

    public void enableAdvancedKillSwitchDialog(boolean value) {
        settingsPreference.putSettingAdvancedKillSwitch(value);
    }

    public void enableAntiSurveillance(boolean value) {
        settingsPreference.putAntiSurveillance(value);
    }

    public void enableAntiSurveillanceHardcore(boolean value) {
        settingsPreference.putAntiSurveillanceHardcore(value);
    }

    public void enableNetworkRulesSettings(boolean value) {
        settingsPreference.putSettingsNetworkRules(value);
    }

    public void enableLocalBypass(boolean value) {
        settingsPreference.setBypassLocalSettings(value);
    }

    public void enableIPv6(boolean value) {
        settingsPreference.setIpv6Settings(value);
    }

    public void enableAllServerShown(boolean value) {
        settingsPreference.setIpv6ShowAllServers(value);
    }

    public boolean isAllServerShown() {
        return settingsPreference.getIpv6ShowAllServers();
    }

    public boolean isIPv6Enabled() {
        return settingsPreference.getIpv6Settings();
    }

    public void enableAutoUpdate(boolean value) {
        settingsPreference.putAutoUpdateSetting(value);
    }

    public boolean isAutoUpdateEnabled() {
        return settingsPreference.isAutoUpdateEnabled();
    }

    public String getNextVersion() {
        return settingsPreference.getNextVersion();
    }

    public void setNextVersion(String nextVersion) {
        settingsPreference.setNextVersion(nextVersion);
    }

    public boolean isMultiHopEnabled() {
        return settingsPreference.getSettingMultiHop();
    }

    public boolean isNetworkRulesEnabled() {
        return settingsPreference.getSettingNetworkRules();
    }

    public boolean isCustomDNSEnabled() {
        return settingsPreference.isCustomDNSEnabled();
    }

    public boolean isKillSwitchEnabled() {
        return settingsPreference.getSettingKillSwitch();
    }

    public boolean isStartOnBootEnabled() {
        return settingsPreference.getSettingStartOnBoot();
    }

    public boolean isSentryEnabled() {
        return settingsPreference.isSentryEnabled();
    }

    public boolean isAntiSurveillanceEnabled() {
        return settingsPreference.getIsAntiSurveillanceEnabled();
    }

    public boolean isLocalBypassEnabled() {
        return settingsPreference.getBypassLocalSettings();
    }

    public boolean isAntiSurveillanceHardcoreEnabled() {
        return settingsPreference.getIsAntiSurveillanceHardcoreEnabled();
    }

    public boolean isAdvancedKillSwitchDialogEnabled() {
        return settingsPreference.getIsAdvancedKillSwitchDialogEnabled();
    }

    public void setAntiTrackerDefaultDNS(String dns) {
        settingsPreference.putAntiSurveillanceDns(dns);
    }

    public void setAntiTrackerDefaultDNSMulti(String dns) {
        settingsPreference.putAntiSurveillanceDnsMulti(dns);
    }

    public void setAntiTrackerHardcoreDNS(String dns) {
        settingsPreference.putAntiSurveillanceHardcoreDns(dns);
    }

    public void setAntiTrackerHardcoreDNSMulti(String dns) {
        settingsPreference.putAntiSurveillanceHardcoreDnsMulti(dns);
    }

    public void setLastUsedIp(String ip) {
        settingsPreference.putLastUsedIp(ip);
    }

    public void setIpList(String ips) {
        settingsPreference.putIpList(ips);
    }

    public void setIPv6List(String ips) {
        settingsPreference.setIpv6List(ips);
    }

    public LinkedList<String> getIpList() {
        return settingsPreference.getIpList();
    }

    public LinkedList<String> getIpv6List() {
        return Mapper.ipListFrom(settingsPreference.getIpv6List());
    }

    public String getAntiTrackerDefaultDNS() {
        return settingsPreference.getAntiSurveillanceDns();
    }

    public String getAntiTrackerHardcoreDNS() {
        return settingsPreference.getAntiSurveillanceHardcoreDns();
    }

    public String getAntiTrackerDefaultDNSMulti() {
        return settingsPreference.getAntiSurveillanceDnsMulti();
    }

    public String getAntiTrackerHardcoreDNSMulti() {
        return settingsPreference.getAntiSurveillanceHardcoreDnsMulti();
    }

    public Port getOpenVpnPort() {
        String portJson = settingsPreference.getOpenvpnPort();
        return portJson.isEmpty() ? Port.UDP_2049 : Port.from(portJson);
    }

    public Port getWireGuardPort() {
        String portJson = settingsPreference.getWgPort();
        return portJson.isEmpty() ? Port.WG_UDP_2049 : Port.from(portJson);
    }

    public boolean isGenerationTimeExist() {
        return settingsPreference.isGenerationTimeExist();
    }

    public long getGenerationTime() {
        return settingsPreference.getGenerationTime();
    }

    public void putGenerationTime(long generationTime) {
        settingsPreference.putGenerationTime(generationTime);
    }

    public int getRegenerationPeriod() {
        return settingsPreference.getRegenerationPeriod();
    }

    public void putRegenerationPeriod(int regenerationPeriod) {
        settingsPreference.putRegenerationPeriod(regenerationPeriod);
    }

    public String getLastUsedIp() {
        return settingsPreference.getLastUsedIp();
    }

    public String getWireGuardPublicKey() {
        return settingsPreference.getSettingsWgPublicKey();
    }

    public String getWireGuardPrivateKey() {
        return settingsPreference.getSettingsWgPrivateKey();
    }

    public Keypair generateWireGuardKeys() {
        return new Keypair();
    }

    public void removeWireGuardKeys() {
        settingsPreference.setSettingsWgPrivateKey("");
        settingsPreference.setSettingsWgPublicKey("");
    }

    public void saveWireGuardKeypair(Keypair keypair) {
        settingsPreference.setSettingsWgPrivateKey(keypair.getPrivateKey());
        settingsPreference.setSettingsWgPublicKey(keypair.getPublicKey());
        settingsPreference.putGenerationTime(System.currentTimeMillis());
        GlobalWireGuardAlarm alarm = IVPNApplication.getApplication().appComponent.provideGlobalWireGuardAlarm();
        alarm.stop();
        alarm.start();
    }

    public void setWireGuardIpAddress(String ipAddress) {
        settingsPreference.setSettingsWgIpAddress(ipAddress);
    }

    public String getWireGuardIpAddress() {
        return settingsPreference.getSettingsWgIpAddress();
    }

    public void putSettingsNetworkRules(boolean value) {
        settingsPreference.putSettingsNetworkRules(value);
    }

    public void setOpenVPNPort(Port port) {
        settingsPreference.setOpenvpnPort(port.toJson());
    }

    public void setCustomDNSValue(String dns) {
        settingsPreference.setCustomDNSValue(dns);
    }

    public String getCustomDNSValue() {
        return settingsPreference.getCustomDNSValue();
    }

    public void setWgPort(Port port) {
        settingsPreference.setWgPort(port.toJson());
    }

    public void nextPort() {
        Protocol protocol = Protocol.valueOf(stickyPreference.getCurrentProtocol());
        if (protocol.equals(Protocol.OPENVPN)) {
            Port nextPort = getOpenVpnPort().next();
            Log.d(TAG, "nextPort: next port = ");
            setOpenVPNPort(nextPort);
        } else {
            Port nextPort = getWireGuardPort().next();
            Log.d(TAG, "nextPort: next port = ");
            setWgPort(nextPort);
        }
    }

    public String getDNS() {
        boolean isAntiSurveillanceEnabled = isAntiSurveillanceEnabled();
        boolean isAntiSurveillanceHardcoreEnabled = isAntiSurveillanceHardcoreEnabled();
        boolean isMultiHopEnabled = isMultiHopEnabled();

        String dns;
        String hardcoreDns;

        if (isMultiHopEnabled) {
            dns = getAntiTrackerDefaultDNSMulti();
            hardcoreDns = getAntiTrackerHardcoreDNSMulti();
        } else {
            dns = getAntiTrackerDefaultDNS();
            hardcoreDns = getAntiTrackerHardcoreDNS();
        }

        if (isAntiSurveillanceEnabled) {
            if (isAntiSurveillanceHardcoreEnabled) {
                return hardcoreDns;
            } else {
                return dns;
            }
        }

        boolean isCustomDNSEnabled = isCustomDNSEnabled();
        String customDNS = getCustomDNSValue();
        if (isCustomDNSEnabled && customDNS != null && !customDNS.isEmpty()) {
            return customDNS;
        }

        return null;
    }

    public void setNightMode(NightMode mode) {
        stickyPreference.setNightMode(mode.name());
    }

    public NightMode getNightMode() {
        String name = stickyPreference.getNightMode();

        if (name != null) {
            return NightMode.valueOf(name);
        }

        if (buildController.isSystemDefaultNightModeSupported()) {
            return NightMode.SYSTEM_DEFAULT;
        } else {
            return NightMode.BY_BATTERY_SAVER;
        }
    }

    public void setFilter(Filters filter) {
        settingsPreference.setFilter(filter.name());
    }

    public Filters getFilter() {
        String name = settingsPreference.getFilter();

        if (name != null) {
            return Filters.valueOf(name);
        }

        return Filters.COUNTRY;
    }
}