package net.ivpn.client.v2.serverlist

import net.ivpn.client.rest.data.model.Server

interface FavouriteServerListener {

    fun onChangeState(server: Server, isFavourite: Boolean)

}