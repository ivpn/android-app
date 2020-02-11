package net.ivpn.client.common.utils;

import android.content.Context;
import android.widget.Toast;

import net.ivpn.client.IVPNApplication;

public class ToastUtil {
    public static void toast(int msgId) {
        Context context = IVPNApplication.getApplication();
        toast(context, context.getString(msgId));
    }

    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, int msgId) {
        toast(context, context.getString(msgId));
    }
}
