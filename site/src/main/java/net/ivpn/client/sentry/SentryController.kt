package net.ivpn.client.sentry

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

import io.sentry.SentryEvent
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import net.ivpn.client.dagger.SiteScope
import net.ivpn.core.IVPNApplication
import net.ivpn.core.common.logger.CrashLoggingController
import net.ivpn.core.common.prefs.Settings
import org.slf4j.LoggerFactory
import javax.inject.Inject

@SiteScope
class SentryController @Inject internal constructor(
        private val settings: Settings
) : CrashLoggingController() {

    override fun init() {
        baseInit()

        SentryAndroid.init(IVPNApplication.application) { options: SentryAndroidOptions ->
            // Add a callback that will be used before the event is sent to Sentry.
            // With this callback, you can modify the event or, when returning null, also discard the event.
            options.beforeSend = BeforeSendCallback { event: SentryEvent?, _: Any? ->
                return@BeforeSendCallback if (isEnabled) {
                    LOGGER.info("Event was sent")
                    event
                } else {
                    LOGGER.info("Event was NOT sent")
                    null
                }
            }
        }
    }

    override fun setState(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        settings.isSentryEnabled = isEnabled
    }

    override fun reset() {
        baseInit()
    }

    private fun baseInit() {
        isSupported = true
        isEnabled = settings.isSentryEnabled
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SentryController::class.java)
    }
}