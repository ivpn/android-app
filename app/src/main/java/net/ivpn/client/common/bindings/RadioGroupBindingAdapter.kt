package net.ivpn.client.common.bindings

import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import net.ivpn.client.common.nightmode.NightMode

@BindingAdapter("app:checked")
fun setMode(view: RadioGroup, nightMode: NightMode) {
    view.check(nightMode.id)
}

@BindingAdapter("app:listener")
fun setListener(view: RadioGroup, listener: RadioGroup.OnCheckedChangeListener) {
    view.setOnCheckedChangeListener(listener)
}