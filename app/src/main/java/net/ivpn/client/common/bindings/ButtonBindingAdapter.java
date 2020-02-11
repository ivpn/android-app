package net.ivpn.client.common.bindings;

import android.databinding.BindingAdapter;
import android.widget.Button;

public class ButtonBindingAdapter {

    @BindingAdapter("app:enabled")
    public static void setImageUri(Button view, boolean isEnabled) {
        view.setEnabled(isEnabled);
    }
}
