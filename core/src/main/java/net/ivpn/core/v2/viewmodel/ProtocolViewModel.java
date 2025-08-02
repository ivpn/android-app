package net.ivpn.core.v2.viewmodel;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import net.ivpn.core.R;
import net.ivpn.core.common.Mapper;
import net.ivpn.core.common.multihop.MultiHopController;
import net.ivpn.core.common.prefs.Settings;
import net.ivpn.core.rest.Responses;
import net.ivpn.core.rest.data.model.Port;
import net.ivpn.core.rest.data.session.SessionErrorResponse;
import net.ivpn.core.v2.protocol.ProtocolNavigator;
import net.ivpn.core.v2.dialog.Dialogs;
import net.ivpn.core.v2.protocol.dialog.WireGuardInfo;
import net.ivpn.core.v2.protocol.port.OnPortSelectedListener;
import net.ivpn.core.common.views.valueSelector.OnValueChangeListener;
import net.ivpn.core.vpn.Protocol;
import net.ivpn.core.vpn.ProtocolController;
import net.ivpn.core.vpn.controller.VpnBehaviorController;
import net.ivpn.core.vpn.controller.WireGuardKeyController;
import net.ivpn.core.vpn.controller.WireGuardKeyController.WireGuardKeysEventsListener;
import net.ivpn.core.vpn.model.ObfuscationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

import javax.inject.Inject;


public class ProtocolViewModel {

