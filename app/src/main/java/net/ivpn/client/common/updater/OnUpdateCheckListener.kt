package net.ivpn.client.common.updater

import java.lang.Exception

interface OnUpdateCheckListener {

    fun onUpdateAvailable(update: Update)

    fun onVersionUpToDate()

    fun onError(exception: Exception)
}
