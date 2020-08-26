package net.ivpn.client.v2.serverlist.holders

import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.databinding.FastestServerItemBinding
import net.ivpn.client.ui.serverlist.AdapterListener

class FastestServerViewHolder(
        binding: FastestServerItemBinding,
        navigator: AdapterListener
): RecyclerView.ViewHolder(binding.root) {
    init {
        binding.navigator = navigator
        binding.executePendingBindings()
    }
}