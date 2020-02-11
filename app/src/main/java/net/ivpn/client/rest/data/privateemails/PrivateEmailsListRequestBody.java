package net.ivpn.client.rest.data.privateemails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrivateEmailsListRequestBody {
    @SerializedName("session_token")
    @Expose
    private String token;

    public PrivateEmailsListRequestBody(String token) {
        this.token = token;
    }
}