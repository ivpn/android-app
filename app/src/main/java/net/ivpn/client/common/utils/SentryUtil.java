package net.ivpn.client.common.utils;

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

import android.content.Context;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import io.sentry.android.core.SentryAndroid;

@ApplicationScope
public class SentryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentryUtil.class);

    private Settings settings;
    private Context context;

    public boolean isEnabled;

    @Inject
    SentryUtil(Context context, Settings settings) {
        this.settings = settings;
        this.context = context;
    }

    public void init() {
        isEnabled = settings.isSentryEnabled();

        SentryAndroid.init(context, options -> {
            // Add a callback that will be used before the event is sent to Sentry.
            // With this callback, you can modify the event or, when returning null, also discard the event.
            options.setBeforeSend((event, hint) -> {
                if (isEnabled) {
                    LOGGER.info("Event was sent");
                    return event;
                } else {
                    LOGGER.info("Event was NOT sent");
                    return null;
                }
            });
        });
    }

    public void setState(boolean isEnabled) {
        this.isEnabled = isEnabled;
        settings.enableSentry(isEnabled);
    }
}