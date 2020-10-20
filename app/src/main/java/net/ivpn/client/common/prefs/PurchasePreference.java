package net.ivpn.client.common.prefs;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.SharedPreferences;

import javax.inject.Inject;

public class PurchasePreference {

    private static final String USER_EMAIL = "USER_EMAIL";
    private static final String PURCHASE_TOKEN = "PURCHASE_TOKEN";
    private static final String PURCHASE_PRODUCT_ID = "PURCHASE_PRODUCT_ID";
    private static final String USER_PASSWORD = "USER_PASSWORD";

    private Preference preference;

    @Inject
    public PurchasePreference(Preference preference) {
        this.preference = preference;
    }

    public void putUserEmail(String email) {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        sharedPreferences.edit()
                .putString(USER_EMAIL, email)
                .apply();
    }

    public void putPurchaseToken(String purchaseToken) {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        sharedPreferences.edit()
                .putString(PURCHASE_TOKEN, purchaseToken)
                .apply();
    }

    public void putPurchaseProductId(String productId) {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        sharedPreferences.edit()
                .putString(PURCHASE_PRODUCT_ID, productId)
                .apply();
    }

    public void putUserPassword(String password) {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        sharedPreferences.edit()
                .putString(USER_PASSWORD, password)
                .apply();
    }

    public String getUserPassword() {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        return sharedPreferences.getString(USER_PASSWORD, "");
    }

    public String getPurchaseProductId() {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        return sharedPreferences.getString(PURCHASE_PRODUCT_ID, "");
    }

    public String getPurchaseToken() {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        return sharedPreferences.getString(PURCHASE_TOKEN, "");
    }

    public String getUserEmail() {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        return sharedPreferences.getString(USER_EMAIL, "");
    }

    public void clear() {
        SharedPreferences sharedPreferences = preference.getPurchaseSharedPreferences();
        if (sharedPreferences == null) return;
        sharedPreferences.edit()
                .clear()
                .apply();
    }

}
