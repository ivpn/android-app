package net.ivpn.client.ui.dialog;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.IVPNApi;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.data.proofs.LocationResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class LocationDialogViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDialogViewModel.class);

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean error = new ObservableBoolean();
    public final ObservableBoolean connected = new ObservableBoolean();
    public final ObservableField<String> ip = new ObservableField<>();
    public final ObservableField<String> location = new ObservableField<>();
    public final ObservableField<String> countryCode = new ObservableField<>();
    public final ObservableField<Protocol> protocol = new ObservableField<>();
    public final ObservableField<String> privateIP = new ObservableField<>();

    private Request<LocationResponse> request;
    private Settings settings;
    private ProtocolController protocolController;
    public VpnBehaviorController vpnBehaviorController;

    @Inject
    LocationDialogViewModel(Settings settings, HttpClientFactory httpClientFactory,
                            ServersRepository serversRepository, ProtocolController protocolController,
                            VpnBehaviorController vpnBehaviorController) {
        this.settings = settings;
        this.protocolController = protocolController;
        this.vpnBehaviorController = vpnBehaviorController;

        connected.set(vpnBehaviorController.getConnectionTime() != -1);
        request = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.SHORT);
    }

    public void start() {
        dataLoading.set(true);
        request.start(IVPNApi::getLocation, new RequestListener<LocationResponse>() {
            @Override
            public void onSuccess(LocationResponse response) {
                LOGGER.info(response.toString());
                LocationDialogViewModel.this.onSuccess(response);
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Error while updating location ", throwable);
                LocationDialogViewModel.this.onError();
            }

            @Override
            public void onError(String string) {
                LOGGER.error("Error while updating location ", string);
                LocationDialogViewModel.this.onError();
            }
        });
    }

    public void retry() {
        error.set(false);
        start();
    }

    private void onSuccess(LocationResponse response) {
        dataLoading.set(false);
        ip.set(response.getIpAddress());
        if (response.getCity() != null && !response.getCity().isEmpty()) {
            location.set(response.getCountry() + ",  " + response.getCity());
        } else {
            location.set(response.getCountry());
        }
        countryCode.set(response.getCountryCode());
        protocol.set(protocolController.getCurrentProtocol());
        privateIP.set(settings.getWireGuardIpAddress());
    }

    private void onError() {
        dataLoading.set(false);
        error.set(true);
    }
}