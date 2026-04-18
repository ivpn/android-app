package net.ivpn.core.v2.serverlist.favourite

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.core.IVPNApplication
import net.ivpn.core.R
import net.ivpn.core.common.distance.DistanceProvider
import net.ivpn.core.common.distance.OnDistanceChangedListener
import net.ivpn.core.common.pinger.OnPingFinishListener
import net.ivpn.core.common.pinger.PingProvider
import net.ivpn.core.common.pinger.PingResultFormatter
import net.ivpn.core.databinding.HostItemBinding
import net.ivpn.core.databinding.ServerItemBinding
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.serverlist.AdapterListener
import net.ivpn.core.v2.serverlist.FavouriteServerListener
import net.ivpn.core.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.core.v2.serverlist.dialog.Filters
import net.ivpn.core.v2.serverlist.holders.HolderListener
import net.ivpn.core.v2.serverlist.holders.HostViewHolder
import net.ivpn.core.v2.serverlist.holders.ServerViewHolder
import net.ivpn.core.v2.serverlist.items.ConnectionOption
import net.ivpn.core.common.nightmode.OledModeController
import net.ivpn.core.v2.serverlist.items.HostItem
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.ArrayList

class FavouriteServerListRecyclerViewAdapter(
        private val navigator: AdapterListener,
        private var filter: Filters?,
        private var isIPv6BadgeEnabled: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServerBasedRecyclerViewAdapter, FavouriteServerListener {

    @Inject
    lateinit var distanceProvider: DistanceProvider

    private var bindings = HashMap<ServerItemBinding, Server>()
    private var hostBindings = HashMap<HostItemBinding, HostItem>()

    private var rawItems = arrayListOf<ConnectionOption>()
    private var itemsToDisplay = arrayListOf<ConnectionOption>()
    private var forbiddenServer: Server? = null

    val distanceChangedListener = object : OnDistanceChangedListener {
        override fun onDistanceChanged() {
            if (filter == Filters.DISTANCE) {
                setDistances()
                applyFilter()
            }
        }
    }

    init {
        IVPNApplication.appComponent.provideActivityComponent().create().inject(this)
        distanceProvider.subscribe(distanceChangedListener)
    }

    private var pings: Map<Server, PingResultFormatter?>? = null

    override fun getItemViewType(position: Int): Int {
        return when (itemsToDisplay.getOrNull(position)) {
            is HostItem -> HOST_ITEM
            else -> SERVER_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOST_ITEM -> {
                val binding = HostItemBinding.inflate(layoutInflater, parent, false)
                HostViewHolder(binding, navigator)
            }
            else -> {
                val binding = ServerItemBinding.inflate(layoutInflater, parent, false)
                ServerViewHolder(binding, navigator)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
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
        val item = getItemFor(position)
        when {
            holder is ServerViewHolder && item is Server -> {
                bindings[holder.binding] = item
                setPing(holder.binding, item)
                holder.bind(item, forbiddenServer, isIPv6BadgeEnabled, filter, false, false, isFavouritesEntry = true)
            }
            holder is HostViewHolder && item is HostItem -> {
                hostBindings[holder.binding] = item
                setPingForHost(holder.binding, item)
                holder.bind(item, forbiddenServer, isFavouritesEntry = true, isIPv6BadgeEnabled = isIPv6BadgeEnabled)
            }
        }
        // Apply OLED colors to recycled/new items
        OledModeController.applyOledToViewTree(holder.itemView)
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

    private fun setPingForHost(binding: HostItemBinding, hostItem: HostItem) {
        pings?.let {
            it[hostItem.parentServer]?.also { formatter ->
                binding.pingstatus = formatter
            } ?: run {
                binding.pingstatus = null
            }
        } ?: run {
            binding.pingstatus = null
        }
    }

    override fun getItemCount(): Int {
        return itemsToDisplay.size
    }

    private fun getItemFor(position: Int): ConnectionOption {
        return itemsToDisplay[position]
    }

    private fun removeFavouriteServer(server: Server) {
        if (!rawItems.contains(server)) return
        rawItems.remove(server)
        applyFilter()
    }

    private fun addFavouriteServer(server: Server) {
        if (rawItems.contains(server)) return
        rawItems.add(server)
        applyFilter()
    }

    /**
     * Replaces the list with servers and/or host items (ConnectionOption).
     * Used by the favouriteItems binding.
     */
    fun replaceDataConnectionOptions(items: List<ConnectionOption>) {
        rawItems = ArrayList(items)
        setDistances()
        setLatencies()
        applyFilter()
    }

    private fun setServers(servers: ArrayList<Server>) {
        rawItems = ArrayList(servers)
        setDistances()
        setLatencies()
        applyFilter()
    }

    private fun notifyChanges(oldList: List<ConnectionOption>, newList: List<ConnectionOption>) {
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

    override fun setForbiddenServer(server: Server?) {
        forbiddenServer = server
    }

    override fun replaceData(items: List<Server>) {
        replaceDataConnectionOptions(ArrayList(items))
    }

    override fun setFilter(filter: Filters?) {
        this.filter = filter
        for ((binding, _) in bindings) {
            binding.filter = filter
            binding.executePendingBindings()
        }
        if (filter == Filters.DISTANCE) {
            setDistances()
        }
        applyFilter()
    }

    override fun setPings(pings: Map<Server, PingResultFormatter?>) {
        this.pings = pings
        setLatencies()

        bindings.forEach {(binding, server) ->
            setPing(binding, server)
            binding.executePendingBindings()
        }
        hostBindings.forEach {(binding, hostItem) ->
            setPingForHost(binding, hostItem)
            binding.executePendingBindings()
        }
        if (filter == Filters.LATENCY) {
            applyFilter()
        }
    }

    private fun setLatencies() {
        pings?.let { pingsObj ->
            rawItems.filterIsInstance<Server>().forEach {
                it.latency = pingsObj[it]?.ping ?: Long.MAX_VALUE
            }
            rawItems.filterIsInstance<HostItem>().forEach {
                it.parentServer.latency = pingsObj[it.parentServer]?.ping ?: Long.MAX_VALUE
            }
        }
    }

    private fun setDistances() {
        val distances = distanceProvider.distances
        rawItems.filterIsInstance<Server>().forEach {
            it.distance = distances[it] ?: Float.MAX_VALUE
        }
        rawItems.filterIsInstance<HostItem>().forEach {
            it.parentServer.distance = distances[it.parentServer] ?: Float.MAX_VALUE
        }
    }

    @Volatile
    var isUpdating = false
    val updateHandler = Handler(Looper.getMainLooper())
    val updateInterval = 500L

    private fun applyFilter() {
        if (isUpdating) {
            updateHandler.postDelayed({
                isUpdating = false
                applyFilter()
            }, updateInterval)
        } else {
            isUpdating = true
            val oldList = itemsToDisplay
            val allItems = ArrayList<ConnectionOption>(rawItems)
            filter?.let {
                Collections.sort(allItems, it.getConnectionOptionComparator())
            } ?: run {
                Collections.sort(allItems) { a, b ->
                    val serverA = when (a) {
                        is Server -> a
                        is HostItem -> a.parentServer
                        else -> return@sort 0
                    }
                    val serverB = when (b) {
                        is Server -> b
                        is HostItem -> b.parentServer
                        else -> return@sort 0
                    }
                    Server.comparator.compare(serverA, serverB)
                }
            }
            itemsToDisplay = allItems
            notifyChanges(oldList, itemsToDisplay)
        }
    }

    private fun getPositionFor(server: Server): Int {
        return itemsToDisplay.indexOf(server)
    }

    private fun getServerFor(position: Int): Server {
        return getItemFor(position) as Server
    }

    companion object {
        private const val SERVER_ITEM = 1
        private const val HOST_ITEM = 2
    }

    fun release() {
        distanceProvider.unsubscribe(distanceChangedListener)
    }

    override fun onChangeState(server: Server, isFavourite: Boolean) {
        if (isFavourite) {
            addFavouriteServer(server)
        } else {
            removeFavouriteServer(server)
        }
    }
}