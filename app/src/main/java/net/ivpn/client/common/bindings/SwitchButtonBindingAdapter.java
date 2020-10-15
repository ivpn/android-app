package net.ivpn.client.common.bindings;

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


import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;

import net.ivpn.client.R;
import net.ivpn.client.ui.connect.ConnectionState;

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
}