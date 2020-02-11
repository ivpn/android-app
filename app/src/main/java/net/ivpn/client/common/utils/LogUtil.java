package net.ivpn.client.common.utils;

public class LogUtil {

    public static void enableLogging(boolean enableLogging) {
        if (enableLogging) {
            FileUtils.clearAllLogs();
        }
    }
}