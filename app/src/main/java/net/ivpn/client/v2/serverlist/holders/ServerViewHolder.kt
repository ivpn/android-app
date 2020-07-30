package net.ivpn.client.v2.serverlist.holders

import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.R
import net.ivpn.client.databinding.ServerItemBinding
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.serverlist.AdapterListener

class ServerViewHolder(
        val binding: ServerItemBinding,
        val navigator: AdapterListener,
        val listener: HolderListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(server: Server, forbiddenServer: Server?) {
        binding.server = server
        binding.forbiddenServer = forbiddenServer
        binding.navigator = navigator
        binding.star.setImageResource(if (server.isFavourite) R.drawable.ic_star_on else R.drawable.ic_star_off)
        binding.starLayout.setOnClickListener {
            server.let {
                it.isFavourite = !it.isFavourite
                binding.star.setImageResource(if (it.isFavourite) R.drawable.ic_star_on else R.drawable.ic_star_off)
                navigator.changeFavouriteStateFor(it, it.isFavourite)
            }
        }
        binding.serverLayout.setOnClickListener {
            navigator.onServerSelected(server, forbiddenServer)
        }
        binding.executePendingBindings()
    }

}