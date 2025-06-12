package net.ivpn.core.vpn.controller

import libV2ray.CoreCallbackHandler
import libV2ray.CoreController
import libV2ray.LibV2ray

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

object V2rayController : CoreCallbackHandler {
    private val controller: CoreController by lazy {
        LibV2ray.newCoreController(this)
    }



    fun start() {
        val config = """
           {
    "log": {
        "loglevel": "debug"
    },
    "inbounds": [
        {
            "tag": "vpn",
            "port": "16661",
            "listen": "127.0.0.1",
            "protocol": "dokodemo-door",
            "settings": {
                "address": "146.70.146.226",
                "port": 15351,
                "network": "udp"
            }
        }
    ],
    "outbounds": [
        {
            "tag": "proxy",
            "protocol": "vmess",
            "settings": {
                "vnext": [
                    {
                        "address": "146.70.146.226",
                        "port": 2049,
                        "users": [
                            {
                                "id": "27de860d-5601-412d-8b71-baa048a94b98",
                                "alterId": 0,
                                "security": "none"
                            }
                        ]
                    }
                ]
            },
            "streamSettings": {
                "network": "tcp",
                "security": "",
                "tcpSettings": {
                    "header": {
                        "type": "http",
                        "request": {
                            "version": "1.1",
                            "method": "GET",
                            "path": ["/"],
                            "headers": {
                                "Host": ["www.inet-telecom.com"],
                                "User-Agent": [
                                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36",
                                    "Mozilla/5.0 (iPhone; CPU iPhone OS 10_0_2 like Mac OS X) AppleWebKit/601.1 (KHTML, like Gecko) CriOS/53.0.2785.109 Mobile/14A456 Safari/601.1.46"
                                ],
                                "Accept-Encoding": ["gzip, deflate"],
                                "Connection": ["keep-alive"],
                                "Pragma": "no-cache"
                            }
                        }
                    }
                }
            }
        }
    ]
}
        """.trimIndent()
        controller.startLoop(config)
    }


    fun stop() {
        if (controller.isRunning) {
            controller.stopLoop()
        }
    }


    override fun onEmitStatus(p0: Long, p1: String?): Long {
        return 0
    }

    override fun shutdown(): Long {
        return 0
    }

    override fun startup(): Long {
        return 0
    }
}