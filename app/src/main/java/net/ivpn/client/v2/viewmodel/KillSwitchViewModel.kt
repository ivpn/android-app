package net.ivpn.client.v2.viewmodel

import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.vpn.GlobalBehaviorController
import javax.inject.Inject

@ApplicationScope
class KillSwitchViewModel @Inject constructor(
        private val settings: Settings,
        private val userPreference: UserPreference,
        private val buildController: BuildController,
        private val globalBehaviorController: GlobalBehaviorController
) : ViewModel() {

    val isEnabled = ObservableBoolean()
    var enableKillSwitch = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> tryEnable(value) }
    var touchListener = OnTouchListener { view, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            if (!isAuthenticated()) {
                navigator?.authenticate()
            } else if (!isActive()) {
                navigator?.subscribe()
            } else {
                view.performClick()
            }
        }

        true
    }

    var isAdvancedModeSupported: Boolean = buildController.isAdvancedKillSwitchModeSupported

    var navigator: KillSwitchNavigator? = null

    fun onResume() {
        isEnabled.set(isKillSwitchEnabled())
    }

    fun enableAdvancedKillSwitchDialog(value: Boolean) {
        settings.enableAdvancedKillSwitchDialog(value)
    }

    fun enable(value: Boolean) {
        isEnabled.set(value)
        settings.enableKillSwitch(value)
        if (value) {
            globalBehaviorController.enableKillSwitch()
        } else {
            globalBehaviorController.disableKillSwitch()
        }
    }

    fun reset() {
        isEnabled.set(isKillSwitchEnabled())
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