package net.ivpn.client.v2.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.prefs.ServersRepository
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.rest.HttpClientFactory
import net.ivpn.client.rest.IVPNApi
import net.ivpn.client.rest.RequestListener
import net.ivpn.client.rest.data.proofs.LocationResponse
import net.ivpn.client.rest.requests.common.Request
import net.ivpn.client.vpn.Protocol
import org.slf4j.LoggerFactory
import javax.inject.Inject

class LocationViewModel @Inject constructor(
        settings: Settings,
        httpClientFactory: HttpClientFactory,
        serversRepository: ServersRepository
) : ViewModel()  {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LocationViewModel::class.java)
    }

    val dataLoading = ObservableBoolean()

    val ip = ObservableField<String>()
    val location = ObservableField<String>()
    val isp = ObservableField<String>()

    private var request: Request<LocationResponse> = Request(settings, httpClientFactory, serversRepository, Request.Duration.SHORT)

    fun checkLocation() {
        dataLoading.set(true)
        request.start({ obj: IVPNApi -> obj.location }, object : RequestListener<LocationResponse?> {
            override fun onSuccess(response: LocationResponse?) {
                LOGGER.info(response.toString())
                this@LocationViewModel.onSuccess(response)
            }

            override fun onError(throwable: Throwable) {
                LOGGER.error("Error while updating location ", throwable)
                this@LocationViewModel.onError()
            }

            override fun onError(string: String) {
                LOGGER.error("Error while updating location ", string)
                this@LocationViewModel.onError()
            }
        })
    }

    private fun onSuccess(response: LocationResponse?) {
        dataLoading.set(false)
        response?.let {
            ip.set(it.ipAddress)
            if (it.city != null && it.city.isNotEmpty()) {
                location.set(it.getLocation())
            } else {
                location.set(it.country)
            }
            isp.set(it.isp)
        }
    }

    private fun onError() {
        dataLoading.set(false)
    }
}