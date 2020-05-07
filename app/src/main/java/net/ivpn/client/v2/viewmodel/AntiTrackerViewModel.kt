package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import javax.inject.Inject

class AntiTrackerViewModel @Inject constructor(
        private val buildController: BuildController
) : ViewModel() {

    val isAntiTrackerEnabled = ObservableBoolean()

    fun onResume() {
        isAntiTrackerEnabled.set(isAntiTrackerEnabled())
    }

    private fun isAntiTrackerEnabled(): Boolean {
        return buildController.isAntiTrackerEnabled
    }
}