package net.ivpn.client.common.multihop

import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.prefs.UserPreference
import net.ivpn.client.vpn.ProtocolController
import net.ivpn.client.vpn.controller.VpnBehaviorController
import javax.inject.Inject

@ApplicationScope
class MultiHopController @Inject constructor(
        val userPreference: UserPreference,
        val vpnBehaviorController: VpnBehaviorController,
        val protocolController: ProtocolController,
        val settings: Settings
) {

    var isEnabled: Boolean

    var listeners = ArrayList<OnValueChangeListener>()

    init {
        isEnabled = getIsEnabled()
    }

    fun enable(value : Boolean) {
        if (isEnabled == value) {
            return
        }

        isEnabled = value
        settings.enableMultiHop(value)

        notifyValueChanges()
    }

    fun isSupportedByPlan(): Boolean {
        return userPreference.capabilityMultiHop && isAuthenticated() && isMultihopAllowedByProtocol()
    }

    fun getIsEnabled(): Boolean {
        isEnabled = settings.isMultiHopEnabled && isMultihopAllowedByProtocol()
        return isEnabled
    }

    fun getState() : State {
        return if (!isAuthenticated()) {
            State.NOT_AUTHENTICATED
        } else if (!isActive()) {
            State.SUBSCRIPTION_NOT_ACTIVE
        } else if (isVpnActive()) {
            State.VPN_ACTIVE
        } else if (!isMultihopAllowedByProtocol()) {
            State.DISABLED_BY_PROTOCOL
        } else {
            State.ENABLED
        }
    }

    fun addListener(listener : OnValueChangeListener) {
        listeners.add(listener)
        listener.onValueChange(isEnabled)
    }

    fun removeListener(listener : OnValueChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyValueChanges() {
        for (listener in listeners) {
            listener.onValueChange(isEnabled)
        }
    }

    private fun isAuthenticated() : Boolean {
        val token: String = userPreference.sessionToken
        return token.isNotEmpty()
    }

    private fun isActive(): Boolean {
        return userPreference.isActive
    }

    private fun isVpnActive(): Boolean {
        return vpnBehaviorController.isVPNActive
    }

    private fun isMultihopAllowedByProtocol(): Boolean {
        return protocolController.currentProtocol.isMultihopEnabled
    }

    enum class State {
        ENABLED,
        NOT_AUTHENTICATED,
        SUBSCRIPTION_NOT_ACTIVE,
        VPN_ACTIVE,
        DISABLED_BY_PROTOCOL
    }

    interface OnValueChangeListener {
        fun onValueChange(value: Boolean)
    }
}