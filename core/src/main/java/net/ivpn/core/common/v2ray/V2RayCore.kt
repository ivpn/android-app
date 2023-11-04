package net.ivpn.core.common.v2ray

class V2RayCore {

    companion object {
        fun start(): Error? {
            close()
//            var error: Error? = null

            val config = makeConfig()

            // Start V2Ray

            return null
        }

        fun close(): Error? {
//            var error: Error? = null

            // Stop V2Ray

            return null
        }

        fun makeConfig(): V2RayConfig? {
            return null
//            val settings = V2RaySettings()
//
//            return V2RayConfig.createQuick(
//                outboundIp = settings.outboundIp,
//                outboundPort = settings.outboundPort,
//                inboundIp = settings.inboundIp,
//                inboundPort = settings.inboundPort,
//                outboundUserId = settings.id,
//                tlsSrvName = settings.tlsSrvName
//            )
        }
    }
}
