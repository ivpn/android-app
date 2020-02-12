package net.ivpn.client.ui.network;

import androidx.databinding.ObservableField;

import net.ivpn.client.vpn.model.NetworkState;
import net.ivpn.client.vpn.model.WifiItem;
import net.ivpn.client.vpn.local.NetworkController;

import javax.inject.Inject;

public class WifiItemViewModel{

    public ObservableField<WifiItem> wifiItem = new ObservableField<>();
    public final ObservableField<NetworkState> currentState = new ObservableField<>();
    public final ObservableField<NetworkState> defaultState = new ObservableField<>();

    private NetworkController networkController;

    public OnNetworkBehaviourChangedListener listener = state -> {
        if (wifiItem.get() != null) {
            networkController.changeMarkFor(wifiItem.get().getSsid(), currentState.get(), state);
        }
        wifiItem.get().setNetworkState(state);
        currentState.set(state);
    };

    @Inject
    WifiItemViewModel(NetworkController networkController) {
        this.networkController = networkController;
    }

    public void setWifiItem(WifiItem wifiItem) {
        this.wifiItem.set(wifiItem);
        currentState.set(wifiItem.getNetworkState());
    }

    public void setDefaultState(NetworkState defaultState) {
        this.defaultState.set(defaultState);
    }

    public void setCurrentState(NetworkState currentState) {
        this.currentState.set(currentState);
    }

    public String getTitle() {
        return wifiItem.get().getTitle();
    }

}