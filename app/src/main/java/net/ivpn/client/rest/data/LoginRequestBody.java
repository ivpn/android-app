package net.ivpn.client.rest.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginRequestBody {

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("device_os")
    @Expose
    private String deviceOS;

    @SerializedName("device_type")
    @Expose
    private String deviceType;

    @SerializedName("wireguard_public_key")
    @Expose
    private String wgPublicKey;

    @SerializedName("wireguard_comment")
    @Expose
    private String wgComment;

    public LoginRequestBody(String username, String password, boolean isTablet, String wgPublicKey) {
        this.username = username;
        this.password = password;
        this.deviceType = isTablet ? "tablet" : "phone";
        this.deviceOS = "android";
        this.wgPublicKey = wgPublicKey;
        this.wgComment = wgPublicKey != null ? "IVPN Client for Android" : null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
