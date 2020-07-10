package net.ivpn.client.v2.serverlist.all

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.R
import net.ivpn.client.databinding.FastestServerItemBinding
import net.ivpn.client.databinding.RandomServerItemBinding
import net.ivpn.client.databinding.SearchItemBinding
import net.ivpn.client.databinding.ServerItemBinding
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.serverlist.AdapterListener
import net.ivpn.client.v2.serverlist.FavouriteServerListener
import net.ivpn.client.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.client.v2.serverlist.holders.*
import net.ivpn.client.v2.serverlist.items.ConnectionOption
import net.ivpn.client.v2.serverlist.items.RandomServerItem
import net.ivpn.client.v2.serverlist.items.SearchServerItem
import java.util.*
import kotlin.collections.ArrayList

class AllServersRecyclerViewAdapter(
        private val navigator: AdapterListener,
        private val isFastestServerAllowed: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServerBasedRecyclerViewAdapter, FavouriteServerListener {

    private var servers = arrayListOf<Server>()
    private var filteredServers = arrayListOf<ConnectionOption>()
    private var forbiddenServer: Server? = null
    private var isFiltering = false

    var listener: HolderListener = object : HolderListener {
        override fun invalidate(server: Server) {
            notifyItemChanged(getPositionFor(server))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isFiltering) {
            return when (position) {
                0 -> SEARCH_ITEM
                else -> SERVER_ITEM
            }
        } else {
            return when (position) {
                0 -> SEARCH_ITEM
                1 -> RANDOM_ITEM
                2 -> {
                    if (isFastestServerAllowed) {
                        FASTEST_SERVER_ITEM
                    } else {
                        SERVER_ITEM
                    }
                }
                else -> SERVER_ITEM
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SEARCH_ITEM -> {
                val binding = SearchItemBinding.inflate(layoutInflater, parent, false)
                SearchViewHolder(binding, searchFilter)
            }
            RANDOM_ITEM -> {
                val binding = RandomServerItemBinding.inflate(layoutInflater, parent, false)
                RandomServerViewHolder(binding, navigator)
            }
            FASTEST_SERVER_ITEM -> {
                val binding = FastestServerItemBinding.inflate(layoutInflater, parent, false)
                FastestServerViewHolder(binding, navigator)
            }
            else -> {
                val binding = ServerItemBinding.inflate(layoutInflater, parent, false)
                ServerViewHolder(binding, navigator, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNotEmpty()) {
            val payload = payloads[0]
            if (payload is Boolean && holder is ServerViewHolder) {
                holder.binding.star.setImageResource(if (payload) R.drawable.ic_star_on else R.drawable.ic_star_off)
            }
        }else {
            super.onBindViewHolder(holder,position, payloads);
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ServerViewHolder) {
            val server: ConnectionOption = getServerFor(position)
            if (server is Server) {
                holder.bind(server, forbiddenServer)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredServers.size
    }

    override fun replaceData(items: List<Server>) {
        setServers(ArrayList(items))
    }

    private fun setServers(servers: ArrayList<Server>) {
        Collections.sort(servers, Server.comparator)
        this.servers = servers
        filteredServers = prepareDataToShow(servers)
        notifyDataSetChanged()
    }

    private fun prepareDataToShow(servers: ArrayList<Server>): ArrayList<ConnectionOption> {
        val listToShow = ArrayList<ConnectionOption>()
        listToShow.add(SearchServerItem())
        if (!isFiltering) {
            listToShow.add(RandomServerItem())
            if (isFastestServerAllowed) {
                listToShow.add(RandomServerItem())
            }
        }
        listToShow.addAll(servers)

        return listToShow
    }

    override fun setForbiddenServer(server: Server?) {
        forbiddenServer = server
    }

    private fun getPositionFor(server: Server): Int {
        return filteredServers.indexOf(server)
    }

    private fun getServerFor(position: Int): ConnectionOption {
        return filteredServers[position]
    }

    private val searchFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList = ArrayList<Server>()

            if (constraint == null || constraint.isEmpty()) {
                isFiltering = false
                filteredList.addAll(servers)
            } else {
                isFiltering = true
                val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                for (server in servers) {
                    if (server.description.toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(server)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.values is List<*>) {
                val oldList = filteredServers
                filteredServers = prepareDataToShow(results.values as ArrayList<Server>)
                notifyChanges(oldList, filteredServers)
            }
        }
    }

    fun notifyChanges(oldList: List<ConnectionOption>, newList: List<ConnectionOption>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size
        })
        diff.dispatchUpdatesTo(this)
    }

    companion object {
        private const val FASTEST_SERVER_ITEM = 0
        private const val SERVER_ITEM = 1
        private const val SEARCH_ITEM = 2
        private const val RANDOM_ITEM = 3
    }

    override fun onChangeState(server: Server, isFavourite: Boolean) {
        var position = filteredServers.indexOf(server)
        if (position >= 0) {
            notifyItemChanged(position, isFavourite)
        }

        position = servers.indexOf(server)
        servers[position].isFavourite = isFavourite
    }

}