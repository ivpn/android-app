package net.ivpn.client.rest.data.subscription

import com.google.gson.annotations.SerializedName

data class NewAccountResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("account_id") val accountId: String
)