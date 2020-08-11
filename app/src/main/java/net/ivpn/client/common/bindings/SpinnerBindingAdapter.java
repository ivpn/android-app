package net.ivpn.client.common.bindings;

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