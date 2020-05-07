package net.ivpn.client.common.nightmode

interface OnNightModeChangedListener {

    fun onNightModeChanged(mode: NightMode?)

    fun onNightModeCancelClicked()

}