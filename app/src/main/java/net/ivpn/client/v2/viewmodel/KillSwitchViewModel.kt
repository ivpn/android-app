package net.ivpn.client.v2.viewmodel

import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import javax.inject.Inject

class KillSwitchViewModel @Inject constructor(
        private val settings: Settings,
        private val userPreference: UserPreference,
        private val buildController: BuildController
) : ViewModel() {

    val isEnabled = ObservableBoolean()
    var enableKillSwitch = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> tryEnable(value) }
    var touchListener = OnTouchListener { _, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            if (!isAuthenticated()) {
                navigator?.authenticate()
            } else if (!isActive()) {
                navigator?.subscribe()
            }

            return@OnTouchListener true
        }

        false
    }

    var isAdvancedModeSupported: Boolean = buildController.isAdvancedKillSwitchModeSupported

    private var navigator: KillSwitchNavigator? = null

    fun onResume() {
        isEnabled.set(isKillSwitchEnabled())
    }

    fun enableAdvancedKillSwitchDialog(value: Boolean) {
        settings.enableAdvancedKillSwitchDialog(value)
    }

    fun enable(value: Boolean) {
        isEnabled.set(value)
        settings.enableKillSwitch(value)
    }

    private fun tryEnable(value: Boolean) {
        navigator?.tryEnableKillSwitch(value, isAdvancedKillSwitchDialogEnabled())
    }

    private fun isAdvancedKillSwitchDialogEnabled(): Boolean {
        return settings.isAdvancedKillSwitchDialogEnabled
    }

    private fun isKillSwitchEnabled(): Boolean {
        return settings.isKillSwitchEnabled
    }

    private fun isAuthenticated() : Boolean {
        val token: String = userPreference.sessionToken
        return token.isNotEmpty()
    }

    private fun isActive(): Boolean {
        return userPreference.isActive
    }

    interface KillSwitchNavigator {
        fun subscribe()
        fun authenticate()
        fun tryEnableKillSwitch(state: Boolean, advancedKillSwitchState: Boolean)
    }
}