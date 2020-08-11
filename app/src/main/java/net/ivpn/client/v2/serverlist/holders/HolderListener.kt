package net.ivpn.client.v2.serverlist.holders

import net.ivpn.client.rest.data.model.Server

interface HolderListener {

    fun invalidate(server: Server)
}