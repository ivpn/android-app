package net.ivpn.client.common.bindings;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatSpinner;

import net.ivpn.client.ui.protocol.port.Port;

public class TextInputLayoutBindingAdapter {

    @BindingAdapter("app:error")
    public static void setPort(TextInputLayout view, String error) {
        view.setError(error);
    }
}
