package net.ivpn.client.rest.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.model.WireGuard;

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
