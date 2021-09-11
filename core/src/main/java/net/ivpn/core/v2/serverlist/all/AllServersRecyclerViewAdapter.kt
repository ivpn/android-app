package net.ivpn.core.v2.serverlist.all

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 
 This file is part of the IVPN Android app.
 
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.distance.DistanceProvider
import net.ivpn.core.common.distance.OnDistanceChangedListener
import net.ivpn.core.common.pinger.PingResultFormatter
import net.ivpn.core.databinding.FastestServerItemBinding
import net.ivpn.core.databinding.RandomServerItemBinding
import net.ivpn.core.databinding.SearchItemBinding
import net.ivpn.core.databinding.ServerItemBinding
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.serverlist.AdapterListener
import net.ivpn.core.v2.serverlist.FavouriteServerListener
import net.ivpn.core.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.core.v2.serverlist.dialog.Filters
import net.ivpn.core.v2.serverlist.holders.*
import net.ivpn.core.v2.serverlist.items.ConnectionOption
import net.ivpn.core.v2.serverlist.items.FastestServerItem
import net.ivpn.core.v2.serverlist.items.RandomServerItem
import net.ivpn.core.v2.serverlist.items.SearchServerItem
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AllServersRecyclerViewAdapter(
        private val navigator: AdapterListener,
        private val isFastestServerAllowed: Boolean,
        private var filter: Filters?,
        private var isIPv6Enabled: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServerBasedRecyclerViewAdapter, FavouriteServerListener {

    @Inject
    lateinit var distanceProvider: DistanceProvider

    private var bindings = HashMap<ServerItemBinding, Server>()
    private var searchBinding: SearchItemBinding? = null
    private var servers = arrayListOf<Server>()
    private var filteredServers = arrayListOf<ConnectionOption>()
    private var forbiddenServer: Server? = null
    private var isFiltering = false

    val distanceChangedListener = object : OnDistanceChangedListener {
        override fun onDistanceChanged() {
            if (filter == Filters.DISTANCE) {
                setDistances()
                sortServers(servers)
                searchBinding?.search?.let { search ->
                    searchFilter.filter(search.query)
                } ?: run {
                    searchFilter.filter("")
                }
            }
        }
    }

    init {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        distanceProvider.subscribe(distanceChangedListener)
    }

    var listener: HolderListener = object : HolderListener {
        override fun invalidate(server: Server) {
            notifyItemChanged(getPositionFor(server))
        }
    }

    private var pings: Map<Server, PingResultFormatter?>? = null

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
        LOGGER.debug("Bind view with payloads")
        if (payloads.isNotEmpty()) {
            val payload = payloads[0]
            if (payload is Boolean && holder is ServerViewHolder) {
                holder.binding.star.setImageResource(if (payload) R.drawable.ic_star_on else R.drawable.ic_star_off)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        LOGGER.debug("Bind view")
        if (holder is ServerViewHolder) {
            val server: ConnectionOption = getServerFor(position)
            if (server is Server) {
                LOGGER.debug("Bind view binding = ${holder.binding} server = ${server.city}")
                bindings[holder.binding] = server
                setPing(holder.binding, server)
                holder.bind(server, forbiddenServer, isIPv6Enabled, filter)
            }
        } else if (holder is SearchViewHolder) {
            searchBinding = holder.binding
        }
    }

    private fun setPing(binding: ServerItemBinding, server: Server) {
        pings?.let {
            it[server]?.also { formatter ->
                binding.pingstatus = formatter
            } ?: run {
                binding.pingstatus = null
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredServers.size
    }

    override fun replaceData(items: List<Server>) {
        setServers(ArrayList(items))
    }

    override fun setPings(pings: Map<Server, PingResultFormatter?>) {
        this.pings = pings
        setLatencies(pings)

        bindings.forEach {(binding, server) ->
            setPing(binding, server)
        }

        if (filter == Filters.LATENCY) {
            sortServers(servers)
            searchBinding?.search?.let { search ->
                searchFilter.filter(search.query)
            } ?: run {
                searchFilter.filter("")
            }
        }
    }

    override fun setFilter(filter: Filters?) {
        this@AllServersRecyclerViewAdapter.filter = filter
        for ((binding, _) in bindings) {
            binding.filter = filter
            binding.executePendingBindings()
        }
        if (filter == Filters.DISTANCE) {
            setDistances()
        }
        if (filter == Filters.LATENCY) {
            setLatencies()
        }
        sortServers(servers)
        searchBinding?.search?.let { search ->
            searchFilter.filter(search.query)
        } ?: run {
            searchFilter.filter("")
        }
    }

    private fun setLatencies() {
        pings?.let{pingsObj ->
            servers.forEach {
                it.latency = pingsObj[it]?.ping ?: Long.MAX_VALUE
            }
        }
    }

    private fun setDistances() {
        val distances = distanceProvider.distances
        servers.forEach {
            it.distance = distances[it] ?: Float.MAX_VALUE
        }
    }

    private fun setServers(servers: ArrayList<Server>) {
        sortServers(servers)
        this.servers = servers
        searchBinding?.search?.let {
            searchFilter.filter(it.query)
        } ?: run {
            searchFilter.filter("")
//            filteredServers = prepareDataToShow(servers)
//            notifyDataSetChanged()
        }
    }

    private fun sortServers(servers: ArrayList<Server>) {
        filter?.let {
            Collections.sort(servers, it.getServerComparator())
        } ?: run {
            Collections.sort(servers, Server.comparator)
        }
    }

    private fun prepareDataToShow(servers: ArrayList<Server>): ArrayList<ConnectionOption> {
        val listToShow = ArrayList<ConnectionOption>()
        listToShow.add(SearchServerItem())
        if (!isFiltering) {
            listToShow.add(RandomServerItem())
            if (isFastestServerAllowed) {
                listToShow.add(FastestServerItem())
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
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Server>()

            if (constraint == null || constraint.isEmpty()) {
                isFiltering = false
                filteredList.addAll(servers)
            } else {
                isFiltering = true
                val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                for (server in servers) {
                    if (server.getDescription(filter).toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(server)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            println("FILTER performFiltering set Result = ${filteredList.size}")
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
        println("FILTER notifyChanges oldList.size = ${oldList.size} and newList.size = ${newList.size}")
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

    fun release() {
        distanceProvider.unsubscribe(distanceChangedListener)
    }

    override fun onChangeState(server: Server, isFavourite: Boolean) {
        LOGGER.debug("On change state server = $server, isFavourite = $isFavourite")
        var position = filteredServers.indexOf(server)
        if (position >= 0) {
            notifyItemChanged(position, isFavourite)
        }

        position = servers.indexOf(server)
        servers[position].isFavourite = isFavourite
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AllServersRecyclerViewAdapter::class.java)

        private const val FASTEST_SERVER_ITEM = 0
        private const val SERVER_ITEM = 1
        private const val SEARCH_ITEM = 2
        private const val RANDOM_ITEM = 3
    }
}