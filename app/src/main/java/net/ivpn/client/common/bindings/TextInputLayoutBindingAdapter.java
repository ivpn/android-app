package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.AppCompatSpinner;

public class TextInputLayoutBindingAdapter {

    @BindingAdapter("error")
    public static void setPort(TextInputLayout view, String error) {
        view.setError(error);
    }
}
