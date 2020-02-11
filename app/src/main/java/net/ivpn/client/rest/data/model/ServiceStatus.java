package net.ivpn.client.rest.data.model;

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