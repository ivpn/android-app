package net.ivpn.client.v2.serverlist

import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.v2.serverlist.dialog.Filters

interface ServerBasedRecyclerViewAdapter {

    fun setForbiddenServer(server: Server?)

    fun replaceData(items: List<Server>)

    fun setFilter(filter: Filters?)
}