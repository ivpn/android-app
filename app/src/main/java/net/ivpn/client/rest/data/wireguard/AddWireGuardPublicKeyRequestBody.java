package net.ivpn.client.rest.data.wireguard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddWireGuardPublicKeyRequestBody {

    @SerializedName("session_token")
    @Expose
    private String sessionToken;
    @SerializedName("public_key")
    @Expose
    private String publicKey;
    @SerializedName("connected_public_key")
    @Expose
    private String connectedPublicKey;

    public AddWireGuardPublicKeyRequestBody(String sessionToken, String publicKey, String connectedPublicKey) {
        this.sessionToken = sessionToken;
        this.publicKey = publicKey;
        this.connectedPublicKey = connectedPublicKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}