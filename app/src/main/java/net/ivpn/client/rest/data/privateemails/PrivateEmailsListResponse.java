package net.ivpn.client.rest.data.privateemails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.privateemails.Email;

import java.util.List;

public class PrivateEmailsListResponse {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName(value="emails", alternate={"list"})
    @Expose
    private List<Email> emails = null;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    @Override
    public String toString() {
        return "PrivateEmailsListResponse{" +
                "status=" + status +
                ", emails=" + emails +
                '}';
    }
}