package net.ivpn.client.rest.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WireGuard {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("ip_address")
    @Expose
    private String ipAddress;

    public Integer getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "WireGuard{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}