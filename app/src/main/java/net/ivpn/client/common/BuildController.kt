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
    }

    val isStartOnBootSupported  = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    val isAlwaysOnVpnSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isAdvancedKillSwitchModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    //TODO increase target build SDK and use this line:
    //Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    @kotlin.jvm.JvmField
    public var isSystemDefaultNightModeSupported = Build.VERSION.SDK_INT >= 29

    val isAntiTrackerEnabled = BuildConfig.BUILD_VARIANT == SITE || BuildConfig.BUILD_VARIANT == F_DROID
    val isSentrySupported = BuildConfig.BUILD_VARIANT != F_DROID
    val isUpdatesSupported = BuildConfig.BUILD_VARIANT == SITE

    init {
    }
}