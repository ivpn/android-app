package net.ivpn.client.rest.data.session;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionStatusRequestBody {
    @SerializedName("session_token")
    @Expose
    private String sessionToken;

    public SessionStatusRequestBody(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String toString() {
        return "SessionStatusRequestBody{" +
                "sessionToken='" + sessionToken + '\'' +
                '}';
    }
}
