package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;

import net.ivpn.client.ui.protocol.view.OnValueChangeListener;
import net.ivpn.client.ui.protocol.view.ValueSelectionView;

public class ValueSelectionBindingAdapter {

    @BindingAdapter("value")
    public static void setValue(ValueSelectionView view, String value) {
        view.setValue(Integer.valueOf(value));
    }

    @BindingAdapter("listener")
    public static void setListener(ValueSelectionView imageView, OnValueChangeListener listener) {
        imageView.setOnValueChangedListener(listener);
    }
}
