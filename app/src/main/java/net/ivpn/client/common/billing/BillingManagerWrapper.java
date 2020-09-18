package net.ivpn.client.common.billing;

import android.app.Activity;
import android.content.Intent;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.PurchasePreference;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.common.session.SessionController;
import net.ivpn.client.common.session.SessionListenerImpl;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.addfunds.AddFundsRequestBody;
import net.ivpn.client.rest.data.addfunds.AddFundsResponse;
import net.ivpn.client.rest.data.addfunds.InitialPaymentRequestBody;
import net.ivpn.client.rest.data.addfunds.InitialPaymentResponse;
import net.ivpn.client.rest.data.addfunds.NewAccountRequestBody;
import net.ivpn.client.rest.data.addfunds.NewAccountResponse;
import net.ivpn.client.rest.data.session.SessionNewResponse;
import net.ivpn.client.rest.data.subscription.SubscriptionRequestBody;
import net.ivpn.client.rest.data.subscription.SubscriptionResponse;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.ui.billing.BillingActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.CREATE_ACCOUNT;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.CREATE_SESSION;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.CREATE_SESSION_ERROR;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.INITIAL_PAYMENT;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.INITIAL_PAYMENT_ERROR;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.UPDATE_SESSION;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.UPDATE_SESSION_ERROR;

