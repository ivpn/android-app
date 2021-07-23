package net.ivpn.core.common.utils;

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