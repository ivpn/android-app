package net.ivpn.client.common.bindings;

import android.databinding.BindingAdapter;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SwitchButtonBindingAdapter {

    @BindingAdapter("app:onChanged")
    public static void setOnChangedSwitchButtonListener(SwitchCompat view,
                                                        OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("app:onTouch")
    public static void setOnTouchListener(SwitchCompat view,
                                          final View.OnTouchListener listener) {
        view.setOnTouchListener(listener);
    }
}