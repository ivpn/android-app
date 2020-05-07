package net.ivpn.client.v2.viewmodel

import android.os.Build
import android.widget.RadioGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.common.prefs.Settings
import javax.inject.Inject

class ThemeViewModel @Inject constructor(
        private val buildController: BuildController,
        private val settings: Settings
) : ViewModel() {

    var themeListener =  RadioGroup.OnCheckedChangeListener {
        _: RadioGroup?, checkedId: Int -> onCheckedChanged(checkedId)
    }

    val isSystemDefaultNightModeSupported = ObservableBoolean()
    val nightMode = ObservableField<NightMode>()

    private val navigator: ColorThemeNavigator? = null

    init {
        isSystemDefaultNightModeSupported.set(buildController.isSystemDefaultNightModeSupported)
        nightMode.set(settings.getNightMode())
    }

    fun applyMode() {
        settings.setNightMode(nightMode.get())
        navigator?.onNightModeChanged(nightMode.get())
    }

    fun onCheckedChanged(checkedId: Int) {
        val nightMode = NightMode.getById(checkedId)
        if (nightMode == this.nightMode.get()) {
            return
        }

        this.nightMode.set(nightMode)
    }

    interface ColorThemeNavigator {
        fun onNightModeChanged(mode: NightMode?)

        fun onNightModeCancelClicked()
    }

}