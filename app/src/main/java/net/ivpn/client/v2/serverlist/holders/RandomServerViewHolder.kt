package net.ivpn.client.v2.serverlist.holders

import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.databinding.RandomServerItemBinding
import net.ivpn.client.ui.serverlist.AdapterListener

class RandomServerViewHolder(
        binding: RandomServerItemBinding,
        navigator: AdapterListener
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.navigator = navigator
        binding.executePendingBindings()
    }
}