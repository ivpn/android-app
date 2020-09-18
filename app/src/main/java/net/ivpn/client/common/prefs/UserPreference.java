package net.ivpn.client.common.prefs;

import android.content.SharedPreferences;

import javax.inject.Inject;

public class UserPreference {

    private static final String USER_LOGIN = "USER_LOGIN";
    private static final String USER_TRIAL = "USER_TRIAL";
    private static final String USER_AVAILABLE_UNTIL = "USER_AVAILABLE_UNTIL";
    private static final String USER_BETA_PRIVATE_EMAIL = "USER_BETA_PRIVATE_EMAIL";
    private static final String USER_MULTI_HOP = "USER_MULTI_HOP";
    private static final String PAYMENT_METHOD = "PAYMENT_METHOD";
    private static final String CURRENT_PLAN = "CURRENT_PLAN";
    private static final String IS_ACTIVE = "IS_ACTIVE";

    private static final String SESSION_TOKEN = "SESSION_TOKEN";
    private static final String SESSION_VPN_USERNAME = "SESSION_VPN_USERNAME";
    private static final String SESSION_VPN_PASSWORD = "SESSION_VPN_PASSWORD";

    private static final String BLANK_USERNAME = "BLANK_USERNAME";
    private static final String BLANK_USERNAME_GENERATED_DATE = "BLANK_USERNAME_GENERATED_DATE";

    private Preference preference;

    @Inject
    public UserPreference(Preference preference) {
        this.preference = preference;
    }

    public void putSessionToken(String sessionToken) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(SESSION_TOKEN, sessionToken)
                .apply();
    }

    public void putSessionUsername(String sessionVpnUsername) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(SESSION_VPN_USERNAME, sessionVpnUsername)
                .apply();
    }

    public void putBlankUsername(String blankUsername) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(BLANK_USERNAME, blankUsername)
                .apply();
    }

    public void putBlankUsernameGenerationDate(long timestamp) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putLong(BLANK_USERNAME_GENERATED_DATE, timestamp)
                .apply();
    }

    public void putSessionPassword(String sessionVpnPassword) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(SESSION_VPN_PASSWORD, sessionVpnPassword)
                .apply();
    }

    public void putCapabilityMultiHop(boolean isAvailable) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(USER_MULTI_HOP, isAvailable)
                .apply();
    }

    public void putPaymentMethod(String paymentMethod) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(PAYMENT_METHOD, paymentMethod)
                .apply();
    }

    public void putCurrentPlan(String accountType) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(CURRENT_PLAN, accountType)
                .apply();
    }

    public String getCurrentPlan() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(CURRENT_PLAN, "");
    }

    public boolean getCapabilityMultiHop() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getBoolean(USER_MULTI_HOP, false);
    }

    public String getPaymentMethod() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(PAYMENT_METHOD, "");
    }

    public String getUserLogin() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(USER_LOGIN, "");
    }

    public boolean getIsActive() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getBoolean(IS_ACTIVE, true);
    }

    public String getSessionVpnUsername() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(SESSION_VPN_USERNAME, "");
    }

    public String getBlankUsername() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(BLANK_USERNAME, "");
    }

    public long getBlankUsernameGeneratedDate() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getLong(BLANK_USERNAME_GENERATED_DATE, 0);
    }

    public String getSessionVpnPassword() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(SESSION_VPN_PASSWORD, "");
    }

    public String getSessionToken() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getString(SESSION_TOKEN, "");
    }

    public boolean isUserOnTrial() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getBoolean(USER_TRIAL, false);
    }

    public long getAvailableUntil() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getLong(USER_AVAILABLE_UNTIL, 0);
    }

    public boolean isUserOnPrivateEmailsBeta() {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        return sharedPreferences.getBoolean(USER_BETA_PRIVATE_EMAIL, false);
    }

    public void putUserLogin(String login) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putString(USER_LOGIN, login)
                .apply();
    }

    public void putIsUserOnTrial(boolean isOnTrial) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(USER_TRIAL, isOnTrial)
                .apply();
    }

    public void putAvailableUntil(long availableUntil) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putLong(USER_AVAILABLE_UNTIL, availableUntil)
                .apply();
    }

    public void putIsUserOnPrivateEmailBeta(boolean isOnPrivateEmailBeta) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(USER_BETA_PRIVATE_EMAIL, isOnPrivateEmailBeta)
                .apply();
    }

    public void putIsActive(boolean isActive) {
        SharedPreferences sharedPreferences = preference.getAccountSharedPreferences();
        sharedPreferences.edit()
                .putBoolean(IS_ACTIVE, isActive)
                .apply();
    }
}
