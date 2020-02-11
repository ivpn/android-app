package net.ivpn.client.common.utils;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;
import android.util.Patterns;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;

import java.util.regex.Matcher;


public class StringUtil {
    public static String getLocationFromGateway(String gateway) {
        String[] gatewayParts = gateway.split("\\.");
        return gatewayParts[0];
    }

    public static boolean validateUsername(String login) {
        if (login == null || login.isEmpty()) return false;
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(login);
        return matcher.matches();
    }

    public static Spanned fromHtml(String unspanned){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(unspanned, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(unspanned);
        }
    }

    public static String formatWifiSSID(String dirtyWifi) {
        if (dirtyWifi == null) {
            return null;
        }
        String noQuoteWifi = dirtyWifi.replaceAll("\"","");
        return noQuoteWifi.trim();
    }

    public static String formatTimeUntilResumed(long timeUntilResumed) {
        Resources resources = IVPNApplication.getApplication().getResources();
        StringBuilder builder = new StringBuilder();
        builder.append(resources.getString(R.string.connect_resumed_in));
        builder.append(" ").append(DateUtil.formatTimerCountDown(timeUntilResumed));
        return builder.toString();
    }
}