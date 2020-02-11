package net.ivpn.client.common.utils;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtil {

    public static void setStatusTopMargin(View view, int topMargin) {
        if (view == null) {
            return;
        }

        int correctedTopMargin = topMargin - view.getHeight() / 2;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, correctedTopMargin, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);
        view.invalidate();
    }

    public static void setPauseTimerTopMargin(View view, int topMargin, int height) {
        if (view == null) {
            return;
        }

        int correctedTopMargin = height - topMargin - view.getHeight() / 2;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, correctedTopMargin, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);
        view.invalidate();
    }

}
