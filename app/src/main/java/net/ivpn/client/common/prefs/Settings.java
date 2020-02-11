package net.ivpn.client.common.prefs;

import android.util.Log;

import com.wireguard.android.crypto.Keypair;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.alarm.GlobalWireGuardAlarm;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.utils.LogUtil;
import net.ivpn.client.ui.protocol.port.Port;
import net.ivpn.client.vpn.Protocol;

import java.io.Serializable;
import java.util.LinkedList;

import javax.inject.Inject;

@ApplicationScope
public class Settings {

    private static final String TAG = Settings.class.getSimpleName();
    private SettingsPreference settingsPreference;
    private StickyPreference stickyPreference;

    @Inject
    Settings(SettingsPreference settingsPreference, StickyPreference stickyPreference) {
        this.settingsPreference = settingsPreference;
        this.stickyPreference = stickyPreference;
    }

    public void enableLogging(boolean value) {
        boolean isLoggingEnabled = isLoggingEnabled();
        if (isLoggingEnabled != value) {
            settingsPreference.putSettingLogging(value);
            LogUtil.enableLogging(value);
        }
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

    public void enableFastestServerSetting(boolean value) {
        settingsPreference.putSettingFastestServer(value);
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

    public boolean isLoggingEnabled() {
        return settingsPreference.getSettingLogging();
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

    public boolean isNewForPrivateEmails() {
        return settingsPreference.getIsNewForPrivateEmails();
    }

    public boolean isSentryEnabled() {
        return settingsPreference.isSentryEnabled();
    }

    public boolean isAntiSurveillanceEnabled() {
        return settingsPreference.getIsAntiSurveillanceEnabled();
    }

    public boolean isAntiSurveillanceHardcoreEnabled() {
        return settingsPreference.getIsAntiSurveillanceHardcoreEnabled();
    }

    public boolean isFastestServerEnabled() {
        return settingsPreference.getSettingFastestServer();
    }

    public boolean isAdvancedKillSwitchDialogEnabled() {
        return settingsPreference.getIsAdvancedKillSwitchDialogEnabled();
    }

    public void setIsNewForPrivateEmails(boolean isNewForPrivateEmails) {
        settingsPreference.putIsNewForPrivateEmails(isNewForPrivateEmails);
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

    public LinkedList<String> getIpList() {
        return settingsPreference.getIpList();
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
}