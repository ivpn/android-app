package net.ivpn.client.common.utils;

import android.content.res.Resources;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final SimpleDateFormat dateFormat =  new SimpleDateFormat("MMM d, yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("MdyyyyHHmmss");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM d, HH:mm");

    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long FIVE_MINUTES = MINUTE * 5;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;
    public static final long DAYS_4 = DAY * 4;

    //To test
    public static final long H7 = HOUR * 7;
    public static final long H40 = HOUR * 40;

    public static String formatDate(long unixTimeStamp) {
        //It's crutch, because we got timestamp in seconds, not in milliseconds
        //Forgive me please, next generation, i don't have another choice
        Date date = new Date(unixTimeStamp * 1000);
        return dateFormat.format(date);
    }

    public static String formatTime(long timeStamp) {
        Date date = new Date(timeStamp);
        return timeFormat.format(date);
    }

    public static String formatLogFileName(long timeStamp) {
        Date date = new Date(timeStamp);
        return fileNameFormat.format(date);
    }

    public static String formatTimerCountDown(long time) {
        //ToDo Bad implementation, look into alternative way to format time for timer.
        //It's not possible to use SimpleDateFormat because Date doesn't have method to change TimeZone
        return String.format("%02d:%02d:%02d", time / HOUR, (time % HOUR) / MINUTE, (time % MINUTE) / SECOND);
    }

    public static String formatNotificationTimerCountDown(long time) {
        if (time == -1) {
            return "";
        }
        if (time < MINUTE) {
            time = MINUTE;
        }
        //ToDo Bad implementation, look into alternative way to format time for timer.
        //It's not possible to use SimpleDateFormat because Date doesn't have method to change TimeZone
        return String.format("%02d:%02d", time / HOUR, (time % HOUR) / MINUTE);
    }

    public static boolean isSubscriptionComingEnd(long activeUntilUnixTimeStamp) {
        if (activeUntilUnixTimeStamp == 0) {
            return false;
        }
        long activeUntilTimeStamp = activeUntilUnixTimeStamp * 1000;
        long currentTimeStamp = System.currentTimeMillis();

        return (activeUntilTimeStamp - currentTimeStamp) < 4 * DAY;
    }

    public static String formatSubscriptionTimeLeft(long activeUntilUnixTimeStamp) {
        long activeUntilTimeStamp = activeUntilUnixTimeStamp;
        long timeLeft = activeUntilTimeStamp - System.currentTimeMillis();
        if (((int) timeLeft / DAY) > 0) {
            return daysInTimeMillis(timeLeft);
        } else {
            return hoursInTimeMillis(timeLeft);
        }
    }

    public static boolean isSubscriptionExpired(long activeUntilUnixTimeStamp) {
        long activeUntilTimeStamp = activeUntilUnixTimeStamp * 1000;
        return System.currentTimeMillis() > activeUntilTimeStamp;
    }

    private static String daysInTimeMillis(long timeMillis) {
        Resources resources = IVPNApplication.getApplication().getResources();
        int days = (int) (timeMillis / DAY);
        return String.format(resources.getQuantityString(R.plurals.days_in_millis, days), days);
    }

    private static String hoursInTimeMillis(long timeMillis) {
        Resources resources = IVPNApplication.getApplication().getResources();
        int hours = Math.max((int) (timeMillis / HOUR), 1);
        return String.format(resources.getQuantityString(R.plurals.hours_in_millis, hours), hours);
    }

    public static String formatCountDown(int seconds) {
        Resources resources = IVPNApplication.getApplication().getResources();
        return String.format(resources.getQuantityString(R.plurals.count_down_in_seconds, seconds), seconds);
    }

    public static String formatRegenerationPeriod(String days) {
        int daysI = Integer.valueOf(days);
        Resources resources = IVPNApplication.getApplication().getResources();
        return String.format(resources.getQuantityString(R.plurals.regeneration_in_days, daysI), daysI);
    }

    public static String formatDateTime(long timeStamp) {
        Date date = new Date(timeStamp);
        return dateTimeFormat.format(date);
    }
}