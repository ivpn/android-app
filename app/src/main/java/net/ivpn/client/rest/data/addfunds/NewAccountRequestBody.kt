package net.ivpn.client.rest.data.addfunds

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewAccountRequestBody(
        @SerializedName("product_name")
        @Expose
        val product_name: String
)