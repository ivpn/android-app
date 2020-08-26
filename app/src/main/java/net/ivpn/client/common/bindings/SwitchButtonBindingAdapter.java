package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;

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
}