package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import javax.inject.Inject

class UpdatesViewModel @Inject constructor(
        private val buildController: BuildController
) : ViewModel() {
    val isUpdatesSupported = ObservableBoolean()

    fun onResume() {
        isUpdatesSupported.set(isUpdatesSupported())
    }

    private fun isUpdatesSupported(): Boolean {
        return buildController.isUpdatesSupported
    }
}