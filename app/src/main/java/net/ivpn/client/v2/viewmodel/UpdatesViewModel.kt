package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.dagger.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class UpdatesViewModel @Inject constructor(
        private val buildController: BuildController
) : ViewModel() {
    val isUpdatesSupported = ObservableBoolean()

    fun onResume() {
        isUpdatesSupported.set(getUpdateSupport())
    }

    private fun getUpdateSupport(): Boolean {
        return buildController.isUpdatesSupported
    }
}