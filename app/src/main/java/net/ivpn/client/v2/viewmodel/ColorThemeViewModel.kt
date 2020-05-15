package net.ivpn.client.v2.viewmodel

import android.widget.RadioGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

class ColorThemeViewModel @Inject constructor(
        buildController: BuildController,
        private val settings: Settings
) : ViewModel() {

    var themeListener =  RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int -> onCheckedChanged(checkedId)
    }

    val isSystemDefaultNightModeSupported = ObservableBoolean()
    val nightMode = ObservableField<NightMode>()

    private val selectedNightMode = ObservableField<NightMode>()

    var navigator: ColorThemeNavigator? = null

    init {
        isSystemDefaultNightModeSupported.set(buildController.isSystemDefaultNightModeSupported)
    }

    fun onResume() {
        selectedNightMode.set(settings.nightMode)
        nightMode.set(settings.nightMode)
    }

    fun applyMode() {
        nightMode.set(selectedNightMode.get())
        settings.nightMode = nightMode.get()
        navigator?.onNightModeChanged(nightMode.get())
    }

    private fun onCheckedChanged(checkedId: Int) {
        val nightMode = NightMode.getById(checkedId)
        if (nightMode == this.selectedNightMode.get()) {
            return
        }

        this.selectedNightMode.set(nightMode)
    }

    interface ColorThemeNavigator {
        fun onNightModeChanged(mode: NightMode?)
    }

}