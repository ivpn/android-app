package net.ivpn.client.common.prefs;

import android.content.SharedPreferences;
import android.util.Log;

import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.vpn.model.NetworkState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.inject.Inject;

@ApplicationScope
public class SettingsPreference {

    private static final String SETTINGS_FASTEST_SERVER = "SETTINGS_FASTEST_SERVER";
    private static final String SETTINGS_LOGGING = "SETTINGS_LOGGING";
    private static final String SETTINGS_SENTRY = "SETTINGS_SENTRY";
    private static final String SETTINGS_MULTI_HOP = "SETTINGS_MULTI_HOP";
    private static final String SETTINGS_KILL_SWITCH = "SETTINGS_KILL_SWITCH";
    private static final String SETTINGS_ADVANCED_KILL_SWITCH_DIALOG = "SETTINGS_ADVANCED_KILL_SWITCH_DIALOG";
    private static final String SETTINGS_START_ON_BOOT = "SETTINGS_START_ON_BOOT";
    private static final String SETTINGS_IS_NEW_FOR_PRIVATE_EMAILS = "SETTINGS_IS_NEW_FOR_PRIVATE_EMAILS";
    private static final String SETTINGS_NETWORK_RULES = "SETTINGS_NETWORK_RULES";
    private static final String SETTINGS_WG_PRIVATE_KEY = "SETTINGS_WG_PRIVATE_KEY";
    private static final String SETTINGS_WG_PUBLIC_KEY = "SETTINGS_WG_PUBLIC_KEY";
    private static final String SETTINGS_WG_IP_ADDRESS = "SETTINGS_WG_IP_ADDRESS";
    private static final String SETTINGS_CUSTOM_DNS = "SETTINGS_CUSTOM_DNS";
    private static final String SETTINGS_ANTI_SURVEILLANCE = "SETTINGS_ANTI_SURVEILLANCE";
    private static final String SETTINGS_ANTI_SURVEILLANCE_HARDCORE = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE";
    private static final String SETTINGS_ANTI_SURVEILLANCE_DNS = "SETTINGS_ANTI_SURVEILLANCE_DNS";
    private static final String SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS";
    private static final String SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI = "SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI";
    private static final String SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI = "SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI";
    private static final String SETTINGS_CUSTOM_DNS_VALUE = "SETTINGS_CUSTOM_DNS_VALUE";
    private static final String SETTINGS_AUTO_UPDATE = "SETTINGS_AUTO_UPDATE";
    private static final String SETTINGS_NEXT_VERSION = "SETTINGS_NEXT_VERSION";
    private static final String OV_PORT = "OV_PORT";
    private static final String WG_PORT = "WG_PORT";
    private static final String WIREGUARD_KEY_GENERATION_TIME = "WIREGUARD_KEY_GENERATION_TIME";
    private static final String WIREGUARD_KEY_REGENERATION_PERIOD = "WIREGUARD_KEY_REGENERATION_PERIOD";
    private static final String RULE_CONNECT_TO_VPN = "RULE_CONNECT_TO_VPN";
    private static final String RULE_DISCONNECT_FROM_VPN = "RULE_DISCONNECT_FROM_VPN";
    private static final String RULE_ENABLE_KILL_SWITCH = "RULE_ENABLE_KILL_SWITCH";
    private static final String RULE_DISABLE_KILL_SWITCH = "RULE_DISABLE_KILL_SWITCH";
    private static final String IP_LIST = "IP_LIST";
    private static final String LAST_USED_IP = "LAST_USED_IP";

    private Preference preference;

    @Inject
    public SettingsPreference(Preference preference) {
        this.preference = preference;
    }

