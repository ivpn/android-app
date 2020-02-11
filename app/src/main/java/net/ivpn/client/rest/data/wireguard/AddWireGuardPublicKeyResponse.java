package net.ivpn.client.rest.data.wireguard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddWireGuardPublicKeyResponse {

    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("ip_address")
    @Expose
    private String ipAddress;
    @SerializedName("message")
    @Expose
    private String message;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AddWireGuardPublicKeyResponse{" +
                "result='" + result + '\'' +
                ", status=" + status +
                ", ipAddress='" + ipAddress + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
