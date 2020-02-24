package net.ivpn.client.ui.login;

import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.wireguard.android.crypto.Keypair;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.R;
import net.ivpn.client.common.Mapper;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.common.utils.ConnectivityUtil;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.model.WireGuard;
import net.ivpn.client.rest.data.session.SessionNewRequestBody;
import net.ivpn.client.rest.data.session.SessionNewResponse;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;

import javax.inject.Inject;

public class LoginViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginViewModel.class);
    private static final String ivpnPrefix = "ivpn";

    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> usernameError = new ObservableField<>();
    public final ObservableBoolean dataLoading = new ObservableBoolean();

    private Context context;
    private LoginNavigator navigator;
    private Request<SessionNewResponse> request;

    private UserPreference userPreference;
    private ProtocolController protocolController;

    private Settings settings;

    @Inject
    LoginViewModel(Context context, Settings settings, UserPreference userPreference,
                   HttpClientFactory clientFactory, ServersRepository serversRepository,
                   ProtocolController protocolController) {
        this.context = context;
        this.settings = settings;
        this.userPreference = userPreference;
        this.protocolController = protocolController;
        request = new Request<>(settings, clientFactory, serversRepository, Request.Duration.SHORT);

        username.set(userPreference.getUserLogin());
    }

    public void login(boolean force) {
        LOGGER.info("Trying to login");
        if (username.get() == null || !username.get().startsWith(ivpnPrefix)) {
            usernameError.set("Account ID should start with \"ivpn\".");
            return;
        }
        dataLoading.set(true);
        resetErrors();
        login(username.get().trim(), getWgKeyPair(), force);
    }

    public void login(final String username, final Keypair keys, boolean force) {
        String publicKey = keys != null ? keys.getPublicKey() : null;

        SessionNewRequestBody body = new SessionNewRequestBody(username, publicKey, force);
        request.start(api -> api.newSession(body),
                new RequestListener<SessionNewResponse>() {
                    @Override
                    public void onSuccess(SessionNewResponse response) {
                        LOGGER.info("Login process: SUCCESS. Response = " + response);
                        dataLoading.set(false);
                        LoginViewModel.this.onSuccess(username, keys, response);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LOGGER.error("Login process: ERROR", throwable);
                        if (!ConnectivityUtil.isOnline(context)) {
                            navigator.openErrorDialogue(Dialogs.CONNECTION_ERROR);
                            dataLoading.set(false);
                            return;
                        }

                        if (throwable instanceof InterruptedIOException) {
                            dataLoading.set(false);
                            navigator.openErrorDialogue(Dialogs.TOO_MANY_ATTEMPTS_ERROR);
                            return;
                        }
                    }

                    @Override
                    public void onError(String error) {
                        LOGGER.error("Login process: ERROR: " + error);
                        ErrorResponse errorResponse = Mapper.errorResponseFrom(error);
                        dataLoading.set(false);
                        handleErrorResponse(errorResponse, username);
                    }
                });
    }

    public void cancel() {
        LOGGER.info("cancel");
        dataLoading.set(false);
        request.cancel();
    }

    public void setNavigator(LoginNavigator navigator) {
        this.navigator = navigator;
    }

    private Keypair getWgKeyPair() {
        Protocol currentProtocol = protocolController.getCurrentProtocol();
        if (currentProtocol.equals(Protocol.WIREGUARD)) {
            return new Keypair();
        }
        return null;
    }

    private void onSuccess(String username, Keypair keys, SessionNewResponse response) {
        if (response.getStatus() == null) {
            navigator.openErrorDialogue(Dialogs.SERVER_ERROR);
            return;
        }

        LOGGER.info("Status = " + response.getStatus());
        if (response.getStatus() == Responses.SUCCESS) {
            putUserData(username, response);
            handleWireGuardResponse(keys, response.getWireGuard());
            if (userPreference.getIsActive()) {
                navigator.onLogin();
            } else {
                if (BuildConfig.BUILD_VARIANT.equals("site")) {
                    navigator.openSite();
                } else {
                    navigator.openSubscriptionScreen();
                }
            }
        } else {
            navigator.openErrorDialogue(Dialogs.SERVER_ERROR);
        }
    }

    private void handleErrorResponse(ErrorResponse errorResponse, String username) {
        if (errorResponse == null || errorResponse.getStatus() == null) {
            navigator.openErrorDialogue(Dialogs.SERVER_ERROR);
            return;
        }

        switch (errorResponse.getStatus()) {
            case Responses.INVALID_CREDENTIALS: {
                navigator.openErrorDialogue(Dialogs.AUTHENTICATION_ERROR);
                break;
            }
            case Responses.NOT_ACTIVE: {
                userPreference.putUserLogin(username);
                navigator.openActivateDialogue();
                break;
            }
            case Responses.SESSION_TOO_MANY: {
                navigator.openSessionLimitReachedDialogue();
                break;
            }

            case Responses.WIREGUARD_KEY_INVALID:
            case Responses.WIREGUARD_PUBLIC_KEY_EXIST:
            case Responses.BAD_REQUEST:
            case Responses.SESSION_SERVICE_ERROR:
            default: {
                navigator.openCustomErrorDialogue(context.getString(R.string.dialogs_error) + errorResponse.getStatus(),
                        errorResponse.getMessage() != null ? errorResponse.getMessage() : "");
                break;
            }
        }
    }

    private void handleWireGuardResponse(Keypair keys, WireGuard wireGuard) {
        if (wireGuard == null || wireGuard.getStatus() == null) {
            resetWireGuard();
            return;
        }

        if (wireGuard.getStatus() == Responses.SUCCESS) {
            putWireGuardData(keys, wireGuard);
        } else {
            LOGGER.error("Error received: " + wireGuard.getStatus() + " "
                    + (wireGuard.getMessage() != null ? wireGuard.getMessage() : ""));
            resetWireGuard();
        }
    }

    private void putUserData(String username, SessionNewResponse response) {
        LOGGER.info("Save account data");

        userPreference.putSessionToken(response.getToken());
        userPreference.putSessionUsername(response.getVpnUsername());
        userPreference.putSessionPassword(response.getVpnPassword());

        userPreference.putAvailableUntil(response.getServiceStatus().getActiveUntil());
        userPreference.putIsUserOnTrial(Boolean.valueOf(response.getServiceStatus().getIsOnFreeTrial()));
        userPreference.putCurrentPlan(response.getServiceStatus().getCurrentPlan());
        userPreference.putPaymentMethod(response.getServiceStatus().getPaymentMethod());
        userPreference.putIsActive(response.getServiceStatus().getIsActive());

        if (response.getServiceStatus().getCapabilities() != null) {
            userPreference.putIsUserOnPrivateEmailBeta(response.getServiceStatus().getCapabilities().contains(Responses.PRIVATE_EMAILS));
            boolean multiHopCapabilities = response.getServiceStatus().getCapabilities().contains(Responses.MULTI_HOP);
            userPreference.putCapabilityMultiHop(response.getServiceStatus().getCapabilities().contains(Responses.MULTI_HOP));
            if (!multiHopCapabilities) {
                settings.enableMultiHop(false);
            }
        }

        userPreference.putUserLogin(username);
    }

    private void resetErrors() {
        usernameError.set(null);
    }

    private void putWireGuardData(Keypair keys, WireGuard wireGuard) {
        settings.saveWireGuardKeypair(keys);
        settings.setWireGuardIpAddress(wireGuard.getIpAddress());
    }

    private void resetWireGuard() {
        protocolController.setCurrentProtocol(Protocol.OPENVPN);
    }
}