    public boolean getSettingLogging() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_LOGGING, false);
    }

    public boolean getSettingMultiHop() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_MULTI_HOP, false);
    }

    public boolean getSettingFastestServer() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_FASTEST_SERVER, true);
    }

    public boolean getSettingKillSwitch() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_KILL_SWITCH, false);
    }

    public boolean getSettingStartOnBoot() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_START_ON_BOOT, false);
    }

    public boolean getIsNewForPrivateEmails() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_IS_NEW_FOR_PRIVATE_EMAILS, true);
    }

    public boolean getIsAntiSurveillanceEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_ANTI_SURVEILLANCE, false);
    }

    public boolean getIsAntiSurveillanceHardcoreEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, false);
    }

    public String getAntiSurveillanceDns() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_DNS, "10.0.254.2");
    }

    public String getAntiSurveillanceHardcoreDns() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS, "10.0.254.3");
    }

    public String getAntiSurveillanceDnsMulti() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI, "10.0.254.102");
    }

    public String getAntiSurveillanceHardcoreDnsMulti() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI, "10.0.254.103");
    }

    public boolean getIsAdvancedKillSwitchDialogEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, true);
    }

    public boolean getSettingNetworkRules() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_NETWORK_RULES, false);
    }

    public boolean isCustomDNSEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_CUSTOM_DNS, false);
    }

    public boolean isNetworkRuleSettingsExist() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.contains(SETTINGS_NETWORK_RULES);
    }

    public boolean isGenerationTimeExist() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.contains(WIREGUARD_KEY_GENERATION_TIME);
    }

    public long getGenerationTime() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getLong(WIREGUARD_KEY_GENERATION_TIME, 0);
    }

    public void putGenerationTime(long generationTime) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putLong(WIREGUARD_KEY_GENERATION_TIME, generationTime)
                .apply();
    }

    public int getRegenerationPeriod() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getInt(WIREGUARD_KEY_REGENERATION_PERIOD, 7);
    }

    public void putRegenerationPeriod(int regenerationPeriod) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putInt(WIREGUARD_KEY_REGENERATION_PERIOD, regenerationPeriod)
                .apply();
    }

    public void putSettingLogging(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_LOGGING, value)
                .apply();
    }

    public void putIsNewForPrivateEmails(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_IS_NEW_FOR_PRIVATE_EMAILS, value)
                .apply();
    }

    public void putSettingMultiHop(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_MULTI_HOP, value)
                .apply();
    }

    public void putSettingCustomDNS(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_CUSTOM_DNS, value)
                .apply();
    }

    public void putSettingFastestServer(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_FASTEST_SERVER, value)
                .apply();
    }

    public void putSettingKillSwitch(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_KILL_SWITCH, value)
                .apply();
    }

    public void putSettingStartOnBoot(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_START_ON_BOOT, value)
                .apply();
    }

    public void putAntiSurveillance(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ANTI_SURVEILLANCE, value)
                .apply();
    }

    public void putAntiSurveillanceHardcore(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ANTI_SURVEILLANCE_HARDCORE, value)
                .apply();
    }

    public void putAntiSurveillanceDns(String value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_DNS, value)
                .apply();
    }

    public void putAntiSurveillanceHardcoreDns(String value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS, value)
                .apply();
    }

    public void putAntiSurveillanceDnsMulti(String value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_DNS_MULTI, value)
                .apply();
    }

    public void putAntiSurveillanceHardcoreDnsMulti(String value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_ANTI_SURVEILLANCE_HARDCORE_DNS_MULTI, value)
                .apply();
    }

    public void putSettingAdvancedKillSwitch(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_ADVANCED_KILL_SWITCH_DIALOG, value)
                .apply();
    }

    public void putAutoUpdateSetting(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_AUTO_UPDATE, value)
                .apply();
    }

    public void putSettingsNetworkRules(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_NETWORK_RULES, value)
                .apply();
    }

    public void setOpenvpnPort(String json) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(OV_PORT, json)
                .apply();
    }

    public void setCustomDNSValue(String dns) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_CUSTOM_DNS_VALUE, dns)
                .apply();
    }

    public String getCustomDNSValue() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_CUSTOM_DNS_VALUE, "");
    }

    public String getOpenvpnPort() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(OV_PORT, "");
    }

    public void setWgPort(String json) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(WG_PORT, json)
                .apply();
    }

    public String getWgPort() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(WG_PORT, "");
    }

    public String getSettingsWgPrivateKey() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_WG_PRIVATE_KEY, "");
    }

    public void setSettingsWgPrivateKey(String privateKey) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_WG_PRIVATE_KEY, privateKey)
                .apply();
    }

    public String getSettingsWgPublicKey() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_WG_PUBLIC_KEY, "");
    }

    public void setSettingsWgPublicKey(String publicKey) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_WG_PUBLIC_KEY, publicKey)
                .apply();
    }

    public void setSettingsWgIpAddress(String ipAddress) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_WG_IP_ADDRESS, ipAddress)
                .apply();
    }

    public String getSettingsWgIpAddress() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_WG_IP_ADDRESS, "");
    }

    public boolean isAutoUpdateEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_AUTO_UPDATE, true);
    }

    public boolean getRuleConnectToVpn() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getBoolean(RULE_CONNECT_TO_VPN, true);
    }

    public boolean getRuleDisconnectFromVpn() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getBoolean(RULE_DISCONNECT_FROM_VPN, true);
    }

    public boolean getRuleEnableKillSwitch() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getBoolean(RULE_ENABLE_KILL_SWITCH, false);
    }

    public boolean getRuleDisableKillSwitch() {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        return sharedPreferences.getBoolean(RULE_DISABLE_KILL_SWITCH, false);
    }

    public void putRuleConnectToVpn(boolean value) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(RULE_CONNECT_TO_VPN, value)
                .apply();
    }

    public void putRuleDisconnectFromVpn(boolean value) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(RULE_DISCONNECT_FROM_VPN, value)
                .apply();
    }

    public void putRuleEnableKillSwitch(boolean value) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(RULE_ENABLE_KILL_SWITCH, value)
                .apply();
    }

    public void putRuleDisableKillSwitch(boolean value) {
        SharedPreferences sharedPreferences = preference.getNetworkRulesSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(RULE_DISABLE_KILL_SWITCH, value)
                .apply();
    }

    public String getNextVersion() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(SETTINGS_NEXT_VERSION, "{}");
    }

    public void setNextVersion(String nextVersion) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(SETTINGS_NEXT_VERSION, nextVersion)
                .apply();
    }

    public void putIpList(String ips) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(IP_LIST, ips)
                .apply();
    }

    public LinkedList<String> getIpList() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return Mapper.ipListFrom(sharedPreferences.getString(IP_LIST, null));
    }

    public void putLastUsedIp(String ip) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putString(LAST_USED_IP, ip)
                .apply();
    }

    public String getLastUsedIp() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getString(LAST_USED_IP, null);
    }

    public void enableSentry(boolean value) {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(SETTINGS_SENTRY, value)
                .apply();
    }

    public boolean isSentryEnabled() {
        SharedPreferences sharedPreferences = preference.getSettingsSharedPreferences();
        return sharedPreferences.getBoolean(SETTINGS_SENTRY, true);
    }
}