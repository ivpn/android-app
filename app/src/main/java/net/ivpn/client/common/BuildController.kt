package net.ivpn.client.common

import android.os.Build
import net.ivpn.client.common.dagger.ApplicationScope

@ApplicationScope
class BuildController {

    val isStartOnBootSupported  = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val isAlwaysOnVpnSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    init {
    }
}