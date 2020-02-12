package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class CheckBoxBindingAdapter {
    @BindingAdapter("app:onChanged")
    public static void setOnChangedSwitchButtonListener(CheckBox view,
                                                        CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("app:onChanged")
    public static void setOnChangedSwitchButtonListener(RadioButton view,
                                                        CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }
}
