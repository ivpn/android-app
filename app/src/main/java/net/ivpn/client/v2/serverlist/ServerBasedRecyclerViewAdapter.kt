package net.ivpn.client.v2.serverlist

import net.ivpn.client.rest.data.model.Server

interface ServerBasedRecyclerViewAdapter {

    fun setForbiddenServer(server: Server?)

    fun replaceData(items: List<Server>)
}