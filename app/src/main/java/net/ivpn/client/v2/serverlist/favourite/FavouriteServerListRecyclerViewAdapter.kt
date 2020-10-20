package net.ivpn.client.v2.serverlist.favourite

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.ivpn.client.IVPNApplication
import net.ivpn.client.R
import net.ivpn.client.common.pinger.OnPingFinishListener
import net.ivpn.client.common.pinger.PingProvider
import net.ivpn.client.common.pinger.PingResultFormatter
import net.ivpn.client.databinding.ServerItemBinding
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.serverlist.AdapterListener
import net.ivpn.client.v2.serverlist.FavouriteServerListener
import net.ivpn.client.v2.serverlist.ServerBasedRecyclerViewAdapter
import net.ivpn.client.v2.serverlist.all.AllServersRecyclerViewAdapter
import net.ivpn.client.v2.serverlist.dialog.Filters
import net.ivpn.client.v2.serverlist.holders.HolderListener
import net.ivpn.client.v2.serverlist.holders.ServerViewHolder
import net.ivpn.client.v2.serverlist.items.ConnectionOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.ArrayList

class FavouriteServerListRecyclerViewAdapter(
        private val navigator: AdapterListener,
        private var filter: Filters?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ServerBasedRecyclerViewAdapter, FavouriteServerListener {

    @Inject
    lateinit var pingProvider: PingProvider

    private var bindings = HashMap<ServerItemBinding, Server>()

    private var rawServers = arrayListOf<Server>()
    private var serversToDisplay = arrayListOf<Server>()
    private var forbiddenServer: Server? = null

    init {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this)
    }

    var listener: HolderListener = object : HolderListener {
        override fun invalidate(server: Server) {
            notifyItemChanged(getPositionFor(server))
        }
    }

    private var pings: ConcurrentHashMap<Server, PingResultFormatter>? = null
    @Volatile
    private var updates = HashMap<Server, PingResultFormatter>()
    @Volatile
    private var iterationUpdates = ConcurrentHashMap<Server, PingResultFormatter>()
    private var pingListener = OnPingFinishListener { server, status ->
//        AllServersRecyclerViewAdapter.LOGGER.debug("Server = ${server.city}, status = ${status}")
        synchronized(this) {
            updates[server] = status
        }
        filter?.let {
//            if (it == Filters.LATENCY) {
//                AllServersRecyclerViewAdapter.LOGGER.debug("isUpdateRunning = $isUpdateRunning needToUpdate = $needToUpdate")
                if (isUpdateRunning) {
                    needToUpdate = true
                } else {
                    isUpdateRunning = true
                    postUpdate(0)
                }
//            }
        }
    }

    @Volatile
    private var needToUpdate = false
    @Volatile
    private var isUpdateRunning = false
    private val updateHandler = Handler()
    private fun postUpdate(delay: Long) {
//        AllServersRecyclerViewAdapter.LOGGER.debug("Post update delay = $delay")
        updateHandler.postDelayed({
            iterationUpdates.clear()
            synchronized(this) {
                if (updates.isNotEmpty()) {
//                    AllServersRecyclerViewAdapter.LOGGER.debug("Updates size = ${updates.size}")
                    pings?.let { pingsObj ->
                        for ((server, status) in updates) {
                            pingsObj[server] = status
                            serversToDisplay[serversToDisplay.indexOf(server)].latency = status.ping
                            iterationUpdates[server] = status
                        }
                    }
                    updates.clear()
                }
            }

            sortServers(rawServers)
//            for (server in servers) {
//                AllServersRecyclerViewAdapter.LOGGER.debug("After sort city = ${server.city} has ${server.latency}")
//            }
//            filteredServers = prepareDataToShow(servers)
            notifyServerListChanged()
            if (needToUpdate) {
                postUpdate(1000)
                needToUpdate = false
            } else {
                isUpdateRunning = false
                iterationUpdates.clear()
            }
//            iterationUpdates.forEach { AllServersRecyclerViewAdapter.LOGGER.debug("Iteration updates item = ${it.key.city}") }
        }, delay)
    }

    override fun getItemViewType(position: Int): Int {
        return SERVER_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ServerItemBinding.inflate(layoutInflater, parent, false)
        return ServerViewHolder(binding, navigator, listener)
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
            holder.bind(server, forbiddenServer)
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
//        val position = getPositionFor(server)
        rawServers.remove(server)
        notifyServerListChanged()
    }

    private fun addFavouriteServer(server: Server) {
        if (rawServers.contains(server)) {
            return
        }
        rawServers.add(server)
        sortServers(rawServers)
        notifyServerListChanged()
    }

    private fun setServers(servers: ArrayList<Server>) {
        updatePings(servers)
        sortServers(servers)
        rawServers = servers
        notifyServerListChanged()
    }

    private fun sortServers(servers: ArrayList<Server>) {
        filter?.let {
            Collections.sort(servers, it.getServerComparator())
        } ?: run {
            Collections.sort(servers, Server.comparator)
        }
    }

    private fun updatePings(servers: ArrayList<Server>) {
        for ((binding, server) in bindings) {
            binding.pingstatus = null
            binding.executePendingBindings()
        }
        pings = pingProvider.pingResults?.also { pingsObj ->
            for (server in servers) {
                pingsObj[server]?.also {
                    server.latency = it.ping
                } ?: run {
                    pingProvider.ping(server, pingListener)
                }
            }
        }
    }

    private fun notifyServerListChanged() {
        val oldList = serversToDisplay
        serversToDisplay = ArrayList(rawServers)
        notifyChanges(oldList, serversToDisplay)
    }

    private fun notifyChanges(oldList: List<ConnectionOption>, newList: List<ConnectionOption>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val item2 = newList[newItemPosition]
                if (item2 is Server) {
                    return !iterationUpdates.containsKey(item2)
                }
//                LOGGER.debug("Result = ${oldList[oldItemPosition] == newList[newItemPosition]}")
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
        sortServers(rawServers)
        notifyServerListChanged()
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

    override fun onChangeState(server: Server, isFavourite: Boolean) {
        if (isFavourite) {
            addFavouriteServer(server)
        } else {
            removeFavouriteServer(server)
        }
    }
}