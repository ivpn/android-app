/*
 * Copyright © 2018 Samuel Holland <samuel@sholland.org>
 * Copyright © 2018 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.model

import com.wireguard.android.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ivpn.client.IVPNApplication
import org.slf4j.LoggerFactory

class Tunnel(var name: String, var config: Config?, private var state: State) {

    var listener: OnStateChangedListener? = null

    fun getState(): State {
        return state
    }

    suspend fun setState(state: State) {
        try {
            this.state = state
            LOGGER.info("Set state = $state")
            withContext(Dispatchers.IO) {
                IVPNApplication.getApplication().appComponent.provideGoBackend().setState(this@Tunnel, state, config)
            }
        } catch (e: Exception) {
            LOGGER.error(e.localizedMessage)
        }
    }

    fun onStateChange(newState: State) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                listener?.onStateChanged(newState)
            }
        }
//        listener?.onStateChanged(newState)
    }

    enum class State {
        DOWN, TOGGLE, UP;

        companion object {
            fun of(running: Boolean): State {
                return if (running) UP else DOWN
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Tunnel::class.java)
    }

    interface OnStateChangedListener {
        fun onStateChanged(newState: State)
    }
}