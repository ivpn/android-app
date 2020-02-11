package net.ivpn.client.rest.data.session;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteSessionResponse {
    @SerializedName("status")
    @Expose
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
