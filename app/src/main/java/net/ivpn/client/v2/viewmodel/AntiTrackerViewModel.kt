package net.ivpn.client.v2.viewmodel

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

@ApplicationScope
class AntiTrackerViewModel @Inject constructor(
        private val buildController: BuildController,
        private val settings: Settings
) : ViewModel() {

    val isAntiTrackerSupported = ObservableBoolean()
    val isAntiSurveillanceEnabled = ObservableBoolean()
    val isHardcoreModeEnabled = ObservableBoolean()
    val isHardcoreModeUIEnabled = ObservableBoolean()

    var enableAntiSurveillance = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAntiSurveillance(value) }
    var enableHardcoreMode = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableHardcoreMode(value) }

    init {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())
    }

    fun reset() {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())
    }

    private fun getAntiTrackerSupport(): Boolean {
        return buildController.isAntiTrackerSupported
    }

    private fun enableAntiSurveillance(value: Boolean) {
        isAntiSurveillanceEnabled.set(value)
        settings.enableAntiSurveillance(value)
        isHardcoreModeUIEnabled.set(value)
        if (!value) {
            isHardcoreModeEnabled.set(false)
            settings.enableAntiSurveillanceHardcore(false)
        }
    }

    private fun enableHardcoreMode(value: Boolean) {
        isHardcoreModeEnabled.set(value)
        settings.enableAntiSurveillanceHardcore(value)
    }
}