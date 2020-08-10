package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class StartOnBootViewModel @Inject constructor(
        val settings: Settings,
        buildController: BuildController
) {
    val isStartOnBootEnabled = ObservableBoolean()
    val isStartOnBootSupported = ObservableBoolean()

    init {
        isStartOnBootSupported.set(buildController.isStartOnBootSupported)
    }

    fun onResume() {
        isStartOnBootEnabled.set(settings.isStartOnBootEnabled)
    }

    fun reset() {
        isStartOnBootEnabled.set(settings.isStartOnBootEnabled)
    }
}