package net.ivpn.client.ui.settings;

import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableLong;
import android.net.Uri;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.billingclient.api.Purchase;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.billing.BillingManagerWrapper;
import net.ivpn.client.common.billing.SubscriptionState;
import net.ivpn.client.common.pinger.OnPingFinishListener;
import net.ivpn.client.common.pinger.PingProvider;
import net.ivpn.client.common.pinger.PingResultFormatter;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.common.utils.FileUtils;
import net.ivpn.client.common.utils.SentryUtil;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.rest.data.session.DeleteSessionRequestBody;
import net.ivpn.client.rest.data.session.DeleteSessionResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.vpn.GlobalBehaviorController;
import net.ivpn.client.vpn.Protocol;
import net.ivpn.client.vpn.ProtocolController;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class SettingsViewModel extends BaseObservable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsViewModel.class);

    public final ObservableBoolean dataLoading = new ObservableBoolean();
//    public final ObservableField<Server> enterServer = new ObservableField<>();
//    public final ObservableField<Server> exitServer = new ObservableField<>();
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> subscriptionPlan = new ObservableField<>();
    public final ObservableField<String> accountType = new ObservableField<>();
    public final ObservableField<SubscriptionState> subscriptionState = new ObservableField<>();
    public final ObservableBoolean authenticated = new ObservableBoolean();
//    public final ObservableBoolean isAlwaysOnVpnSupported = new ObservableBoolean();
//    public final ObservableBoolean fastestServer = new ObservableBoolean();
    public final ObservableBoolean logging = new ObservableBoolean();
    public final ObservableBoolean crashLogging = new ObservableBoolean();
//    public final ObservableBoolean multiHop = new ObservableBoolean();
    public final ObservableBoolean killSwitch = new ObservableBoolean();
    public final ObservableBoolean isSentryEnabled = new ObservableBoolean();
    public final ObservableBoolean isOnFreeTrial = new ObservableBoolean();
    public final ObservableBoolean isAntiTrackerEnabled = new ObservableBoolean();
    public final ObservableBoolean isUpdatesEnabled = new ObservableBoolean();
    public final ObservableBoolean isManageSubscriptionAvailable = new ObservableBoolean();
//    public final ObservableBoolean isMultiHopEnabled = new ObservableBoolean();
    public final ObservableBoolean isNativeSubscription = new ObservableBoolean();
//    public final ObservableBoolean isStartOnBootEnabled = new ObservableBoolean();
    public final ObservableLong availableUntil = new ObservableLong();
//    public final ObservableField<PingResultFormatter> pingResultExitServer = new ObservableField<>();
//    public final ObservableField<PingResultFormatter> pingResultEnterServer = new ObservableField<>();

    public OnCheckedChangeListener enableLoggingListener = (compoundButton, value) -> enableLogging(value);
    public OnCheckedChangeListener enableCrashLoggingListener = (compoundButton, value) -> enableCrashLogging(value);
//    public OnCheckedChangeListener enableMultiHopListener = (compoundButton, value) -> enableMultiHop(value);
    public OnCheckedChangeListener enableKillSwitch = (compoundButton, value) -> enableKillSwitch(value);