    private static final String TAG = ProtocolViewModel.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolViewModel.class);

    public ObservableBoolean dataLoading = new ObservableBoolean();
    public ObservableField<String> loadingMessage = new ObservableField<>();
    public ObservableField<Protocol> protocol = new ObservableField<>();
    public ObservableField<Port> openVPNPort = new ObservableField<>();
    public ObservableField<Port> wireGuardPort = new ObservableField<>();
    public ObservableField<String> regenerationPeriod = new ObservableField<>();
    public ObservableField<MultiHopController> multiHop = new ObservableField<>();

    public ObservableField<WireGuardInfo> wgInfo = new ObservableField<>();
    public ObservableField<ObfuscationType> obfuscationType = new ObservableField<>();

    private ProtocolNavigator navigator;
    private String wireGuardPublicKey;
    private Context context;
    private WireGuardKeyController keyController;
    private Settings settings;
    private ProtocolController protocolController;
    private VpnBehaviorController vpnBehaviorController;
    private MultiHopController multiHopController;

    public RadioGroup.OnCheckedChangeListener obfuscationCheckedChangeListener = (group, checkedId) -> {
        ObfuscationType type = ObfuscationType.DISABLED;
        if (checkedId == R.id.obfuscation_v2ray_quic) {
            type = ObfuscationType.V2RAY_QUIC;
        } else if (checkedId == R.id.obfuscation_v2ray_tcp) {
            type = ObfuscationType.V2RAY_TCP;
        }

        setObfuscationType(type);
    };

    private void setObfuscationType(ObfuscationType type) {
        obfuscationType.set(type);
        settings.setObfuscationType(type);
        
        if (protocol.get().equals(Protocol.WIREGUARD)) {
            validateAndUpdateCurrentPort();
        }
    }
    
    private void validateAndUpdateCurrentPort() {
        Port currentPort = wireGuardPort.get();
        List<Port> availablePorts = getAvailablePortsForCurrentObfuscationType();
        
        boolean portExists = availablePorts.stream()
                .anyMatch(port -> port.equals(currentPort));
        
        if (!portExists) {
            if (!availablePorts.isEmpty()) {
                wireGuardPort.set(availablePorts.get(0));
                settings.setWireGuardPort(availablePorts.get(0));
            }
        }
    }
    
    private List<Port> getAvailablePortsForCurrentObfuscationType() {
        List<Port> ports = new ArrayList<>();
        ports.addAll(settings.getWireGuardPorts());
        
        // Add custom ports based on current obfuscation type
        switch (obfuscationType.get()) {
            case V2RAY_TCP:
                ports.addAll(settings.getWireGuardCustomPortsV2RayTcp());
                break;
            case V2RAY_QUIC:
                ports.addAll(settings.getWireGuardCustomPortsV2RayUdp());
                break;
            case DISABLED:
                ports.addAll(settings.getWireGuardCustomPorts());
                break;
        }
        
        return ports;
    }
    public CompoundButton.OnCheckedChangeListener openVPNCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked && protocol.get().equals(Protocol.WIREGUARD)) {
            setProtocol(Protocol.OPENVPN);
        }
    };
    public CompoundButton.OnCheckedChangeListener wireGuardCheckedChangeListener = (buttonView, isChecked) -> {
        Log.d(TAG, "wireguardCheckedChangeListener: ");
        if (protocol.get() == null) {
            return;
        }
        if (isChecked && protocol.get().equals(Protocol.OPENVPN)) {
            protocol.set(null);
            tryEnableWgProtocol();
        }
    };

    public OnValueChangeListener listener = value -> {
        regenerationPeriod.set(String.valueOf(value));
        keyController.putRegenerationPeriod(value);
    };

    public OnPortSelectedListener onPortChangedListener = this::setPort;
    @SuppressLint("ClickableViewAccessibility")
    public View.OnTouchListener portsTouchListener = (view, motionEvent) -> {
        if (isVpnActive()) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                navigator.notifyUser(R.string.snackbar_to_change_port_disconnect_first_msg,
                        R.string.snackbar_disconnect_first, null);
            }
            return true;
        }
        if (multiHopController.isEnabled() && protocol.get().equals(Protocol.WIREGUARD)) {
            // Allow port changes when V2Ray obfuscation is enabled
            ObfuscationType currentObfuscationType = settings.getObfuscationType();
            boolean isV2RayEnabled = currentObfuscationType != ObfuscationType.DISABLED;
            if (!isV2RayEnabled) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    navigator.openNotifyDialogue(Dialogs.WG_CANT_CHANGE_PORT);
                }
                return true;
            }
        }
        return false;
    };

    @Inject
    ProtocolViewModel(Context context, Settings settings, WireGuardKeyController keyController,
                      ProtocolController protocolController, VpnBehaviorController vpnBehaviorController,
                      MultiHopController multiHopController) {
        this.context = context;
        this.settings = settings;
        this.keyController = keyController;
        this.protocolController = protocolController;
        this.vpnBehaviorController = vpnBehaviorController;
        this.multiHopController = multiHopController;

        this.keyController.setKeysEventsListener(getWireGuardKeysEventsListener());
        init();
    }

    private void init() {

        protocol.set(protocolController.getCurrentProtocol());
        openVPNPort.set(settings.getOpenVpnPort());
        wireGuardPort.set(settings.getWireGuardPort());

        wireGuardPublicKey = settings.getWireGuardPublicKey();
        regenerationPeriod.set(String.valueOf(keyController.getRegenerationPeriod()));

        obfuscationType.set(settings.getObfuscationType());

        wgInfo.set(getWireGuardInfo());
        multiHop.set(multiHopController);
    }

    public void reset() {
        init();
    }

    public void setNavigator(ProtocolNavigator navigator) {
        this.navigator = navigator;
    }

    public void copyWgKeyToClipboard(ClipboardManager clipboard) {
        ClipData clip = ClipData.newPlainText("wireguard_public_key", wireGuardPublicKey);
        clipboard.setPrimaryClip(clip);
    }

    public void copyWgIpToClipboard(ClipboardManager clipboard) {
        ClipData clip = ClipData.newPlainText("wireguard_ip", settings.getWireGuardIpAddress());
        clipboard.setPrimaryClip(clip);
    }

    public WireGuardInfo getWireGuardInfo() {
        String ipAddress = settings.getWireGuardIpAddress();
        String presharedKey = settings.getWireGuardPresharedKey();

        long regenerationPeriod = keyController.getRegenerationPeriod();
        long lastGeneratedTime = settings.getGenerationTime();

        return new WireGuardInfo(wireGuardPublicKey, ipAddress, presharedKey, lastGeneratedTime, regenerationPeriod);
    }

    public String getDescription() {
        if (protocol.get().equals(Protocol.WIREGUARD)) {
            return "WireGuard" + ", " + wireGuardPort.get().toThumbnailWithObfuscation(obfuscationType.get());
        } else {
            return "OpenVPN" + ", " + openVPNPort.get().toThumbnail();
        }
    }

    public String getDescriptionMultihop() {
        if (protocol.get().equals(Protocol.WIREGUARD)) {
            return "WireGuard" + ", " + wireGuardPort.get().toThumbnailWithObfuscation(obfuscationType.get());
        } else {
            return "OpenVPN" + ", " + openVPNPort.get().toThumbnail();
        }
    }

    private void tryEnableWgProtocol() {
        LOGGER.info("Try to enable WireGuard protocol");
        if (wireGuardPublicKey == null || wireGuardPublicKey.isEmpty()) {
            keyController.generateKeys();
            return;
        }

        setProtocol(Protocol.WIREGUARD);
    }

    public void reGenerateKeys() {
        LOGGER.info(TAG, "Regenerate keys");
        keyController.regenerateKeys();
    }

    private void setProtocol(Protocol protocol) {
        LOGGER.info(TAG, "Set protocol: " + protocol);
        this.protocol.set(protocol);
        protocolController.setCurrentProtocol(protocol);
    }

    void setPort(Port port) {
        LOGGER.info(TAG, "Set port: " + port);
        if (protocol.get().equals(Protocol.WIREGUARD)) {
            settings.setWireGuardPort(port);
        } else {
            settings.setOpenVpnPort(port);
        }
    }

    private void onGeneratingError(String error, Throwable throwable) {
        LOGGER.error(TAG, "On generating error: " + error, throwable);
        dataLoading.set(false);

        if (throwable != null) {
            if (throwable instanceof UnknownHostException) {
                navigator.openDialogueError(Dialogs.CONNECTION_ERROR);
                return;
            } else {
                navigator.openDialogueError(Dialogs.WG_UPLOADING_KEY_ERROR);
                return;
            }
        }

        SessionErrorResponse errorResponse = Mapper.sessionErrorResponseFrom(error);
        if (error == null) {
            navigator.openDialogueError(Dialogs.WG_UPLOADING_KEY_ERROR);
            return;
        } else {
            errorResponse.getMessage();
        }

        if (errorResponse.getStatus() == Responses.WIREGUARD_KEY_LIMIT_REACHED) {
            navigator.openDialogueError(Dialogs.WG_MAXIMUM_KEYS_REACHED);
        } else {
            navigator.openCustomDialogueError(context.getString(R.string.dialogs_error) + errorResponse.getStatus(), errorResponse.getMessage());
        }
    }

    private boolean isVpnActive() {
        return vpnBehaviorController.isVPNActive();
    }

    private WireGuardKeysEventsListener getWireGuardKeysEventsListener() {
        return new WireGuardKeysEventsListener() {
            @Override
            public void onKeyGenerating() {
                LOGGER.info("onKeyGenerating");
                dataLoading.set(true);
                loadingMessage.set(context.getString(R.string.protocol_generating_and_uploading_key));
            }

            @Override
            public void onKeyGeneratedSuccess() {
                LOGGER.info("WireGuard public key was added to server");
                dataLoading.set(false);
                setProtocol(Protocol.WIREGUARD);
                wireGuardPublicKey = settings.getWireGuardPublicKey();
                wgInfo.set(getWireGuardInfo());
            }

            @Override
            public void onKeyGeneratedError(String error, Throwable throwable) {
                LOGGER.info("onKeyGeneratedError error = " + error + " throwable = " + throwable);
                dataLoading.set(false);
                ProtocolViewModel.this.onGeneratingError(error, throwable);
            }
        };
    }
}