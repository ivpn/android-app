package net.ivpn.client.ui.protocol;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

import net.ivpn.client.R;
import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.protocol.dialog.WireGuardDialogInfo;
import net.ivpn.client.ui.protocol.port.OnPortSelectedListener;
import net.ivpn.client.ui.protocol.port.Port;
import net.ivpn.client.ui.protocol.view.OnValueChangeListener;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.controller.WireGuardKeyController;
import net.ivpn.client.vpn.controller.WireGuardKeyController.WireGuardKeysEventsListener;

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

    private ProtocolNavigator navigator;
    private String wireGuardPublicKey;
    private Context context;
    private WireGuardKeyController keyController;
    private Settings settings;
    private ProtocolController protocolController;
    private VpnBehaviorController vpnBehaviorController;

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
    public View.OnTouchListener portsTouchListener = (view, motionEvent) -> {
        if (isVpnActive()) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                navigator.notifyUser(R.string.snackbar_to_change_port_disconnect_first_msg,
                        R.string.snackbar_disconnect_first, null);
            }
            return true;
        }
        return false;
    };

    @Inject
    ProtocolViewModel(Context context, Settings settings, WireGuardKeyController keyController,
                      ProtocolController protocolController, VpnBehaviorController vpnBehaviorController) {
        this.context = context;
        this.settings = settings;
        this.keyController = keyController;
        this.protocolController = protocolController;
        this.vpnBehaviorController = vpnBehaviorController;

        init();
    }

    private void init() {
        this.keyController.setKeysEventsListener(getWireGuardKeysEventsListener());

        protocol.set(protocolController.getCurrentProtocol());
        openVPNPort.set(settings.getOpenVpnPort());
        wireGuardPort.set(settings.getWireGuardPort());

        wireGuardPublicKey = settings.getWireGuardPublicKey();
        regenerationPeriod.set(String.valueOf(keyController.getRegenerationPeriod()));
    }

    public void setNavigator(ProtocolNavigator navigator) {
        this.navigator = navigator;
    }

    public void copyWgKeyToClipboard(ClipboardManager clipboard) {
        ClipData clip = ClipData.newPlainText("wireguard_public_key", wireGuardPublicKey);
        clipboard.setPrimaryClip(clip);
    }

    public void copyWgIpToClipboard(ClipboardManager clipboard) {
        ClipData clip = ClipData.newPlainText("wireguard_ip", wireGuardPublicKey);
        clipboard.setPrimaryClip(clip);
    }

    public WireGuardDialogInfo getWireGuardInfo() {
        String ipAddress = settings.getWireGuardIpAddress();

        long regenerationPeriod = keyController.getRegenerationPeriod();
        long lastGeneratedTime = settings.getGenerationTime();

        return new WireGuardDialogInfo(wireGuardPublicKey, ipAddress, lastGeneratedTime, regenerationPeriod);
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
            settings.setWgPort(port);
        } else {
            settings.setOpenVPNPort(port);
        }
    }

    private void onGeneratingError(String error, Throwable throwable) {
        LOGGER.error(TAG, "On generating error: " + error, throwable);
        dataLoading.set(false);
        setProtocol(Protocol.OPENVPN);

        if (throwable != null) {
            if (throwable instanceof UnknownHostException) {
                navigator.openDialogueError(Dialogs.CONNECTION_ERROR);
                return;
            } else {
                navigator.openDialogueError(Dialogs.WG_UPLOADING_KEY_ERROR);
                return;
            }
        }

        ErrorResponse errorResponse = Mapper.errorResponseFrom(error);
        if (error == null || errorResponse.getStatus() == null || errorResponse.getMessage() == null) {
            navigator.openDialogueError(Dialogs.WG_UPLOADING_KEY_ERROR);
            return;
        }

        if (errorResponse.getStatus() == Responses.WIREGUARD_KEY_LIMIT_REACHED) {
            navigator.openDialogueError(Dialogs.WG_MAXIMUM_KEYS_REACHED);
        } else {
            navigator.openCustomDialogueError(context.getString(R.string.dialogs_error) + errorResponse.getStatus(),
                    errorResponse.getMessage() != null ? errorResponse.getMessage() : "");
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