//    public View.OnTouchListener multiHopTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (!authenticated.get()) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    navigator.authenticate();
//                }
//                return true;
//            }
//            if (!isActive()) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    navigator.subscribe();
//                }
//                return true;
//            }
//            if (isVpnActive()) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    navigator.notifyUser(R.string.snackbar_to_change_multihop_disconnect_first_msg,
//                            R.string.snackbar_disconnect_first, null);
//                }
//                return true;
//            }
//            if (!isMultihopAllowedByProtocol()) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    navigator.notifyUser(R.string.snackbar_multihop_not_allowed_for_wg,
//                            R.string.snackbar_disconnect_first, null);
//                }
//                return true;
//            }
//            return false;
//        }
//    };
    public View.OnTouchListener killSwitchTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!authenticated.get()) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    navigator.authenticate();
                }
                return true;
            }
            if (!isActive()) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    navigator.subscribe();
                }
                return true;
            }
            return false;
        }
    };

    private SettingsNavigator navigator;

    private Settings settings;
    private UserPreference userPreference;
    private ServersRepository serversRepository;
    private GlobalBehaviorController globalBehaviorController;
    private ProtocolController protocolController;
    private VpnBehaviorController vpnBehaviorController;
    private PingProvider pingProvider;
    private BillingManagerWrapper billingManager;
    private SentryUtil sentryUtil;

    private Request<DeleteSessionResponse> deleteSessionRequest;

    @Inject
    public SettingsViewModel(Settings settings, ServersRepository serversRepository,
                             UserPreference userPreference, HttpClientFactory clientFactory,
                             GlobalBehaviorController globalBehaviorController,
                             ProtocolController protocolController, VpnBehaviorController vpnBehaviorController,
                             PingProvider pingProvider, BillingManagerWrapper billingManager, SentryUtil sentryUtil) {
        this.settings = settings;
        this.userPreference = userPreference;
        this.serversRepository = serversRepository;
        this.globalBehaviorController = globalBehaviorController;
        this.protocolController = protocolController;
        this.vpnBehaviorController = vpnBehaviorController;
        this.pingProvider = pingProvider;
        this.billingManager = billingManager;
        this.sentryUtil = sentryUtil;

        deleteSessionRequest = new Request<>(settings, clientFactory, serversRepository, Request.Duration.SHORT);
    }

    public void setNavigator(SettingsNavigator navigator) {
        this.navigator = navigator;
    }

    void onResume() {
        logging.set(isLoggingEnabled());
//        multiHop.set(isMultiHopChecked());
//        isMultiHopEnabled.set(isMultiHopUIEnabled());
        killSwitch.set(isKillSwitchEnabled());
//        fastestServer.set(isFastestServerEnabled());
//        isAlwaysOnVpnSupported.set(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
//        enterServer.set(getCurrentServer(ServerType.ENTRY));
//        exitServer.set(getCurrentServer(ServerType.EXIT));
//        pingResultExitServer.set(null);
//        pingResultEnterServer.set(null);
        username.set(getUsername());
        accountType.set(getUserAccountType());
        isOnFreeTrial.set(isOnFreeTrial());
        availableUntil.set(getAvailableUntil());
        isAntiTrackerEnabled.set(isAntiTrackerEnabled());
        authenticated.set(isAuthenticated());
        isNativeSubscription.set(isNativeSubscription());
//        isStartOnBootEnabled.set(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P);
        subscriptionState.set(getSubscriptionState());
        subscriptionPlan.set(getSubscriptionPlan());
        isManageSubscriptionAvailable.set(isManageSubscriptionAvailable());
        crashLogging.set(sentryUtil.isEnabled);
        isSentryEnabled.set(isSentryEnabled());
        isUpdatesEnabled.set(isUpdatesEnabled());
//        ping(enterServer.get(), getPingFinishListener(ServerType.ENTRY));
//        ping(exitServer.get(), getPingFinishListener(ServerType.EXIT));
    }

    private void enableLogging(boolean value) {
        logging.set(value);
        settings.enableLogging(value);
    }

    private void enableCrashLogging(boolean value) {
        crashLogging.set(value);
        sentryUtil.setState(value);
    }

//    private void enableMultiHop(boolean value) {
//        multiHop.set(value);
//        fastestServer.set(isFastestServerEnabled());
//        settings.enableMultiHop(value);
//    }

    private void enableKillSwitch(boolean value) {
        killSwitch.set(value);
        settings.enableKillSwitch(value);
        navigator.enableKillSwitch(value, isAdvancedKillSwitchDialogEnabled());
    }

    public void cancel() {
        deleteSessionRequest.cancel();
    }

    void chooseServer(ServerType serverType) {
        navigator.chooseServer(serverType);
    }

    void splitTunneling() {
        if (isVpnActive()) {
            navigator.notifyUser(R.string.snackbar_to_use_split_tunneling_disconnect,
                    R.string.snackbar_disconnect_first, null);
        } else if (navigator != null) {
            navigator.splitTunneling();
        }
    }

    void customDNS() {
        if (isVpnActive()) {
            navigator.notifyUser(R.string.snackbar_to_use_custom_dns_disconnect,
                    R.string.snackbar_disconnect_first, null);
        } else if (navigator != null) {
            navigator.customDNS();
        }
    }

    void antiTracker() {
        if (isVpnActive()) {
            navigator.notifyUser(R.string.snackbar_to_use_antitracker_disconnect,
                    R.string.snackbar_disconnect_first, null);
        } else if (navigator != null) {
            navigator.antiTracker();
        }
    }

    void enableAdvancedKillSwitchDialog(boolean value) {
        settings.enableAdvancedKillSwitchDialog(value);
    }

//    private OnPingFinishListener getPingFinishListener(final ServerType serverType) {
//        return result -> {
//            if (serverType.equals(ServerType.ENTRY)) {
//                pingResultEnterServer.set(result);
//            } else {
//                pingResultExitServer.set(result);
//            }
//        };
//    }

