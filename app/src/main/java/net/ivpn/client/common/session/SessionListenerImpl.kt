package net.ivpn.client.common.session

import net.ivpn.client.common.session.SessionController.*
import net.ivpn.client.rest.data.session.SessionNewResponse
import net.ivpn.client.rest.data.wireguard.ErrorResponse

open class SessionListenerImpl: SessionListener {
    override fun onRemoveSuccess() {
    }

    override fun onRemoveError() {
    }

    override fun onCreateSuccess(response: SessionNewResponse) {
    }

    override fun onCreateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
    }

    override fun onUpdateSuccess() {
    }

    override fun onUpdateError(throwable: Throwable?, errorResponse: ErrorResponse?) {
    }
}