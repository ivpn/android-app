package net.ivpn.client.rest.data.addfunds

import com.google.gson.annotations.SerializedName

data class AddFundsRequestBody (
        @SerializedName("session_token") val sessionToken : String,
        @SerializedName("sku_id") val skuId : String,
        @SerializedName("purchase_token") val purchaseToken : String
)