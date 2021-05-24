package net.ivpn.client.v2.network.viewmodel

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

import android.content.Context
import android.widget.RadioGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ObservableField
import net.ivpn.client.R
import net.ivpn.client.vpn.local.NetworkController
import net.ivpn.client.vpn.model.NetworkState
import org.slf4j.LoggerFactory
import javax.inject.Inject

class CommonBehaviourItemViewModel @Inject constructor(
        private val networkController: NetworkController
) {
    val defaultState = ObservableField<NetworkState>()
    private val selectedDefaultState = ObservableField<NetworkState>()

    @JvmField
    var navigator: OnDefaultBehaviourChanged? = null

    var defaultNetworkStateListener = RadioGroup.OnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
        onDefaultCheckedChanged(checkedId)
    }

    private lateinit var context: Context

    init {
    }

    fun setContext(context: Context) {
        this.context = context
    }

    private fun onDefaultCheckedChanged(checkedId: Int) {
        val networkState = NetworkState.getById(checkedId)
        if (networkState == this.selectedDefaultState.get()) {
            return
        }

        this.selectedDefaultState.set(networkState)
    }

    fun getColor(state: NetworkState): Int {
        return when (state) {
            NetworkState.TRUSTED -> {
                ResourcesCompat.getColor(context.resources, R.color.color_trusted_text, null)
            }
            NetworkState.UNTRUSTED -> {
                ResourcesCompat.getColor(context.resources, R.color.color_untrusted_text, null)
            }
            NetworkState.NONE -> {
                ResourcesCompat.getColor(context.resources, R.color.color_none_text, null)
            }
            NetworkState.DEFAULT -> {
                ResourcesCompat.getColor(context.resources, R.color.color_default_text, null)
            }
        }
    }

    fun getDefaultText(): String? {
        return defaultState.get()?.let {
            context.getString(it.textRes)
        }
    }

    fun applyState() {
        defaultState.set(selectedDefaultState.get())
        navigator?.onDefaultBehaviourChanged(selectedDefaultState.get()!!)
        networkController.updateDefaultNetworkState(selectedDefaultState.get())
    }

    fun setNavigator(navigator: OnDefaultBehaviourChanged) {
        this.navigator = navigator
    }

    fun setDefaultState(defaultState: NetworkState) {
        this.defaultState.set(defaultState)
        this.selectedDefaultState.set(defaultState)
    }

//    fun setMobileDataState(mobileDataState: NetworkState) {
//        this.mobileDataState.set(mobileDataState)
//        this.selectedState.set(mobileDataState)
//    }

    interface OnDefaultBehaviourChanged {
        fun onDefaultBehaviourChanged(defaultState: NetworkState)
//        fun onMobileDataBehaviourChanged(mobileDataState: NetworkState)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CommonBehaviourItemViewModel::class.java)
    }

}