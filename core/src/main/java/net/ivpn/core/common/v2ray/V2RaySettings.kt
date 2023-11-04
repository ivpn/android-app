package net.ivpn.core.common.v2ray

data class V2RaySettings(
    var id: String = "",
    var outboundIp: String = "",
    var outboundPort: Int = 0,
    var inboundIp: String = "",
    var inboundPort: Int = 0,
    var dnsName: String = "",
    var wireguard: List<V2RayPort> = listOf()
) {
    val tlsSrvName: String
        get() = dnsName.replace("ivpn.net", "inet-telecom.com")

    val singleHopInboundPort: Int
        get() = wireguard.firstOrNull()?.port ?: 0
}

data class V2RayPort(
    val type: String,
    val port: Int
)
