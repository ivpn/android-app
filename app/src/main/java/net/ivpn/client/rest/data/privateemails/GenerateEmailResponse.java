package net.ivpn.client.rest.data.privateemails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenerateEmailResponse {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("generated")
    @Expose
    private String generated;
    @SerializedName("forwarded-to")
    @Expose
    private String forwardedTo;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGenerated() {
        return generated;
    }

    public void setGenerated(String generated) {
        this.generated = generated;
    }

    public String getForwardedTo() {
        return forwardedTo;
    }

    public void setForwardedTo(String forwardedTo) {
        this.forwardedTo = forwardedTo;
    }

    @Override
    public String toString() {
        return "GenerateEmailResponse{" +
                "status=" + status +
                ", generated='" + generated + '\'' +
                ", forwardedTo='" + forwardedTo + '\'' +
                '}';
    }
}