//    private Server getCurrentServer(ServerType serverType) {
//        return serversRepository.getCurrentServer(serverType);
//    }

    public String getUsername() {
        return userPreference.getUserLogin();
    }

    private String getUserAccountType() {
        return userPreference.getCurrentPlan();
    }

    public String getSessionToken() {
        return userPreference.getSessionToken();
    }

    void logout() {
        LOGGER.info("Logout");
        vpnBehaviorController.disconnect();
        billingManager.logout();

        String token = userPreference.getSessionToken();

        if (token != null && !token.isEmpty()) {
            LOGGER.info("Logout, removing current session...");
            deleteSession(token);
        } else {
            LOGGER.info("Logout, removing local cache...");
            clearLocalCache();
        }
    }

    private void clearLocalCache() {
        IVPNApplication.getApplication().appComponent.provideComponentUtil().resetComponents();

        authenticated.set(false);
//        multiHop.set(false);
    }

    boolean isVpnActive() {
        return vpnBehaviorController.isVPNActive();
    }

    void setKillSwitchState(boolean isEnabled) {
        if (isEnabled) {
            globalBehaviorController.enableKillSwitch();
        } else {
            globalBehaviorController.disableKillSwitch();
        }
    }

    private boolean isMultihopAllowedByProtocol() {
        return protocolController.getCurrentProtocol().equals(Protocol.OPENVPN);
    }

    private boolean isLoggingEnabled() {
        return settings.isLoggingEnabled();
    }

    private boolean isMultiHopChecked() {
        return settings.isMultiHopEnabled() && isMultihopAllowedByProtocol();
    }

    private boolean isMultiHopUIEnabled() {
        LOGGER.info("Capabilities multihop = " + userPreference.getCapabilityMultiHop());
        return userPreference.getCapabilityMultiHop();
    }

    private boolean isKillSwitchEnabled() {
        return settings.isKillSwitchEnabled();
    }

    private boolean isFastestServerEnabled() {
//        if (multiHop.get() || isVpnActive()) {
//            return false;
//        }

        return settings.isFastestServerEnabled();
    }

    private boolean isAdvancedKillSwitchDialogEnabled() {
        return settings.isAdvancedKillSwitchDialogEnabled();
    }

    private boolean isOnFreeTrial() {
        return userPreference.isUserOnTrial();
    }

    private boolean isAntiTrackerEnabled() {
        return BuildConfig.BUILD_VARIANT.equals("site") || BuildConfig.BUILD_VARIANT.equals("fdroid");
    }

    private boolean isUpdatesEnabled() {
        return BuildConfig.BUILD_VARIANT.equals("site");
    }

    private boolean isAuthenticated() {
        String token = userPreference.getSessionToken();
        return !token.isEmpty();
    }

    private boolean isSentryEnabled() {
        return !BuildConfig.BUILD_VARIANT.equals("fdroid");
    }

    private boolean isManageSubscriptionAvailable() {
        if (!userPreference.getIsActive()) {
            return true;
        }

        String paymentMethod = userPreference.getPaymentMethod();
        Purchase purchase = billingManager.getPurchase();

        return paymentMethod.equals("ivpnandroidiap") && purchase != null;
    }

    private boolean isNativeSubscription() {
        String paymentMethod = userPreference.getPaymentMethod();
        Purchase purchase = billingManager.getPurchase();
        return paymentMethod.equals("ivpnandroidiap") && purchase != null && purchase.isAutoRenewing();
    }

    private String getSubscriptionPlan() {
        String plan = userPreference.getCurrentPlan();
        if (!userPreference.getIsActive()) {
            plan += " (inactive)";
            return plan;
        }
        Purchase purchase = billingManager.getPurchase();
        if (plan == null || purchase == null) {
            return plan;
        }
        if (!purchase.isAutoRenewing()) {
            plan += " (cancelled)";
        }

        return plan;
    }

//    private void ping(Server server, OnPingFinishListener listener) {
//        pingProvider.ping(server, listener);
//    }

    private SubscriptionState getSubscriptionState() {
        if (!userPreference.getIsActive()) {
            return SubscriptionState.INACTIVE;
        }
        Purchase purchase = billingManager.getPurchase();
        if (purchase == null) {
            return SubscriptionState.ACTIVE;
        }
        if (purchase.isAutoRenewing()) {
            return SubscriptionState.ACTIVE;
        } else {
            return SubscriptionState.CANCELLED;
        }
    }

    boolean isActive() {
        return userPreference.getIsActive();
    }

    private void deleteSession(String token) {
        LOGGER.info("Deleting current session from server");
        dataLoading.set(true);
        DeleteSessionRequestBody requestBody = new DeleteSessionRequestBody(token);

        deleteSessionRequest.start(api -> api.deleteSession(requestBody),
                new RequestListener<DeleteSessionResponse>() {

            @Override
            public void onSuccess(DeleteSessionResponse response) {
                LOGGER.info("Deleting session from server state: SUCCESS");
                LOGGER.info(response.toString());
                SettingsViewModel.this.onRemoveSuccess();
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Error while deleting session from server", throwable);
                SettingsViewModel.this.onRemoveError();
            }

            @Override
            public void onError(String error) {
                LOGGER.error("Error while deleting session from server", error);
                SettingsViewModel.this.onRemoveError();
            }
        });
    }

    private void onRemoveSuccess() {
        dataLoading.set(false);
        clearLocalCache();
    }

    private void onRemoveError() {
        dataLoading.set(false);
        clearLocalCache();
    }

    Uri getLogFileUri(Context context) {
        return FileUtils.createLogFileUri(context);
    }

    Uri getSubscriptionUri() {
        if (billingManager.getPurchase() == null) {
            return Uri.parse("https://play.google.com/store/account/subscriptions");
        }
        return Uri.parse("https://play.google.com/store/account/subscriptions?sku=" +
                billingManager.getPurchase().getSku() + "&package=" +
                BuildConfig.APPLICATION_ID);
    }

    private long getAvailableUntil() {
        return userPreference.getAvailableUntil();
    }
}