package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import android.widget.Button;

public class ButtonBindingAdapter {

    @BindingAdapter("enabled")
    public static void setImageUri(Button view, boolean isEnabled) {
        view.setEnabled(isEnabled);
    }
}
