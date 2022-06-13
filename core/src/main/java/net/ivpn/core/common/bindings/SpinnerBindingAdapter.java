package net.ivpn.core.common.bindings;

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


import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;

import net.ivpn.core.v2.protocol.port.OnPortSelectedListener;
import net.ivpn.core.v2.protocol.port.Port;
import net.ivpn.core.v2.protocol.port.PortAdapter;

import java.util.Arrays;
import java.util.List;

public class SpinnerBindingAdapter {

    @BindingAdapter("onChanged")
    public static void setOnItemSelectedListener(AppCompatSpinner view,
                                                 final OnPortSelectedListener listener) {
        final PortAdapter adapter = (PortAdapter) view.getAdapter();
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Port[] ports = adapter.getAllowedPorts();
                listener.onPortSelected(ports[position]);
                adapter.setCurrentPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @BindingAdapter("onTouch")
    public static void setOnTouchListener(AppCompatSpinner view,
                                                 final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }

    @BindingAdapter("selectedItem")
    public static void setPort(AppCompatSpinner view, Port port) {
        final PortAdapter adapter = (PortAdapter) view.getAdapter();
        List<Port> ports = Arrays.asList(adapter.getAllowedPorts());
        view.setSelection(ports.indexOf(port));
    }
}