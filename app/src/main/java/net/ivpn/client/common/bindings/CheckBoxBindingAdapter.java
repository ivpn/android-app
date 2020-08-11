package net.ivpn.client.common.bindings;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.databinding.BindingAdapter;

public class CheckBoxBindingAdapter {
    @BindingAdapter("onChanged")
    public static void setOnChangedSwitchButtonListener(CheckBox view,
                                                        CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("onChanged")
    public static void setOnChangedSwitchButtonListener(RadioButton view,
                                                        CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }
}
