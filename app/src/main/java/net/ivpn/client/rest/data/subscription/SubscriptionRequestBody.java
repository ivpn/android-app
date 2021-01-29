package net.ivpn.client.rest.data.subscription;

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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.BuildConfig;

public class SubscriptionRequestBody {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("password_confirmation")
    @Expose
    private String passwordConfirmation;
    @SerializedName("package_name")
    @Expose
    private String packageName;
    @SerializedName("subscription_id")
    @Expose
    private String subscriptionId;
    @SerializedName("purchase_token")
    @Expose
    private String purchaseToken;

    public SubscriptionRequestBody(String email, String password,
                                   String subscriptionId, String purchaseToken) {
        this.email = email;
        this.password = password;
        this.passwordConfirmation = password;
        this.packageName = BuildConfig.APPLICATION_ID;
        this.subscriptionId = subscriptionId;
        this.purchaseToken = purchaseToken;
    }

    public SubscriptionRequestBody(String username,
                                   String subscriptionId, String purchaseToken) {
        this.username = username;
        this.packageName = BuildConfig.APPLICATION_ID;
        this.subscriptionId = subscriptionId;
        this.purchaseToken = purchaseToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "SubscriptionRequestBody{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", passwordConfirmation='" + passwordConfirmation + '\'' +
                ", packageName='" + packageName + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                '}';
    }
}
