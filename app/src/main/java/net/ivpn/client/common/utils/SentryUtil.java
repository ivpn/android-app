package net.ivpn.client.common.utils;

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
    }
}