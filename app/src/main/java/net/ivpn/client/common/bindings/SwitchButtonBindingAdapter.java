package net.ivpn.client.common.bindings;

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
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.appcompat.widget.SwitchCompat;
import androidx.databinding.BindingAdapter;

import net.ivpn.client.R;
import net.ivpn.client.ui.connect.ConnectionState;

import static net.ivpn.client.v2.viewmodel.AntiTrackerViewModel.AntiTrackerState;

public class SwitchButtonBindingAdapter {

    @BindingAdapter("onChanged")
    public static void setOnChangedSwitchButtonListener(SwitchCompat view,
                                                        OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("onTouch")
    public static void setOnTouchListener(SwitchCompat view,
                                          final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter("onTouch")
    public static void setOnTouchListener(View view,
                                          final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter("connectionState")
    public static void setConnectionState(SwitchCompat switchView, ConnectionState state) {
        int thumbRes = 0;
        int trackRes = 0;

        switch (state) {
            case NOT_CONNECTED:
                thumbRes = R.drawable.thumb_disconnected;
                trackRes = R.drawable.track_disconnected;
                break;
            case CONNECTING:
                thumbRes = R.drawable.thumb_connecting;
                trackRes = R.drawable.track_connecting;
                break;
            case CONNECTED:
                thumbRes = R.drawable.thumb_connected;
                trackRes = R.drawable.track_connected;
                break;
            case DISCONNECTING:
                thumbRes = R.drawable.thumb_disconnecting;
                trackRes = R.drawable.track_disconnecting;
                break;
            case PAUSING:
            case PAUSED:
                thumbRes = R.drawable.thumb_paused;
                trackRes = R.drawable.track_paused;
                break;
        }

        switchView.setThumbResource(thumbRes);
        switchView.setTrackResource(trackRes);
    }

    @BindingAdapter("antitrackerState")
    public static void setAntiTrackerState(SwitchCompat switchView, AntiTrackerState state) {
        int thumbRes = 0;
        int trackRes = 0;

        switch (state) {
            case DISABLED:
                thumbRes = R.drawable.thumb_antitracker_disabled;
                trackRes = R.drawable.track_antitracker_disabled;
                break;
            case NORMAL:
                thumbRes = R.drawable.thumb_antitracker_normal;
                trackRes = R.drawable.track_antitracker_normal;
                break;
            case HARDCORE:
                thumbRes = R.drawable.thumb_antitracker_hardcore;
                trackRes = R.drawable.track_antitracker_hardcore;
                break;
        }

        switchView.setThumbResource(thumbRes);
        switchView.setTrackResource(trackRes);
    }
}