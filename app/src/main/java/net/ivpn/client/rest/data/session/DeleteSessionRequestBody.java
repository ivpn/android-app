package net.ivpn.client.rest.data.session;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteSessionRequestBody {
    @SerializedName("session_token")
    @Expose
    private String sessionToken;

    public DeleteSessionRequestBody(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
