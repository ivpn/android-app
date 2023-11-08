package net.ivpn.core.common.v2ray

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.ivpn.core.rest.data.model.Port

class V2RaySettings {

    @SerializedName("id")
    @Expose
    var id: String = ""

    @SerializedName("wireguard")
    @Expose
    lateinit var wireguard: List<Port>

    var outboundIp: String = ""
    var outboundPort: Int = 0
    var inboundIp: String = ""
    var inboundPort: Int = 0
    var dnsName: String = ""

    val tlsSrvName: String
        get() = dnsName.replace("ivpn.net", "inet-telecom.com")

    val singleHopInboundPort: Int
        get() = wireguard.firstOrNull()?.portNumber ?: 0

}

//data class V2RayPort(
//    val type: String,
//    val port: Int
//)
