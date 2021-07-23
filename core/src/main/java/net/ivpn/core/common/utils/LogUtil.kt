package net.ivpn.core.common.utils

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

import ch.qos.logback.classic.Logger
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.logger.FileUtils
import net.ivpn.core.common.prefs.EncryptedSettingsPreference
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import javax.inject.Inject

@ApplicationScope
class LogUtil @Inject constructor(
        private val settingsPreference: EncryptedSettingsPreference
) {

    var isLoggingEnabled = settingsPreference.getSettingLogging()

    fun initialize() {
        SLF4JBridgeHandler.install()
        if (isLoggingEnabled) {
            nativeEnableLogging()
//            val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
//            root.level = ch.qos.logback.classic.Level.OFF
        } else {
            nativeDisableLogging()
        }
    }

    fun enableLogging(enableLogging: Boolean) {
        if (enableLogging == isLoggingEnabled) return

        isLoggingEnabled = enableLogging
        if (isLoggingEnabled) {
            nativeEnableLogging()
        } else {
            nativeDisableLogging()
            FileUtils.clearAllLogs()
        }
        settingsPreference.putSettingLogging(isLoggingEnabled)
    }


    private fun nativeDisableLogging() {
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.level = ch.qos.logback.classic.Level.OFF
    }

    private fun nativeEnableLogging() {
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.level = ch.qos.logback.classic.Level.ALL
    }

}