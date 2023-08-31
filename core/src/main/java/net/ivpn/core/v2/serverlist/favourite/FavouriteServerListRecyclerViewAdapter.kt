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
import net.ivpn.core.databinding.ServerItemBinding
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.serverlist.AdapterListener
import net.ivpn.core.v2.serverlist.FavouriteServerListener
import net.ivpn.core.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.core.v2.serverlist.dialog.Filters
import net.ivpn.core.v2.serverlist.holders.HolderListener
import net.ivpn.core.v2.serverlist.holders.ServerViewHolder
import net.ivpn.core.v2.serverlist.items.ConnectionOption
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

    private var rawServers = arrayListOf<Server>()
    private var serversToDisplay = arrayListOf<Server>()
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
        return SERVER_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ServerItemBinding.inflate(layoutInflater, parent, false)
        return ServerViewHolder(binding, navigator)
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
        if (holder is ServerViewHolder) {
            val server: Server = getServerFor(position)
            bindings[holder.binding] = server
            setPing(holder.binding, server)
            holder.bind(server, forbiddenServer, isIPv6BadgeEnabled, filter)
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
        return serversToDisplay.size
    }

    private fun removeFavouriteServer(server: Server) {
        if (!rawServers.contains(server)) {
            return
        }
        rawServers.remove(server)
        applyFilter()
    }

    private fun addFavouriteServer(server: Server) {
        if (rawServers.contains(server)) {
            return
        }
        rawServers.add(server)
        applyFilter()
    }

    private fun setServers(servers: ArrayList<Server>) {
        rawServers = servers
        setDistances()
        setLatencies()
        applyFilter()
    }

    private fun sortServers(servers: ArrayList<Server>) {
        filter?.let {
            Collections.sort(servers, it.getServerComparator())
        } ?: run {
            Collections.sort(servers, Server.comparator)
        }
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
        setServers(ArrayList(items))
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
        }
        if (filter == Filters.LATENCY) {
            applyFilter()
        }
    }

    private fun setLatencies() {
        pings?.let{pingsObj ->
            rawServers.forEach {
                it.latency = pingsObj[it]?.ping ?: Long.MAX_VALUE
            }
        }
    }

    private fun setDistances() {
        val distances = distanceProvider.distances
        rawServers.forEach {
            it.distance = distances[it] ?: Float.MAX_VALUE
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
            val oldList = serversToDisplay
            serversToDisplay = ArrayList(rawServers)
            sortServers(serversToDisplay)
            notifyChanges(oldList, serversToDisplay)
        }
    }

    private fun getPositionFor(server: Server): Int {
        return serversToDisplay.indexOf(server)
    }

    private fun getServerFor(position: Int): Server {
        return serversToDisplay[position]
    }

    companion object {
        private const val SERVER_ITEM = 1
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