@ApplicationScope
public class BillingManagerWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingManagerWrapper.class);

    private BillingManager billingManager;
    private PurchasePreference purchasePreference;
    private UserPreference userPreference;
    private Settings settings;
    private HttpClientFactory httpClientFactory;
    private ServersRepository serversRepository;
    private SessionController sessionController;

    private List<BillingListener> listeners;

    private SkuDetails skuDetails;
    private Purchase purchase;
    private String productName;

    private boolean isInit;
    private int error = 0;

    @Inject
    BillingManagerWrapper(PurchasePreference purchasePreference, BillingManager billingManager,
                          UserPreference userPreference, SessionController sessionController,
                          Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository) {
        this.billingManager = billingManager;
        this.purchasePreference = purchasePreference;
        this.userPreference = userPreference;
        this.settings = settings;
        this.httpClientFactory = httpClientFactory;
        this.serversRepository = serversRepository;
        this.sessionController = sessionController;

        listeners = new ArrayList<>();
        isInit = false;
        setPurchaseState(PurchaseState.NONE);

        init();
    }

    public void init() {
        billingManager.init(new BillingManager.BillingUpdatesListener() {
            @Override
            public void onBillingClientSetupFinished() {
                LOGGER.info("On billing client setup finished");
                isInit = true;

                for (BillingListener listener : listeners) {
                    listener.onInitStateChanged(isInit, error);
                }
            }

            @Override
            public void onPurchasesUpdated(List<Purchase> purchases) {
                LOGGER.info("Received purchases list");
                if (purchases.isEmpty()) {
                    purchase = null;
                    return;
                }

                for (Purchase purchase : purchases) {
                    LOGGER.info(purchase.toString());
                    if (purchase.isAcknowledged()
                            && ConsumableProducts.INSTANCE.getConsumableSKUs().contains(purchase.getSku())) {
                        billingManager.consumePurchase(purchase);
                    }
                }
                startValidatingActivity(purchases.get(0));
            }

            @Override
            public void onBillingError(int error) {
                LOGGER.info("Error code =" + error + " received");
                BillingManagerWrapper.this.error = error;
                isInit = false;

                for (BillingListener listener : listeners) {
                    listener.onInitStateChanged(isInit, error);
                }
            }
        });
    }

    public void startPurchase(Activity activity) {
        LOGGER.info("Purchasing...");
        setPurchaseState(PurchaseState.PURCHASING);
        String currentSku = purchase != null ? purchase.getSku() : null;
        billingManager.initiatePurchaseFlow(activity, skuDetails, currentSku, 0);
    }

    public void checkSkuDetails(List<String> skuList) {
        LOGGER.info("Query sku details...");

        billingManager.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList, (billingResult, skuDetailsList) -> {
            //ToDo Check all available billing results;
            LOGGER.info("Sku details, result = " + billingResult.getResponseCode());
            LOGGER.info("Sku details, error = " + billingResult.getDebugMessage());
            LOGGER.info("Sku details, listeners size = " + billingResult.getDebugMessage());
            for (BillingListener listener : listeners) {
                listener.onCheckingSkuDetailsSuccess(skuDetailsList);
            }
        });
    }

    private void startValidatingActivity(Purchase purchase) {
        this.purchase = purchase;
        if (purchase.isAcknowledged()) {
            LOGGER.info("Purchase is acknowledged");
            return;
        }

        IVPNApplication application = IVPNApplication.getApplication();
        Intent intent = new Intent(application, BillingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    public void validatePurchase() {
        String sessionToken = userPreference.getSessionToken();
        if (sessionToken == null || sessionToken.isEmpty()) {
            LOGGER.info("Start new purchase");
            initialPayment();
        } else {
            addFundsRequest(sessionToken);
        }

        saveSensitiveData(purchase);
    }

    private void initialPayment() {
        setPurchaseState(INITIAL_PAYMENT);
        final String accountId = userPreference.getBlankUsername();
        InitialPaymentRequestBody requestBody = new InitialPaymentRequestBody(accountId, purchase.getSku(), purchase.getPurchaseToken());
        LOGGER.info(requestBody.toString());
        Request<InitialPaymentResponse> request = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.LONG);
        request.start(api -> api.initialPayment(requestBody), new RequestListener<InitialPaymentResponse>() {

            @Override
            public void onSuccess(InitialPaymentResponse response) {
                LOGGER.info(response.toString());
                if (response.getStatus() == Responses.SUCCESS) {
                    createSession(accountId);

                    if (purchase != null && ConsumableProducts.INSTANCE.getConsumableSKUs().contains(purchase.getSku())) {
                        billingManager.consumePurchase(purchase);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                setPurchaseState(INITIAL_PAYMENT_ERROR);
            }

            @Override
            public void onError(String string) {
                setPurchaseState(INITIAL_PAYMENT_ERROR);
            }
        });
    }

    private void createSession(String accountId) {
        userPreference.putUserLogin(accountId);
        setPurchaseState(CREATE_SESSION);
        sessionController.subscribe(new SessionListenerImpl() {
            @Override
            public void onCreateSuccess(@NotNull SessionNewResponse response) {
                LOGGER.info("On create session success: " + response.toString());
                sessionController.unSubscribe(this);
                for (BillingListener listener : listeners) {
                    listener.onCreateAccountFinish();
                }
            }

            @Override
            public void onCreateError(@Nullable Throwable throwable, @Nullable ErrorResponse errorResponse) {
                LOGGER.info("On create session Error: " + throwable + "/n" + errorResponse);
                sessionController.unSubscribe(this);
                setPurchaseState(CREATE_SESSION_ERROR);
            }
        });
        sessionController.createSession(false, accountId);
    }

    private void addFundsRequest(String sessionToken) {
        setPurchaseState(INITIAL_PAYMENT);
        AddFundsRequestBody requestBody = new AddFundsRequestBody(sessionToken, purchase.getSku(), purchase.getPurchaseToken());
        LOGGER.info(requestBody.toString());
        Request<AddFundsResponse> request = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.LONG);
        request.start(api -> api.addFunds(requestBody), new RequestListener<AddFundsResponse>() {

            @Override
            public void onSuccess(AddFundsResponse response) {
                LOGGER.info(response.toString());
                if (response.getStatus() == Responses.SUCCESS) {
                    updateSession();
                } else {
                    setPurchaseState(INITIAL_PAYMENT_ERROR);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                setPurchaseState(INITIAL_PAYMENT_ERROR);
            }

            @Override
            public void onError(String string) {
                setPurchaseState(INITIAL_PAYMENT_ERROR);
            }
        });
    }

    private void updateSession() {
        setPurchaseState(UPDATE_SESSION);
        sessionController.subscribe(new SessionListenerImpl() {
            @Override
            public void onUpdateSuccess() {
                LOGGER.info("On update session success: ");
                sessionController.unSubscribe(this);
                for (BillingListener listener : listeners) {
                    listener.onAddFundsFinish();
                }
            }

            @Override
            public void onUpdateError(@Nullable Throwable throwable, @Nullable ErrorResponse errorResponse) {
                LOGGER.info("On create session Error: " + throwable + "/n" + errorResponse);
                sessionController.unSubscribe(this);
                setPurchaseState(UPDATE_SESSION_ERROR);
            }
        });
        sessionController.updateSessionStatus();
    }

    private SubscriptionRequestBody getSubscriptionRequestBody(Purchase purchase) {
        if (userPreference.getUserLogin() == null || userPreference.getUserLogin().isEmpty()) {
            return new SubscriptionRequestBody(purchasePreference.getUserEmail(),
                    purchasePreference.getUserPassword(), purchase.getSku(), purchase.getPurchaseToken());
        } else {
            return new SubscriptionRequestBody(userPreference.getUserLogin(),
                    purchase.getSku(), purchase.getPurchaseToken());
        }
    }

    private void processSubscriptionResponse(SubscriptionResponse response) {
        LOGGER.info("response.getStatus() = " + response.getStatus());
        switch (response.getStatus()) {
            case Responses.SUCCESS:
                saveAccountData(response);
                clearSensitiveData();
                setPurchaseState(PurchaseState.DONE);
                break;
            case Responses.SUBSCRIPTION_ALREADY_REGISTERED:
//                if (response.getData() != null && response.getData().getUsername() != null) {
//                    userPreference.putUserLogin(response.getData().getUsername());
//                }
                setPurchaseState(PurchaseState.NONE);
                for (BillingListener listener : listeners) {
                    listener.onPurchaseAlreadyDone();
                }
                break;
            case Responses.SUBSCRIPTION_GOOGLE_ERROR:
            case Responses.SUBSCRIPTION_ERROR_WHILE_CREATING_ACCOUNT:
                setPurchaseError(response.getStatus(), response.getMessage());
                break;
        }
    }

    private void saveAccountData(SubscriptionResponse response) {
        LOGGER.info("Save account data");
        if (response.getSessionToken() != null) {
            userPreference.putSessionToken(response.getSessionToken());
        }
        if (response.getVpnUsername() != null) {
            userPreference.putSessionUsername(response.getVpnUsername());
        }
        if (response.getVpnPassword() != null) {
            userPreference.putSessionPassword(response.getVpnPassword());
        }
        if (response.getUsername() != null) {
            userPreference.putUserLogin(response.getUsername());
        }

        if (response.getServiceStatus() == null) {
            return;
        }
        userPreference.putAvailableUntil(response.getServiceStatus().getActiveUntil());
        userPreference.putIsUserOnTrial(Boolean.valueOf(response.getServiceStatus().getIsOnFreeTrial()));
        userPreference.putCurrentPlan(response.getServiceStatus().getCurrentPlan());
        userPreference.putPaymentMethod(response.getServiceStatus().getPaymentMethod());
        userPreference.putIsActive(response.getServiceStatus().getIsActive());
        if (response.getServiceStatus().getCapabilities() != null) {
            userPreference.putIsUserOnPrivateEmailBeta(response.getServiceStatus().getCapabilities().contains(Responses.PRIVATE_EMAILS));
            boolean multiHopCapabilities = response.getServiceStatus().getCapabilities().contains(Responses.MULTI_HOP);
            LOGGER.info("multiHopCapabilities = " + multiHopCapabilities);
            userPreference.putCapabilityMultiHop(response.getServiceStatus().getCapabilities().contains(Responses.MULTI_HOP));
            if (!multiHopCapabilities) {
                LOGGER.info("multiHopCapabilities setting enable multihop as false ");
                settings.enableMultiHop(false);
            }
        }
    }

    private void setPurchaseState(PurchaseState newState) {
        for (BillingListener listener : listeners) {
            listener.onPurchaseStateChanged(newState);
        }
    }

    private void setPurchaseError(int errorCode, String errorMessage) {
        for (BillingListener listener : listeners) {
            listener.onPurchaseError(errorCode, errorMessage);
        }
    }

    private void finishCreateAccount() {
        for (BillingListener listener : listeners) {
            listener.onCreateAccountFinish();
        }
    }

    private void saveSensitiveData(Purchase purchase) {
        purchasePreference.putPurchaseProductId(purchase.getSku());
        purchasePreference.putPurchaseToken(purchase.getPurchaseToken());
    }

    private int getProrationMode(String currentSku, String newSku) {
        if (currentSku == null) {
            return BillingFlowParams.ProrationMode.UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY;
        }

        switch (currentSku) {
            case Sku.MONTH_STANDARD_SKU:
                return BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE;
            case Sku.MONTH_PRO_SKU:
                if (newSku.equals(Sku.MONTH_STANDARD_SKU)) {
                    return BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION;
                } else {
                    return BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE;
                }
            case Sku.YEAR_STANDARD_SKU:
                if (newSku.equals(Sku.YEAR_PRO_SKU)) {
                    return BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_PRORATED_PRICE;
                } else {
                    return BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION;
                }
            case Sku.YEAR_PRO_SKU:
                return BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION;
        }

        return BillingFlowParams.ProrationMode.UNKNOWN_SUBSCRIPTION_UPGRADE_DOWNGRADE_POLICY;
    }

    private void clearSensitiveData() {
        purchasePreference.clear();
    }

    public void forceInit() {
        if (!isInit) {
            init();
        }
    }

    public void setEmail(String email) {
        purchasePreference.putUserEmail(email);
    }

    public void setPassword(String password) {
        purchasePreference.putUserPassword(password);
    }

    public void setSkuDetails(SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void logout() {
        purchase = null;
    }

    public void setBillingListener(BillingListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        listener.onInitStateChanged(isInit, error);
    }

    public void removeBillingListener(BillingListener listener) {
        listeners.remove(listener);
    }

    public enum PurchaseState {
        NONE,
        PURCHASING,
        VALIDATING,
        DONE,

        CREATE_ACCOUNT,
        CREATE_ACCOUNT_ERROR,
        INITIAL_PAYMENT,
        INITIAL_PAYMENT_ERROR,
        CREATE_SESSION,
        CREATE_SESSION_ERROR,
        UPDATE_SESSION,
        UPDATE_SESSION_ERROR,
        ADD_FUNDS_ERROR
    }
}