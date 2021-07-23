package net.ivpn.core.v2.viewmodel

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

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.core.common.dagger.ApplicationScope
import net.ivpn.core.common.multihop.MultiHopController
import net.ivpn.core.common.prefs.OnServerListUpdatedListener
import net.ivpn.core.rest.data.model.ServerType
import net.ivpn.core.common.prefs.ServersRepository
import net.ivpn.core.common.prefs.Settings
import net.ivpn.core.rest.data.model.Server
import net.ivpn.core.v2.dialog.Dialogs
import net.ivpn.core.v2.serverlist.AdapterListener
import net.ivpn.core.v2.serverlist.FavouriteServerListener
import javax.inject.Inject

@ApplicationScope
class ServerListViewModel @Inject constructor(
        val settings: Settings,
        val serversRepository: ServersRepository,
        val multiHopController: MultiHopController
) : ViewModel() {

    val all = ObservableArrayList<Server>()
    val favourites = ObservableArrayList<Server>()
    val forbiddenServer = ObservableField<Server>()

    val dataRefreshing = ObservableBoolean()
    val dataLoading = ObservableBoolean()
    val navigators = arrayListOf<ServerListNavigator>()
    val favouriteListeners = arrayListOf<FavouriteServerListener>()

    val adapterListener = object : AdapterListener {
        override fun onServerLongClick(server: Server) {
            changeFavouriteState(server, !server.isFavourite)
        }

        override fun onRandomServerSelected() {
//            val availableServers = all.filter { it != forbiddenServer }

//            setCurrentServer(availableServers.random())
            setSettingRandomServer()
            if (navigators.isNotEmpty()) {
                navigators[0].onServerSelected()
            }
        }

        override fun onFastestServerSettingsClick() {
            if (navigators.isNotEmpty()) {
                navigators[0].openFastestSetting()
            }
        }

        override fun onServerSelected(server: Server, forbiddenServer: Server?) {
            if (server.canBeUsedAsMultiHopWith(forbiddenServer)) {
                setCurrentServer(server)
                if (navigators.isNotEmpty()) {
                    navigators[0].onServerSelected()
                }
            } else {
                if (navigators.isNotEmpty()) {
                    navigators[0].showDialog(Dialogs.INCOMPATIBLE_SERVERS)
                }
            }
        }

        override fun changeFavouriteStateFor(server: Server, isFavourite: Boolean) {
            changeFavouriteState(server, isFavourite)
        }

        override fun onFastestServerSelected() {
            setSettingFastestServer()
            if (navigators.isNotEmpty()) {
                navigators[0].onServerSelected()
            }
        }
    }

    private var listener: OnServerListUpdatedListener = object : OnServerListUpdatedListener {
        override fun onSuccess(servers: List<Server>, isForced: Boolean) {
            dataRefreshing.set(false)
            dataLoading.set(false)
            all.clear()
            all.addAll(servers)
            applyFavourites()
        }

        override fun onError(throwable: Throwable) {
            dataRefreshing.set(false)
        }

        override fun onError() {
            dataRefreshing.set(false)
        }
    }
    private var serverType: ServerType? = null

    init {
        serversRepository.addOnServersListUpdatedListener(listener)
    }

    fun reset() {
    }

    fun setServerType(serverType: ServerType?) {
        this.serverType = serverType
    }

    fun setCurrentServer(server: Server?) {
        serverType?.let {
            serversRepository.serverSelected(server, it)
        }
    }

    fun start(serverType: ServerType?) {
        if (serverType == null) return

        this.serverType = serverType
        forbiddenServer.set(getForbiddenServer(serverType))
        favourites.clear()
        favourites.addAll(serversRepository.getFavouritesServers())

        if (isServersListExist()) {
            getCachedServersList()?.let {
                all.clear()
                all.addAll(it)
                applyFavourites()
            }
        } else {
            loadServers(false)
        }
    }

    fun loadServers(isRefreshing: Boolean) {
        if (isRefreshing) {
            dataRefreshing.set(true)
        } else {
            dataLoading.set(true)
        }
        serversRepository.updateServerList(isRefreshing)
    }

    fun cancel() {
        dataLoading.set(false)
        serversRepository.removeOnServersListUpdatedListener(listener)
    }

    private fun applyFavourites() {
        for (server in all) {
            server.isFavourite = favourites.contains(server)
        }
        for (server in favourites) {
            server.isFavourite = true
        }
    }

    private fun addToFavourites(server: Server) {
        serversRepository.addFavouritesServer(server)
        for (listener in favouriteListeners) {
            listener.onChangeState(server, true)
        }
    }

    private fun removeFromFavourites(server: Server) {
        serversRepository.removeFavouritesServer(server)
        for (listener in favouriteListeners) {
            listener.onChangeState(server, false)
        }
    }

    fun changeFavouriteState(server: Server, isFavourite: Boolean) {
        if (isFavourite) {
            addToFavourites(server)
        } else {
            removeFromFavourites(server)
        }
    }

    fun setSettingFastestServer() {
        serversRepository.fastestServerSelected()
    }

    fun setSettingRandomServer() {
        serverType?.let {
            serversRepository.randomServerSelected(it)
        }
    }

    fun isFastestServerAllowed(): Boolean {
//        return !settings.isMultiHopEnabled
        return !multiHopController.getIsEnabled()
    }

    private fun getCachedServersList(): List<Server>? {
        return serversRepository.getServers(false)
    }

    private fun getForbiddenServer(serverType: ServerType): Server? {
        return serversRepository.getForbiddenServer(serverType)
    }

    private fun isServersListExist(): Boolean {
        return serversRepository.isServersListExist()
    }

    interface ServerListNavigator {
        fun onServerSelected()

        fun navigateBack()

        fun showDialog(dialogs: Dialogs)

        fun openFastestSetting()
    }
}