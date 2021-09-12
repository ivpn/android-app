package net.ivpn.core.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 
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

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.BuildController
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.common.utils.StringUtil
import net.ivpn.core.v2.connect.createSession.ConnectionState
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.vpn.controller.DefaultVPNStateListener
import net.ivpn.core.vpn.controller.VpnBehaviorController
import net.ivpn.core.vpn.controller.VpnStateListener
import javax.inject.Inject

@ApplicationScope
class AntiTrackerViewModel @Inject constructor(
        private val buildController: BuildController,
        private val vpnBehaviorController: VpnBehaviorController,
        private val settings: Settings
) : ViewModel() {

    val isAntiTrackerSupported = ObservableBoolean()
    val isAntiSurveillanceEnabled = ObservableBoolean()
    val isHardcoreModeEnabled = ObservableBoolean()
    val isHardcoreModeUIEnabled = ObservableBoolean()

    val antiTrackerDescription = ObservableField<String>()

    val state = ObservableField<AntiTrackerState>()

    var connectionState: ConnectionState? = null

    var enableAntiSurveillance = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableAntiSurveillance(value) }
    var enableHardcoreMode = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableHardcoreMode(value) }

    init {
        initStates()
        vpnBehaviorController.addVpnStateListener(getVPNStateListener())
    }

    fun reset() {
        initStates()
    }

    private fun initStates() {
        isAntiTrackerSupported.set(getAntiTrackerSupport())
        isAntiSurveillanceEnabled.set(settings.isAntiSurveillanceEnabled)
        isHardcoreModeEnabled.set(settings.isAntiSurveillanceHardcoreEnabled)
        isHardcoreModeUIEnabled.set(isAntiSurveillanceEnabled.get())

        getAntiTrackerState()
        getAntiTrackerDescriptionValue()
    }

    private fun getAntiTrackerSupport(): Boolean {
        return IVPNApplication.config.isAntiTrackerSupported
    }

    private fun enableAntiSurveillance(value: Boolean) {
        isAntiSurveillanceEnabled.set(value)
        settings.isAntiSurveillanceEnabled = value
        isHardcoreModeUIEnabled.set(value)
        getAntiTrackerState()
        getAntiTrackerDescriptionValue()
    }

    private fun enableHardcoreMode(value: Boolean) {
        isHardcoreModeEnabled.set(value)
        settings.isAntiSurveillanceHardcoreEnabled = value
        getAntiTrackerState()
    }

    private fun getAntiTrackerState() {
        if (!isAntiSurveillanceEnabled.get()) {
            state.set(AntiTrackerState.DISABLED)
            return
        }

        state.set(if(isHardcoreModeEnabled.get()) AntiTrackerState.HARDCORE else AntiTrackerState.NORMAL)
    }

    private fun getAntiTrackerDescriptionValue() {
        val context = IVPNApplication.application
        if (isAntiSurveillanceEnabled.get()) {
            connectionState?.let {
                if (it == ConnectionState.CONNECTED) {
                    antiTrackerDescription.set(context.getString(R.string.anti_tracker_description_state_enabled))
                } else {
                    antiTrackerDescription.set(context.getString(R.string.anti_tracker_description_state_not_active))
                }
            } ?: run {
                antiTrackerDescription.set(context.getString(R.string.anti_tracker_description_state_not_active))
            }
        } else {
            antiTrackerDescription.set(context.getString(R.string.anti_tracker_description_state_disabled))
        }
    }

    private fun getVPNStateListener(): VpnStateListener {
        return object : DefaultVPNStateListener() {
            override fun onConnectionStateChanged(state: ConnectionState?) {
                if (state == null) {
                    return
                }
                connectionState = state
                getAntiTrackerDescriptionValue()
            }
        }
    }

    enum class AntiTrackerState {
        DISABLED,
        NORMAL,
        HARDCORE
    }
}