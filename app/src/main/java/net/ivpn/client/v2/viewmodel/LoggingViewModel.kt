package net.ivpn.client.v2.viewmodel

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context
import android.net.Uri
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.BuildController
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.common.utils.FileUtils
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
        isLoggingEnabled.set(getLoggingValue())
        isCrashLoggingEnabled.set(isSentryEnabled())
        isSentrySupported.set(getSentrySupport())
    }

    fun getLogFileUri(context: Context?): Uri {
        return FileUtils.createLogFileUri(context)
    }

    fun reset() {
        isLoggingEnabled.set(getLoggingValue())
        isCrashLoggingEnabled.set(isSentryEnabled())
        isSentrySupported.set(getSentrySupport())
    }

    private fun getLoggingValue(): Boolean {
        return settings.isLoggingEnabled
    }

    private fun isSentryEnabled(): Boolean {
        return sentryUtil.isEnabled
    }

    private fun getSentrySupport(): Boolean {
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