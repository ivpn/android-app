package net.ivpn.client.v2.viewmodel

import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.utils.SentryUtil
import javax.inject.Inject

class LoggingViewModel @Inject constructor(
        private val settings: Settings,
        private val sentryUtil: SentryUtil,
        private val buildController: BuildController
) : ViewModel()  {

    val isLoggingEnabled = ObservableBoolean()
    val isCrashLoggingEnabled = ObservableBoolean()
    val isSentrySupported = ObservableBoolean()

    var enableLoggingListener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableLogging(value) }
    var enableCrashLoggingListener = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean -> enableCrashLogging(value) }

    init {
    }

    fun onResume() {
        isLoggingEnabled.set(isLoggingEnabled())
        isCrashLoggingEnabled.set(isSentryEnabled())
        isSentrySupported.set(isSentryEnabled())
    }

    private fun isLoggingEnabled(): Boolean {
        return settings.isLoggingEnabled
    }

    private fun isSentryEnabled(): Boolean {
        return sentryUtil.isEnabled
    }

    private fun isSentrySupported(): Boolean {
        return buildController.isSentrySupported
    }

    private fun enableLogging(value: Boolean) {
        isLoggingEnabled.set(value)
        settings.enableLogging(value)
    }

    private fun enableCrashLogging(value: Boolean) {
        isCrashLoggingEnabled.set(value)
        sentryUtil.setState(value)
    }


}