package net.ivpn.core.common.alarm;

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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.ivpn.core.common.dagger.ApplicationScope;
import net.ivpn.core.common.prefs.EncryptedSettingsPreference;
import net.ivpn.core.common.utils.DateUtil;
import net.ivpn.core.vpn.Protocol;
import net.ivpn.core.vpn.ProtocolController;
import net.ivpn.core.vpn.ServiceConstants;
import net.ivpn.core.vpn.controller.WireGuardKeyBroadcastReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static android.content.Context.ALARM_SERVICE;

@ApplicationScope
public class GlobalWireGuardAlarm implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalWireGuardAlarm.class);
    private static final int WIREGUARD_KEYS_ALARM_CODE = 177;

    private Context context;
    private EncryptedSettingsPreference settingsPreference;
    private ProtocolController protocolController;
    private boolean isRunning;

    @Inject
    GlobalWireGuardAlarm(Context context, EncryptedSettingsPreference settingsPreference, ProtocolController protocolController) {
        this.context = context;
        this.settingsPreference = settingsPreference;
        this.protocolController = protocolController;

        initAlarmManager();
        checkForKeysGenerationDate();
    }

    private void initAlarmManager() {
        LOGGER.info( "initAlarmManager: ");
        if (!protocolController.getCurrentProtocol().equals(Protocol.WIREGUARD)) {
            return;
        }
        LOGGER.info( "initAlarmManager: setAlarm");

        start();
    }

    public void startShortPeriod() {
        if (isRunning) {
            stop();
        }
        isRunning = true;

        Intent intent = new Intent(context, WireGuardKeyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WIREGUARD_KEYS_ALARM_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        LOGGER.info("initAlarmManager: currentTime = " + System.currentTimeMillis());
        LOGGER.info("initAlarmManager: startAt = " + (settingsPreference.getGenerationTime() + settingsPreference.getRegenerationPeriod() * DateUtil.DAY));
        LOGGER.info( "initAlarmManager: every = " + settingsPreference.getRegenerationPeriod() * DateUtil.DAY);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + DateUtil.HOUR,
                DateUtil.HOUR, pendingIntent);
    }

    public void start() {
        if (isRunning) {
            stop();
        }
        isRunning = true;

        Intent intent = new Intent(context, WireGuardKeyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WIREGUARD_KEYS_ALARM_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        LOGGER.info("start: currentTime = " + System.currentTimeMillis());
        LOGGER.info("start: startAt = " + (settingsPreference.getGenerationTime() + settingsPreference.getRegenerationPeriod() * DateUtil.DAY));
        LOGGER.info( "start: every = " + settingsPreference.getRegenerationPeriod() * DateUtil.DAY);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                settingsPreference.getGenerationTime() + settingsPreference.getRegenerationPeriod() * DateUtil.DAY,
                settingsPreference.getRegenerationPeriod() * DateUtil.DAY, pendingIntent);
    }

    public void stop() {
        isRunning = false;

        Intent intent = new Intent(context, WireGuardKeyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WIREGUARD_KEYS_ALARM_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void checkForKeysGenerationDate() {
        if (settingsPreference.getSettingsWgPrivateKey().equals("")) {
            return;
        }
        if (settingsPreference.isGenerationTimeExist()) {
            return;
        }

        settingsPreference.putGenerationTime(System.currentTimeMillis());
    }
}