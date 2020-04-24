package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.prefs.ServerType
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.rest.data.model.Server
import javax.inject.Inject

class ServersViewModel @Inject constructor(val serversRepository: ServersRepository): ViewModel() {
    val entryServer = ObservableField<Server>()
    val exitServer = ObservableField<Server>()

    fun onResume() {
        entryServer.set(getCurrentServer(ServerType.ENTRY))
        exitServer.set(getCurrentServer(ServerType.EXIT))
    }

    private fun getCurrentServer(serverType: ServerType): Server? {
        return serversRepository.getCurrentServer(serverType)
    }
}