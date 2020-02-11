package net.ivpn.client.rest.data.session;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.ivpn.client.rest.data.model.ServiceStatus;

public class SessionStatusResponse {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("service_status")
    @Expose
    private ServiceStatus serviceStatus;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    @Override
    public String toString() {
        return "SessionStatusResponse{" +
                "status=" + status +
                ", serviceStatus=" + serviceStatus +
                '}';
    }
}
