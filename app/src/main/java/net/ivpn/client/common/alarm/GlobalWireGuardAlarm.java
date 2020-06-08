package net.ivpn.client.common.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.SettingsPreference;
import net.ivpn.client.common.utils.DateUtil;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.ServiceConstants;
import net.ivpn.client.vpn.controller.WireGuardKeyBroadcastReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static android.content.Context.ALARM_SERVICE;

@ApplicationScope
public class GlobalWireGuardAlarm implements ServiceConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalWireGuardAlarm.class);
    private static final int WIREGUARD_KEYS_ALARM_CODE = 177;

    private Context context;
    private SettingsPreference settingsPreference;
    private ProtocolController protocolController;
    private boolean isRunning;

    @Inject
    GlobalWireGuardAlarm(Context context, SettingsPreference settingsPreference, ProtocolController protocolController) {
        this.context = context;
        this.settingsPreference = settingsPreference;
        this.protocolController = protocolController;

        initAlarmManager();
        checkForKeysGenerationDate();
    }

    private void initAlarmManager() {
        LOGGER.info( "initAlarmManager: ");
        if (!protocolController.getCurrentProtocol().equals(Protocol.WireGuard)) {
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
                context, WIREGUARD_KEYS_ALARM_CODE, intent, 0);
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
                context, WIREGUARD_KEYS_ALARM_CODE, intent, 0);
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
                context, WIREGUARD_KEYS_ALARM_CODE, intent, 0);
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