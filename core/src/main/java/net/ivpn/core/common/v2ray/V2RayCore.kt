package net.ivpn.core.common.v2ray

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Juraj Hilje.
 Copyright (c) 2023 IVPN Limited.

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

import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import libv2ray.V2RayVPNServiceSupportsSet
import java.io.File
import javax.inject.Inject

@ApplicationScope
class V2RayCore @Inject constructor(
    private val settings: Settings,
    private val context: Context
) {

    companion object {
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "26.26.26.1"
        private const val PRIVATE_VLAN4_ROUTER = "26.26.26.2"
        private const val PRIVATE_VLAN6_CLIENT = "da26:2626::1"
        private const val PRIVATE_VLAN6_ROUTER = "da26:2626::2"
        private const val TUN2SOCKS = "libtun2socks.so"
    }

    private val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(V2RayCallback(), Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
    private lateinit var process: Process
    private lateinit var mInterface: ParcelFileDescriptor

    fun start() {
        if (isRunning()) {
            return
        }

        val config = makeConfig()
        if (config != null) {
            v2rayPoint.configureFileContent = config.jsonString()
            v2rayPoint.domainName = "127.0.0.1:16661"
            try {
                v2rayPoint.runLoop(false)
//                runTun2socks()
            } catch (e: Exception) {
                println("Error system fucked up: " + e.message)
            }
        }
    }

    fun stop() {
        try {
            v2rayPoint.stopLoop()
        } catch (e: Exception) {
            println("Error system fucked up: " + e.message)
        }
    }

    fun isRunning(): Boolean {
        return v2rayPoint.isRunning
    }

    private fun makeConfig(): V2RayConfig? {
        val v2raySettings = settings.v2raySettings
        if (v2raySettings != null) {
            return V2RayConfig.createQuick(
                v2raySettings.outboundIp,
                v2raySettings.outboundPort,
                v2raySettings.inboundIp,
                v2raySettings.inboundPort,
                v2raySettings.id,
                v2raySettings.tlsSrvName
            )
        }
        return null
    }

//    private fun runTun2socks() {
//        val socksPort = 16661
//        val cmd = arrayListOf(
//            File(context.applicationInfo.nativeLibraryDir, TUN2SOCKS).absolutePath,
//            "--netif-ipaddr", PRIVATE_VLAN4_ROUTER,
//            "--netif-netmask", "255.255.255.252",
//            "--socks-server-addr", "127.0.0.1:${socksPort}",
//            "--tunmtu", VPN_MTU.toString(),
//            "--sock-path", File(context.filesDir, "sock_path").absolutePath,//File(applicationContext.filesDir, "sock_path").absolutePath,
//            "--enable-udprelay",
//            "--loglevel", "notice")
//
//        try {
//            val proBuilder = ProcessBuilder(cmd)
//            proBuilder.redirectErrorStream(true)
//            process = proBuilder
//                .directory(context.filesDir)
//                .start()
//            Thread(Runnable {
//                Log.d("INFO","$TUN2SOCKS check")
//                process.waitFor()
//                Log.d("INFO","$TUN2SOCKS exited")
//                    Log.d("INFO","$TUN2SOCKS restart")
//                    runTun2socks()
//            }).start()
//            Log.d("INFO", process.toString())
//
//            sendFd()
//        } catch (e: Exception) {
//            Log.d("INFO", e.toString())
//        }
//    }

//    @OptIn(DelicateCoroutinesApi::class)
//    private fun sendFd() {
//        val fd = mInterface.fileDescriptor
//        val path = File(context.filesDir, "sock_path").absolutePath
//        Log.d("INFO", path)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            var tries = 0
//            while (true) try {
//                Thread.sleep(50L shl tries)
//                Log.d("INFO", "sendFd tries: $tries")
//                LocalSocket().use { localSocket ->
//                    localSocket.connect(LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM))
//                    localSocket.setFileDescriptorsForSend(arrayOf(fd))
//                    localSocket.outputStream.write(42)
//                }
//                break
//            } catch (e: Exception) {
//                Log.d("INFO", e.toString())
//                if (tries > 5) break
//                tries += 1
//            }
//        }
//    }

    private class V2RayCallback : V2RayVPNServiceSupportsSet {
        override fun shutdown(): Long {
            println("V2RayCallback shutdown")
            return 0
        }

        override fun prepare(): Long {
            println("V2RayCallback prepare")
            return 0
        }

        override fun protect(l: Long): Boolean {
            println("V2RayCallback protect $l")
            return true
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            println("V2RayCallback onEmitStatus $l   $s")
            return 0
        }

        override fun setup(s: String): Long {
            println("V2RayCallback setup $s")
            return 0
        }
    }

}
