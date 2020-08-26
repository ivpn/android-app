package net.ivpn.client.common.bindings

import android.graphics.Typeface
import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.ivpn.client.vpn.model.NetworkState


@BindingAdapter("background")
fun setBackground(view: TextView, backgroundId: Int) {
    view.setBackgroundResource(backgroundId)
}

@BindingAdapter("android:text")
fun setText(view: TextView, state: NetworkState?) {
    state?.let {
        view.setText(it.textRes)
    }
}

@BindingAdapter("isBold")
fun setBold(view: TextView, isBold: Boolean) {
    view.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
}