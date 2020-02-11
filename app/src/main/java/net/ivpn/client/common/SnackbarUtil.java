package net.ivpn.client.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import net.ivpn.client.R;

public class SnackbarUtil {

    //ToDo redo
    public static void show(View rootView, int msgId, int actionId, View.OnClickListener listener) {
        Context context = rootView.getContext();
        show(rootView, context.getString(msgId), context.getString(actionId), listener);
    }

    public static void show(View rootView, String msg, String action, View.OnClickListener listener) {
        if (rootView == null || rootView.getContext() == null) {
            return;
        }

        Context context = rootView.getContext();
        Resources resources = context.getResources();
        Snackbar snackbar = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG);
        if (listener != null) {
            snackbar.setAction(action, listener)
                    .setActionTextColor(resources.getColor(R.color.colorPrimary));
        }

        TextView mainTextView = (snackbar.getView()).findViewById(android.support.design.R.id.snackbar_text);
        mainTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        TextView actionTextView = (snackbar.getView()).findViewById(android.support.design.R.id.snackbar_action);
        actionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        snackbar.show();
    }
}
