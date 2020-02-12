package net.ivpn.client.ui.connect;

import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import android.util.Log;

import com.todtenkopf.mvvm.ViewModelBase;

import net.ivpn.client.R;
import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.billing.BillingManagerWrapper;
import net.ivpn.client.common.pinger.OnPingFinishListener;
import net.ivpn.client.common.pinger.PingProvider;
import net.ivpn.client.common.pinger.PingResultFormatter;
import net.ivpn.client.common.prefs.NetworkProtectionPreference;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.common.utils.ComponentUtil;
import net.ivpn.client.common.utils.ConnectivityUtil;
import net.ivpn.client.common.utils.StringUtil;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.model.ServiceStatus;
import net.ivpn.client.rest.data.model.WireGuard;
import net.ivpn.client.rest.data.session.SessionNewRequestBody;
import net.ivpn.client.rest.data.session.SessionNewResponse;
import net.ivpn.client.rest.data.session.SessionStatusRequestBody;
import net.ivpn.client.rest.data.session.SessionStatusResponse;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.network.OnNetworkBehaviourChangedListener;
import net.ivpn.client.ui.network.OnNetworkSourceChangedListener;
import net.ivpn.client.vpn.GlobalBehaviorController;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.controller.VpnBehaviorController;
import net.ivpn.client.vpn.controller.VpnStateListener;
import net.ivpn.client.vpn.local.NetworkController;
import net.ivpn.client.vpn.model.NetworkSource;
import net.ivpn.client.vpn.model.NetworkState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.util.Objects;

import javax.inject.Inject;

import static net.ivpn.client.vpn.model.NetworkSource.WIFI;

