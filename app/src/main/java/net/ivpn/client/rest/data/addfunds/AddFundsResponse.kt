package net.ivpn.client.rest.data.addfunds

import com.google.gson.annotations.SerializedName
import net.ivpn.client.rest.data.model.ServiceStatus

data class AddFundsResponse(
        @SerializedName("status") val status : Int,
        @SerializedName("service_status") val serviceStatus: ServiceStatus
)