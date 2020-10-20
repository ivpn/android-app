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
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;

import net.ivpn.client.ui.network.NetworkAdapter;
import net.ivpn.client.ui.network.OnNetworkBehaviourChangedListener;
import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.ui.protocol.port.OnPortSelectedListener;
import net.ivpn.client.ui.protocol.port.Port;
import net.ivpn.client.ui.protocol.port.PortAdapter;

import java.util.Arrays;

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

    @BindingAdapter("onChanged")
    public static void setOnItemSelectedListener(AppCompatSpinner view, final OnNetworkBehaviourChangedListener listener) {
        final NetworkAdapter adapter = (NetworkAdapter) view.getAdapter();
        view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NetworkState[] allowedStates = adapter.getAllowedStates();
                if (listener != null) {
                    listener.onNetworkBehaviourChanged(allowedStates[position]);
                }
                adapter.setCurrentPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
        view.setSelection(port.ordinalForProtocol());
    }

    @BindingAdapter("selectedItem")
    public static void setNetworkState(AppCompatSpinner view, NetworkState state) {
        final NetworkAdapter adapter = (NetworkAdapter) view.getAdapter();
        if (state != null && adapter != null) {
            NetworkState[] allowedStates = adapter.getAllowedStates();
            view.setSelection(Arrays.asList(allowedStates).indexOf(state));
        }
    }

    @BindingAdapter("default_network_state")
    public static void setDefaultNetworkState(AppCompatSpinner view, NetworkState state) {
        NetworkAdapter adapter = (NetworkAdapter) view.getAdapter();
        if (state != null && adapter != null) {
            adapter.setDefaultState(state);
        }
    }
}