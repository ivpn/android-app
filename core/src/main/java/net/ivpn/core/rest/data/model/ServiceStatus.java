package net.ivpn.core.rest.data.model;

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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceStatus {

    @SerializedName("current_plan")
    @Expose
    private String currentPlan;
    @SerializedName("payment_method")
    @Expose
    private String paymentMethod;
    @SerializedName("is_active")
    @Expose
    private Boolean isActive;
    @SerializedName("active_until")
    @Expose
    private long activeUntil;
    @SerializedName("is_renewable")
    @Expose
    private String isRenewable;
    @SerializedName("will_auto_rebill")
    @Expose
    private String willAutoRebill;
    @SerializedName("is_on_free_trial")
    @Expose
    private String isOnFreeTrial;
    @SerializedName("capabilities")
    @Expose
    private List<String> capabilities = null;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public long getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(long activeUtil) {
        this.activeUntil = activeUtil;
    }

    public String getIsRenewable() {
        return isRenewable;
    }

    public void setIsRenewable(String isRenewable) {
        this.isRenewable = isRenewable;
    }

    public String getWillAutoRebill() {
        return willAutoRebill;
    }

    public void setWillAutoRebill(String willAutoRebill) {
        this.willAutoRebill = willAutoRebill;
    }

    public String getIsOnFreeTrial() {
        return isOnFreeTrial;
    }

    public void setIsOnFreeTrial(String isOnFreeTrial) {
        this.isOnFreeTrial = isOnFreeTrial;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(String currentPlan) {
        this.currentPlan = currentPlan;
    }

    @Override
    public String toString() {
        return "ServiceStatus{" +
                "currentPlan='" + currentPlan + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", isActive='" + isActive + '\'' +
                ", activeUntil=" + activeUntil +
                ", isRenewable='" + isRenewable + '\'' +
                ", willAutoRebill='" + willAutoRebill + '\'' +
                ", isOnFreeTrial='" + isOnFreeTrial + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}