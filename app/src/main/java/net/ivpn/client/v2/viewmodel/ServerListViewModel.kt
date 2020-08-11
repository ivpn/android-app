package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.OnServerListUpdatedListener
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.data.model.Server
import net.ivpn.client.ui.dialog.Dialogs
import net.ivpn.client.ui.serverlist.AdapterListener
import net.ivpn.client.v2.serverlist.FavouriteServerListener
import javax.inject.Inject

@ApplicationScope
class ServerListViewModel @Inject constructor(
        val settings: Settings,
        val serversRepository: ServersRepository
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
            val availableServers = all.filter { it != forbiddenServer }

            setCurrentServer(availableServers.random())
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
        if (serverType != null) {
            serversRepository.serverSelected(server, serverType)
        }
    }

    fun start(serverType: ServerType?) {
        this.serverType = serverType
        forbiddenServer.set(getForbiddenServer(serverType))
        favourites.clear()
        favourites.addAll(serversRepository.favouritesServers)
        if (isServersListExist()) {
            all.clear()
            all.addAll(getCachedServersList())
            applyFavourites()
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

    fun isFastestServerAllowed(): Boolean {
        return !settings.isMultiHopEnabled
    }

    private fun getCachedServersList(): List<Server> {
        return serversRepository.getServers(false)
    }

    private fun getForbiddenServer(serverType: ServerType?): Server? {
        return serversRepository.getForbiddenServer(serverType)
    }

    private fun isServersListExist(): Boolean {
        return serversRepository.isServersListExist
    }

    interface ServerListNavigator {
        fun navigateBack()

        fun onServerSelected()

        fun showDialog(dialogs: Dialogs)

        fun openFastestSetting()
    }
}