package net.ivpn.client.rest.data.privateemails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdatePrivateEmailRequestBody {
    @SerializedName("session_token")
    @Expose
    private String token;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("note")
    @Expose
    private String note;

    public UpdatePrivateEmailRequestBody(String token, String email, String note) {
        this.token = token;
        this.email = email;
        this.note = note;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}