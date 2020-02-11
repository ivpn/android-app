package net.ivpn.client.common.updater

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Update {
    @SerializedName("latestVersion")
    @Expose
    var latestVersion: String? = null
    @SerializedName("latestVersionCode")
    @Expose
    var latestVersionCode: Int? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("releaseNotes")
    @Expose
    var releaseNotes: List<String>? = null
}