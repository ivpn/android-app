package net.ivpn.client.vpn.model

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R

enum class NetworkState(val id: Int, val textRes: Int, val backgroundId: Int) {
    TRUSTED(R.id.trusted_state, R.string.network_trusted, R.drawable.trusted_state_background) {
        override fun getColor(): Int {
            return ResourcesCompat.getColor(IVPNApplication.getApplication().resources, R.color.color_trusted_text, null)
        }

    },
    UNTRUSTED(R.id.untrusted_state, R.string.network_untrusted, R.drawable.untrusted_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_untrusted_text)
        }

    },
    NONE(R.id.none_state, R.string.network_state_none, R.drawable.none_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_none_text)
        }

    },
    DEFAULT(R.id.default_state, R.string.network_default, R.drawable.none_state_background) {
        override fun getColor(): Int {
            return ContextCompat.getColor(IVPNApplication.getApplication(), R.color.color_default_text)
        }
    };

    abstract fun getColor(): Int

    companion object {
        fun getById(id: Int): NetworkState {
            for (mode in values()) {
                if (mode.id == id) {
                    return mode
                }
            }

            return TRUSTED
        }

        val defaultStates: Array<NetworkState>
            get() = arrayOf(TRUSTED, UNTRUSTED, NONE)

        val activeState: Array<NetworkState>
            get() = arrayOf(TRUSTED, UNTRUSTED, DEFAULT)
    }
}