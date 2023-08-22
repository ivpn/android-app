package net.ivpn.core.rest.data;

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

import net.ivpn.core.rest.data.model.WireGuard;

import java.util.List;

public class LoginResponse {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("authenticated")
    @Expose
    private Boolean authenticated;
    @SerializedName("isActive")
    @Expose
    private Boolean isActive;
    @SerializedName("activeUtil")
    @Expose
    private Long activeUntil;
    @SerializedName("isRenewable")
    @Expose
    private Boolean isRenewable;
    @SerializedName("willAutoRebill")
    @Expose
    private Boolean willAutoRebill;
    @SerializedName("isOnFreeTrial")
    @Expose
    private Boolean isOnFreeTrial;
    @SerializedName("beta")
    @Expose
    private List<String> beta = null;
    @SerializedName("wireguard")
    @Expose
    private WireGuard wireGuard = null;

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(Long activeUntil) {
        this.activeUntil = activeUntil;
    }

    public Boolean getIsRenewable() {
        return isRenewable;
    }

    public void setIsRenewable(Boolean isRenewable) {
        this.isRenewable = isRenewable;
    }

    public Boolean getWillAutoRebill() {
        return willAutoRebill;
    }

    public void setWillAutoRebill(Boolean willAutoRebill) {
        this.willAutoRebill = willAutoRebill;
    }

    public Boolean getIsOnFreeTrial() {
        return isOnFreeTrial;
    }

    public void setIsOnFreeTrial(Boolean isOnFreeTrial) {
        this.isOnFreeTrial = isOnFreeTrial;
    }

    public List<String> getBeta() {
        return beta;
    }

    public void setBeta(List<String> beta) {
        this.beta = beta;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public WireGuard getWireGuard() {
        return wireGuard;
    }

    public void setWireGuard(WireGuard wireGuard) {
        this.wireGuard = wireGuard;
    }
}
