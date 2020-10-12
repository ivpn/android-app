package net.ivpn.client.common

import android.os.Build
import net.ivpn.client.BuildConfig
import net.ivpn.client.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class BuildController @Inject constructor() {

    companion object {
        const val SITE = "site"
        const val F_DROID = "fdroid"
        const val STORE = "store"
    }

    val isStartOnBootSupported = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val isAlwaysOnVpnSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isAdvancedKillSwitchModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    //TODO increase target build SDK and use this line:
    //Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    @kotlin.jvm.JvmField
    var isSystemDefaultNightModeSupported = Build.VERSION.SDK_INT >= 29

    val isAntiTrackerSupported = BuildConfig.BUILD_VARIANT == SITE || BuildConfig.BUILD_VARIANT == F_DROID
    val isSentrySupported = BuildConfig.BUILD_VARIANT != F_DROID
    val isUpdatesSupported = BuildConfig.BUILD_VARIANT == SITE
    val isIAPEnabled = BuildConfig.BUILD_VARIANT == STORE

    init {
    }
}