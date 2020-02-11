package net.ivpn.client.ui.network;

import android.databinding.ObservableField;

import net.ivpn.client.vpn.local.NetworkController;
import net.ivpn.client.vpn.model.NetworkState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import javax.inject.Inject;

public class CommonBehaviourItemViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBehaviourItemViewModel.class);

    public final ObservableField<NetworkState> mobileDataState = new ObservableField<>();
    public final ObservableField<NetworkState> defaultState = new ObservableField<>();
    public OnDefaultBehaviourChanged navigator;
    public OnNetworkBehaviourChangedListener mobileDataStateListener = new OnNetworkBehaviourChangedListener() {
        @Override
        public void onNetworkBehaviourChanged(NetworkState state) {
            if (mobileDataState != null && Objects.equals(mobileDataState.get(), state)) {
                return;
            }
            LOGGER.info("Set mobile network state as " + state);
            mobileDataState.set(state);
            if (navigator != null) {
                navigator.onMobileDataBehaviourChanged(state);
            }
            networkController.updateMobileDataState(state);
        }
    };
    public OnNetworkBehaviourChangedListener defaultStateListener = new OnNetworkBehaviourChangedListener() {
        @Override
        public void onNetworkBehaviourChanged(NetworkState state) {
            LOGGER.info("Set default state as " + state);
            defaultState.set(state);
            if (navigator != null) {
                navigator.onDefaultBehaviourChanged(state);
            }
            networkController.updateDefaultNetworkState(state);
        }
    };

    private NetworkController networkController;

    @Inject
    CommonBehaviourItemViewModel(NetworkController networkController) {
        this.networkController = networkController;
    }

    public void setNavigator(OnDefaultBehaviourChanged navigator) {
        this.navigator = navigator;
    }

    public void setDefaultState(NetworkState defaultState) {
        this.defaultState.set(defaultState);
    }

    public void setMobileDataState(NetworkState mobileDataState) {
        this.mobileDataState.set(mobileDataState);
    }

    public interface OnDefaultBehaviourChanged {
        void onDefaultBehaviourChanged(NetworkState defaultState);

        void onMobileDataBehaviourChanged(NetworkState mobileDataState);
    }
}