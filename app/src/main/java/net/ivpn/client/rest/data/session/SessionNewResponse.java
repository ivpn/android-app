package net.ivpn.client.rest.data.session;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.model.ServiceStatus;
import net.ivpn.client.rest.data.model.WireGuard;

public class SessionNewResponse {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("vpn_username")
    @Expose
    private String vpnUsername;
    @SerializedName("vpn_password")
    @Expose
    private String vpnPassword;
    @SerializedName("service_status")
    @Expose
    private ServiceStatus serviceStatus;
    @SerializedName("wireguard")
    @Expose
    private WireGuard wireGuard = null;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVpnUsername() {
        return vpnUsername;
    }

    public void setVpnUsername(String vpnUsername) {
        this.vpnUsername = vpnUsername;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public void setVpnPassword(String vpnPassword) {
        this.vpnPassword = vpnPassword;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public WireGuard getWireGuard() {
        return wireGuard;
    }

    public void setWireGuard(WireGuard wireGuard) {
        this.wireGuard = wireGuard;
    }

    @Override
    public String toString() {
        return "SessionNewResponse{" +
                "status=" + status +
                ", token='" + token + '\'' +
                ", vpnUsername='" + vpnUsername + '\'' +
                ", vpnPassword='" + vpnPassword + '\'' +
                ", serviceStatus=" + serviceStatus +
                ", wireGuard=" + wireGuard +
                '}';
    }
}