public class ConnectViewModel extends ViewModelBase implements OnNetworkSourceChangedListener,
        VpnStateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectViewModel.class);

    private ConnectionNavigator navigator;
    private Context context;
    private UserPreference userPreference;
    private Settings settings;
    private ServersRepository serversRepository;
    private ComponentUtil componentUtil;
    private GlobalBehaviorController globalBehaviorController;
    private ProtocolController protocolController;
    private VpnBehaviorController vpnBehaviorController;
    private NetworkController networkController;
    private PingProvider pingProvider;
    private NetworkProtectionPreference networkProtectionPreference;
    private BillingManagerWrapper billingManager;

    public final ObservableField<Long> activeUntil = new ObservableField<>();
    public final ObservableBoolean isPaused = new ObservableBoolean();
    public final ObservableBoolean isFastestServerEnabled = new ObservableBoolean();
    public final ObservableBoolean isMultiHopEnabled = new ObservableBoolean();
    public final ObservableBoolean isPrivateEmailsEnabled = new ObservableBoolean();
    public final ObservableBoolean isNewForPrivateEmails = new ObservableBoolean();
    public final ObservableBoolean areNetworkRulesEnabled = new ObservableBoolean();
    public final ObservableField<Server> entryServer = new ObservableField<>();
    public final ObservableField<Server> exitServer = new ObservableField<>();
    public final ObservableField<String> connectionStatus = new ObservableField<>();
    public final ObservableField<String> connectionUserHint = new ObservableField<>();
    public final ObservableField<String> timeUntilResumed = new ObservableField<>();
    public final ObservableField<String> connectionViewHint = new ObservableField<>();
    public final ObservableField<PingResultFormatter> pingResultExitServer = new ObservableField<>();
    public final ObservableField<PingResultFormatter> pingResultEntryServer = new ObservableField<>();
    public final ObservableField<NetworkSource> networkSource = new ObservableField<>();
    public final ObservableField<NetworkState> defaultState = new ObservableField<>();
    public final ObservableField<NetworkState> currentState = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public OnNetworkBehaviourChangedListener listener = state -> {
        if (networkSource.get() != null && Objects.equals(networkSource.get(), WIFI)) {
            networkController.changeMarkFor(networkSource.get().getSsid(), currentState.get(), state);
        } else {
            networkController.updateMobileDataState(state);
        }
        currentState.set(state);
    };

    private Request<SessionStatusResponse> sessionStatusRequest;
    private Request<SessionNewResponse> sessionNewRequest;

    CommandVM privateEmailCommand = new CommandVM() {
        @Override
        public void execute() {
            navigator.openPrivateEmails();
        }

        @Override
        public void refresh() {
            isEnabled(isPrivateEmailsEnabled.get() && !isNewForPrivateEmails.get());
        }
    };

    CommandVM newPrivateEmailCommand = new CommandVM() {
        @Override
        public void execute() {
            navigator.openPrivateEmails();
        }

        @Override
        public void refresh() {
            isEnabled(isPrivateEmailsEnabled.get() && isNewForPrivateEmails.get());
        }
    };

    CommandVM settingsCommand = new CommandVM() {
        @Override
        public void execute() {
            navigator.openSettings();
        }

        @Override
        public void refresh() {
            isEnabled(true);
        }
    };

    CommandVM infoCommand = new CommandVM() {
        @Override
        public void execute() {
            navigator.openInfoDialogue();
        }

        @Override
        public void refresh() {
            isEnabled(true);
        }
    };

    @Inject
    ConnectViewModel(Context context, Settings settings, UserPreference userPreference,
                     ServersRepository serversRepository, GlobalBehaviorController globalBehaviorController,
                     NetworkProtectionPreference networkProtectionPreference,
                     HttpClientFactory httpClientFactory, ComponentUtil componentUtil,
                     ProtocolController protocolController, VpnBehaviorController vpnBehaviorController,
                     NetworkController networkController, PingProvider pingProvider, BillingManagerWrapper billingManager) {
        this.context = context;
        this.settings = settings;
        this.userPreference = userPreference;
        this.componentUtil = componentUtil;
        this.serversRepository = serversRepository;
        this.globalBehaviorController = globalBehaviorController;
        this.protocolController = protocolController;
        this.vpnBehaviorController = vpnBehaviorController;
        this.networkController = networkController;
        this.pingProvider = pingProvider;
        this.networkProtectionPreference = networkProtectionPreference;
        this.billingManager = billingManager;

        sessionStatusRequest = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.SHORT);
        sessionNewRequest = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.SHORT);
        activeUntil.set(getAvailableUntil());
        defaultState.set(networkProtectionPreference.getDefaultNetworkState());
    }

    void onStart() {
        LOGGER.info("onStart: ");
        networkController.setNetworkSourceChangedListener(this);
        vpnBehaviorController.setVpnStateListener(this);
    }

    void onResume() {
        LOGGER.info("onResume: ");
        isPrivateEmailsEnabled.set(isPrivateEmailsEnabled());
        isNewForPrivateEmails.set(isNewForPrivateEmails());
        isMultiHopEnabled.set(isMultiHopEnabled());
        if (getFastestServerSetting()) {
            pingProvider.pingAll(false);
        }
        pingResultExitServer.set(null);
        pingResultEntryServer.set(null);

        updateStatus();

        entryServer.set(getCurrentServer(ServerType.ENTRY));
        exitServer.set(getCurrentServer(ServerType.EXIT));
        ping(entryServer.get(), getPingFinishListener(ServerType.ENTRY));
        ping(exitServer.get(), getPingFinishListener(ServerType.EXIT));

        areNetworkRulesEnabled.set(areNetworkRulesEnabled());
        networkController.updateNetworkSource(context);

        notifyConnectionState();
        refreshCommands();
    }

    void onStop() {
        networkController.removeNetworkSourceListener();
        vpnBehaviorController.removeVpnStateListener(this);
    }

    void setNavigator(ConnectionNavigator navigator) {
        this.navigator = navigator;
    }

    void onConnectRequest() {
        if (!isTokenExist()) {
            createNewSession(false);
            return;
        }
        vpnBehaviorController.connectionActionByUser();
    }

    void onPauseRequest() {
        vpnBehaviorController.pauseActionByUser();
    }

    void onStopRequest() {
        vpnBehaviorController.stopActionByUser();
    }

    void chooseServer(ServerType serverType) {
        navigator.chooseServer(serverType);
    }

    void tryWifiWatcher() {
        networkController.tryWifiWatcher();
    }

    void createNewSession(boolean force) {
        connectionUserHint.set(context.getString(R.string.connect_hint_creating_session));
        SessionNewRequestBody body = new SessionNewRequestBody(getUsername(), getWireGuardPublicKey(), force);
        LOGGER.info("SessionNewRequestBody = " + body);
        sessionNewRequest.start(api -> api.newSession(body),
                getSessionNewRequestListener());
    }

    private RequestListener<SessionNewResponse> getSessionNewRequestListener() {
        return new RequestListener<SessionNewResponse>() {
            @Override
            public void onSuccess(SessionNewResponse response) {
                LOGGER.info(response.toString());
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
                if (response.getStatus() == null) {
                    //ignore it
                    vpnBehaviorController.connectionActionByUser();
                    return;
                }

                LOGGER.info("Status = " + response.getStatus());
                if (response.getStatus() == Responses.SUCCESS) {
                    putUserData(response);
                    handleWireGuardResponse(response.getWireGuard());
                    //Connect using session username/password
                    vpnBehaviorController.connectionActionByUser();
                } else {
                    navigator.openErrorDialog(Dialogs.SERVER_ERROR);
                }

            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Create session: ERROR", throwable);
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
                if (!ConnectivityUtil.isOnline(context)) {
                    navigator.openErrorDialog(Dialogs.CONNECTION_ERROR);
                    return;
                }

                if (throwable instanceof InterruptedIOException) {
                    navigator.openErrorDialog(Dialogs.TOO_MANY_ATTEMPTS_ERROR);
                    return;
                }
            }

            @Override
            public void onError(String error) {
                LOGGER.error("Create session: ERROR", error);
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
                ErrorResponse errorResponse = Mapper.errorResponseFrom(error);
                handleErrorResponse(errorResponse);
            }
        };
    }

    private OnPingFinishListener getPingFinishListener(final ServerType serverType) {
        return result -> {
            if (serverType.equals(ServerType.ENTRY)) {
                pingResultEntryServer.set(result);
            } else {
                pingResultExitServer.set(result);
            }
        };
    }

    private void notifyConnectionState() {
        vpnBehaviorController.notifyVpnState();
    }

    private Long getAvailableUntil() {
        return userPreference.getAvailableUntil();
    }

    private Server getCurrentServer(ServerType serverType) {
        return serversRepository.getCurrentServer(serverType);
    }

    boolean isCredentialsAbsent() {
        if (isTokenExist()) {
            return false;
        }
        return userPreference.getUserLogin().isEmpty();
    }

    boolean isActive() {
        return userPreference.getIsActive();
    }

    private boolean isTokenExist() {
        return !userPreference.getSessionToken().isEmpty();
    }

    private boolean isMultiHopEnabled() {
        LOGGER.info("isMultiHopEnabled = " + (settings.isMultiHopEnabled() && isMultihopAllowedByProtocol()));
        return settings.isMultiHopEnabled() && isMultihopAllowedByProtocol();
    }

    private boolean isMultihopAllowedByProtocol() {
        return protocolController.getCurrentProtocol().equals(Protocol.OPENVPN);
    }

    private boolean isPrivateEmailsEnabled() {
        return userPreference.isUserOnPrivateEmailsBeta();
    }

    private boolean isNewForPrivateEmails() {
        return settings.isNewForPrivateEmails();
    }

    private boolean areNetworkRulesEnabled() {
        return settings.isNetworkRulesEnabled();
    }

    private void updateFastestServer(ConnectionState state) {
        if (isMultiHopEnabled.get() || !state.equals(ConnectionState.NOT_CONNECTED)) {
            isFastestServerEnabled.set(false);
            return;
        }

        isFastestServerEnabled.set(getFastestServerSetting());
    }

    private boolean getFastestServerSetting() {
        return settings.isFastestServerEnabled();
    }


    private void updateStatus() {
        String sessionToken = getSessionToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            updateSessionStatus();
        }
    }

    private void updateSessionStatus() {
        SessionStatusRequestBody body = new SessionStatusRequestBody(getSessionToken());
        LOGGER.info("SessionStatusRequestBody = " + body);
        sessionStatusRequest.start(api -> api.sessionStatus(body),
                new RequestListener<SessionStatusResponse>() {
                    @Override
                    public void onSuccess(SessionStatusResponse response) {
                        if (response.getStatus() != null && response.getStatus().equals(Responses.SUCCESS)) {
                            LOGGER.info("Session status response received successfully");
                            LOGGER.info(response.toString());
                            saveSessionStatus(response.getServiceStatus());
                            if (response.getServiceStatus().getActiveUntil() != 0) {
                                activeUntil.set(response.getServiceStatus().getActiveUntil());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LOGGER.error("Failed updating session status ", throwable);
                    }

                    @Override
                    public void onError(String error) {
                        LOGGER.error("Error while getting account status to see the confirmation" + error);
                        ErrorResponse errorResponse = Mapper.errorResponseFrom(error);
                        if (errorResponse == null || errorResponse.getStatus() == null) {
                            return;
                        }

                        switch (errorResponse.getStatus()) {
                            case Responses.SESSION_NOT_FOUND:
                                navigator.logout();
                                break;
                            case Responses.SERVICE_IS_NOT_ACTIVE:
                                userPreference.putIsActive(false);
                                break;
                        }
                    }
                });
    }

    boolean isVpnActive() {
        return vpnBehaviorController.isVPNActive();
    }

    void logout() {
        componentUtil.resetComponents();
        globalBehaviorController.stopVPN();
    }

    void startKillSwitch() {
        globalBehaviorController.startKillSwitch();
    }

    boolean isKillSwitchShouldBeStarted() {
        return globalBehaviorController.isKillSwitchShouldBeStarted();
    }

    void disconnect() {
        vpnBehaviorController.disconnect();
    }

    NetworkState getDefaultNetworkState() {
        return networkProtectionPreference.getDefaultNetworkState();
    }

    private void saveSessionStatus(ServiceStatus serviceStatus) {
        if (serviceStatus.getIsOnFreeTrial() == null
                || serviceStatus.getActiveUntil() == 0) {
            return;
        }

        userPreference.putIsUserOnTrial(Boolean.valueOf(serviceStatus.getIsOnFreeTrial()));
        userPreference.putAvailableUntil(serviceStatus.getActiveUntil());
        userPreference.putCurrentPlan(serviceStatus.getCurrentPlan());
        userPreference.putPaymentMethod(serviceStatus.getPaymentMethod());
        userPreference.putIsActive(serviceStatus.getIsActive());
        if (serviceStatus.getCapabilities() != null) {
            userPreference.putIsUserOnPrivateEmailBeta(serviceStatus.getCapabilities().contains(Responses.PRIVATE_EMAILS));
            boolean multiHopCapabilities = serviceStatus.getCapabilities().contains(Responses.MULTI_HOP);
            userPreference.putCapabilityMultiHop(serviceStatus.getCapabilities().contains(Responses.MULTI_HOP));
            if (!multiHopCapabilities) {
                settings.enableMultiHop(false);
            }
        }
    }

    private String getSessionToken() {
        return userPreference.getSessionToken();
    }

    private String getUsername() {
        return userPreference.getUserLogin();
    }

    private String getWireGuardPublicKey() {
        return settings.getWireGuardPublicKey();
    }

    private void ping(Server server, OnPingFinishListener listener) {
        pingProvider.ping(server, listener);
    }

    private void putUserData(SessionNewResponse response) {
        LOGGER.info("Save account data");

        userPreference.putSessionToken(response.getToken());
        userPreference.putSessionUsername(response.getVpnUsername());
        userPreference.putSessionPassword(response.getVpnPassword());
        saveSessionStatus(response.getServiceStatus());
    }

    private void handleWireGuardResponse(WireGuard wireGuard) {
        LOGGER.info("Handle WireGuard response: " + wireGuard);
        if (wireGuard == null || wireGuard.getStatus() == null) {
            //ignore it right now
            resetWireGuard();
            return;
        }

        if (wireGuard.getStatus() == Responses.SUCCESS) {
            putWireGuardData(wireGuard);
        } else {
            LOGGER.error("Error received: " + wireGuard.getStatus() + " "
                    + (wireGuard.getMessage() != null ? wireGuard.getMessage() : ""));
            resetWireGuard();
        }
    }

    private void putWireGuardData(WireGuard wireGuard) {
        LOGGER.info("Save WireGuard data");
        settings.setWireGuardIpAddress(wireGuard.getIpAddress());
    }

    private void resetWireGuard() {
        LOGGER.info("Reset WireGuard protocol");
        protocolController.setCurrentProtocol(Protocol.OPENVPN);
    }

    private void handleErrorResponse(ErrorResponse errorResponse) {
        LOGGER.info("Handle error response: " + errorResponse);
        if (errorResponse == null || errorResponse.getStatus() == null) {
            vpnBehaviorController.connectionActionByUser();
            return;
        }

        switch (errorResponse.getStatus()) {
            case Responses.INVALID_CREDENTIALS: {
                navigator.openErrorDialog(Dialogs.AUTHENTICATION_ERROR);
                navigator.logout();
                break;
            }

            case Responses.SESSION_TOO_MANY: {
                navigator.openSessionLimitReachedDialogue();
                break;
            }

            case Responses.WIREGUARD_KEY_INVALID:
            case Responses.WIREGUARD_PUBLIC_KEY_EXIST:
            case Responses.BAD_REQUEST:
            case Responses.SESSION_SERVICE_ERROR: {
                vpnBehaviorController.connectionActionByUser();
                break;
            }

            default: {
                vpnBehaviorController.connectionActionByUser();
                break;
            }
        }
    }

    @Override
    public void onNetworkSourceChanged(NetworkSource source) {
        Log.d("NetworkController", "onNetworkSourceChanged: source = " + source);
        if (source.equals(WIFI)) {
            Log.d("NetworkController", "onNetworkSourceChanged: ssid = " + source.getSsid());
        }
        networkSource.set(source);
        defaultState.set(source.getDefaultState());
        currentState.set(source.getState());
        title.set(source.getTitle());
    }

    @Override
    public void onDefaultNetworkStateChanged(NetworkState defaultState) {
        this.defaultState.set(defaultState);
    }

    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        if (state == null) {
            return;
        }
        navigator.onConnectionStateChanged(state);
        updateFastestServer(state);
        switch (state) {
            case CONNECTED: {
                isPaused.set(false);
                connectionStatus.set(context.getString(R.string.connect_status_connected));
                connectionUserHint.set(context.getString(R.string.connect_hint_connected));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_connected));
                break;
            }
            case CONNECTING: {
                isPaused.set(false);
                connectionStatus.set(context.getString(R.string.connect_status_connecting));
                connectionUserHint.set(context.getString(R.string.connect_hint_connecting));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_connecting));
                break;
            }
            case DISCONNECTING: {
                isPaused.set(false);
                connectionStatus.set(context.getString(R.string.connect_status_disconnecting));
                connectionUserHint.set(context.getString(R.string.connect_hint_disconnecting));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_disconnecting));
                break;
            }
            case NOT_CONNECTED: {
                isPaused.set(false);
                connectionStatus.set(context.getString(R.string.connect_status_not_connected));
                connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_not_connected));
                break;
            }
            case PAUSING: {
                connectionStatus.set(context.getString(R.string.connect_status_pausing));
                connectionUserHint.set(context.getString(R.string.connect_hint_pausing));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_pausing));
                break;
            }
            case PAUSED: {
                isPaused.set(true);
                connectionStatus.set(context.getString(R.string.connect_status_paused));
                connectionUserHint.set(context.getString(R.string.connect_hint_paused));
                connectionViewHint.set(context.getString(R.string.connect_state_hint_paused));
                break;
            }
        }
    }

    @Override
    public void onAuthFailed() {
        navigator.onAuthFailed();
    }

    @Override
    public void onTimeTick(long millisUntilResumed) {
        timeUntilResumed.set(StringUtil.formatTimeUntilResumed(millisUntilResumed));
    }

    @Override
    public void notifyAnotherPortUsedToConnect() {
        navigator.notifyAnotherPortUsedToConnect();
    }

    @Override
    public void onTimeOut() {
        navigator.onTimeOut();
    }

    @Override
    public void onFindingFastestServer() {
        connectionUserHint.set(context.getString(R.string.connect_hint_finding_fastest));
    }

    @Override
    public void onCheckSessionState() {
        LOGGER.info("Check session state");
        String sessionToken = getSessionToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            updateSessionStatus();
        }
    }

    @Override
    public void onRegeneratingKeys() {
        connectionUserHint.set(context.getString(R.string.connect_hint_regeneration_wg_key));
    }

    @Override
    public void onRegenerationSuccess() {
        connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
    }

    @Override
    public void onRegenerationError(Dialogs errorDialog) {
        connectionUserHint.set(context.getString(R.string.connect_hint_not_connected));
        navigator.openErrorDialog(errorDialog);
    }

    @Override
    public void notifyServerAsFastest(Server server) {
        Log.d("DRD", "notifyServerAsFastest: server = " + server.getDescription());
        entryServer.set(server);
    }

    @Override
    public void notifyNoNetworkConnection() {
        navigator.openNoNetworkDialog();
    }

    void applyNetworkFeatureState(boolean isEnabled) {
        areNetworkRulesEnabled.set(isEnabled);
        settings.enableNetworkRulesSettings(isEnabled);
    }
}