package net.ivpn.client.rest.data.addfunds

import com.google.gson.annotations.SerializedName

data class InitialPaymentRequestBody(
    @SerializedName("account_id") val account_id : String,
    @SerializedName("sku_id") val sku_id : String,
    @SerializedName("purchase_token") val purchase_token : String
)