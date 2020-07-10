package net.ivpn.client.common.bindings

import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import net.ivpn.client.common.nightmode.NightMode
import net.ivpn.client.vpn.model.NetworkState

@BindingAdapter("checked")
fun setMode(view: RadioGroup, nightMode: NightMode) {
    view.check(nightMode.id)
}

@BindingAdapter("listener")
fun setListener(view: RadioGroup, listener: RadioGroup.OnCheckedChangeListener) {
    view.setOnCheckedChangeListener(listener)
}

@BindingAdapter("checked")
fun setMode(view: RadioGroup, networkState: NetworkState) {
    view.check(networkState.id)
}