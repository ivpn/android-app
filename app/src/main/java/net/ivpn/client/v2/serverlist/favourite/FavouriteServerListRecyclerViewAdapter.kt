package net.ivpn.client.v2.serverlist.favourite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.databinding.ServerItemBinding
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.serverlist.AdapterListener
import net.ivpn.client.v2.serverlist.FavouriteServerListener
import net.ivpn.client.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.client.v2.serverlist.holders.HolderListener
import net.ivpn.client.v2.serverlist.holders.ServerViewHolder
import java.util.*

class FavouriteServerListRecyclerViewAdapter(
        private val navigator: AdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServerBasedRecyclerViewAdapter, FavouriteServerListener {

    private var favourites = arrayListOf<Server>()
    private var forbiddenServer: Server? = null

    var listener: HolderListener = object : HolderListener {
        override fun invalidate(server: Server) {
            notifyItemChanged(getPositionFor(server))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return SERVER_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ServerItemBinding.inflate(layoutInflater, parent, false)
        return ServerViewHolder(binding, navigator, listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ServerViewHolder) {
            holder.bind(getServerFor(position), forbiddenServer)
        }
    }

    override fun getItemCount(): Int {
        return favourites.size
    }

    private fun removeFavouriteServer(server: Server) {
        if (!favourites.contains(server)) {
            return
        }
        val position = getPositionFor(server)
        favourites.remove(server)
        if (position >= 0) {
            notifyItemRemoved(position)
        }
    }

    private fun addFavouriteServer(server: Server) {
        if (favourites.contains(server)) {
            return
        }
        favourites.add(server)
        Collections.sort(favourites, Server.comparator)
        val position = getPositionFor(server)
        notifyItemInserted(position)
    }

    private fun setServers(servers: ArrayList<Server>) {
        Collections.sort(servers, Server.comparator)
        this.favourites = servers
        notifyDataSetChanged()
    }

    override fun setForbiddenServer(server: Server?) {
        forbiddenServer = server
    }

    override fun replaceData(items: List<Server>) {
        setServers(ArrayList(items))
    }

    private fun getPositionFor(server: Server): Int {
        return favourites.indexOf(server)
    }

    private fun getServerFor(position: Int): Server {
        return favourites[position]
    }

    companion object {
        private const val SERVER_ITEM = 1
    }

    override fun onChangeState(server: Server, isFavourite: Boolean) {
        if (isFavourite) {
            addFavouriteServer(server)
        } else {
            removeFavouriteServer(server)
        }
    }
}