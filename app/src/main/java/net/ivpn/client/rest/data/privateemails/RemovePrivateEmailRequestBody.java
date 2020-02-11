package net.ivpn.client.rest.data.privateemails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RemovePrivateEmailRequestBody {

    @SerializedName("session_token")
    @Expose
    private String token;

    @SerializedName("email")
    @Expose
    private String email;

    public RemovePrivateEmailRequestBody(String token, String email) {
        this.token = token;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
