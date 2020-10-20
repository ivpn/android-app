package net.ivpn.client.common;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

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
                    .setActionTextColor(resources.getColor(R.color.primary));
        }

        snackbar.show();
    }
}