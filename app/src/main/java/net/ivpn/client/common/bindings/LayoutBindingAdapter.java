package net.ivpn.client.common.bindings;

import android.content.res.Resources;
import androidx.databinding.BindingAdapter;
import android.widget.LinearLayout;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.vpn.model.NetworkState;

public class LayoutBindingAdapter {
    @BindingAdapter({"currentState", "defaultState"})
    public static void setBackgroundColor(LinearLayout view, NetworkState currentState, NetworkState defaultState) {
        Resources resources = IVPNApplication.getApplication().getResources();
//        view.setBackgroundColor(resources.getColor(currentState.getBackgroundColor(defaultState)));
    }
}