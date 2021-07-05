package net.ivpn.client.common.billing;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

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


import android.app.Activity;
import android.content.Intent;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.common.dagger.ApplicationScope;
import net.ivpn.client.common.prefs.EncryptedUserPreference;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.session.SessionController;
import net.ivpn.client.common.session.SessionListenerImpl;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.addfunds.AddFundsRequestBody;
import net.ivpn.client.rest.data.addfunds.AddFundsResponse;
import net.ivpn.client.rest.data.addfunds.InitialPaymentRequestBody;
import net.ivpn.client.rest.data.addfunds.InitialPaymentResponse;
import net.ivpn.client.rest.data.session.SessionNewResponse;
import net.ivpn.client.rest.data.wireguard.ErrorResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.rest.requests.common.RequestWrapper;
import net.ivpn.client.v2.billing.BillingActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.CREATE_SESSION;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.CREATE_SESSION_ERROR;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.INITIAL_PAYMENT;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.INITIAL_PAYMENT_ERROR;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.UPDATE_SESSION;
import static net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState.UPDATE_SESSION_ERROR;

@ApplicationScope
public class BillingManagerWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingManagerWrapper.class);

    private final BillingManager billingManager;
    private final EncryptedUserPreference userPreference;
    private final Settings settings;
    private final HttpClientFactory httpClientFactory;
    private final ServersRepository serversRepository;
    private final SessionController sessionController;

    private final List<BillingListener> listeners;

    private SkuDetails skuDetails;
    private Purchase purchase;

    private boolean isInit;
    private int error = 0;

    @Inject
    BillingManagerWrapper(BillingManager billingManager,
                          EncryptedUserPreference userPreference, SessionController sessionController,
                          Settings settings, HttpClientFactory httpClientFactory, ServersRepository serversRepository) {
        this.billingManager = billingManager;
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
                    if (purchase.isAcknowledged()
                            && ConsumableProducts.INSTANCE.getConsumableSKUs().contains(purchase.getSkus().get(0))) {
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
        String currentSku = purchase != null ? purchase.getSkus().get(0) : null;
        billingManager.initiatePurchaseFlow(activity, skuDetails, currentSku, 0);
    }

    public void checkSkuDetails(List<String> skuList) {
        LOGGER.info("Query sku details...");

        billingManager.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList, (billingResult, skuDetailsList) -> {
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
        if (sessionToken.isEmpty()) {
            LOGGER.info("Start new purchase");
            initialPayment();
        } else {
            addFundsRequest(sessionToken);
        }
    }

    private void initialPayment() {
        setPurchaseState(INITIAL_PAYMENT);
        final String accountId = userPreference.getBlankUsername();
        InitialPaymentRequestBody requestBody = new InitialPaymentRequestBody(accountId, purchase.getSkus().get(0), purchase.getPurchaseToken());
        Request<InitialPaymentResponse> request = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.LONG, RequestWrapper.IpMode.IPv4);
        request.start(api -> api.initialPayment(requestBody), new RequestListener<InitialPaymentResponse>() {

            @Override
            public void onSuccess(InitialPaymentResponse response) {
                if (response.getStatus() == Responses.SUCCESS) {
                    createSession(accountId);

                    if (purchase != null && ConsumableProducts.INSTANCE.getConsumableSKUs().contains(purchase.getSkus().get(0))) {
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
                LOGGER.info("On create session success");
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
        AddFundsRequestBody requestBody = new AddFundsRequestBody(sessionToken, purchase.getSkus().get(0), purchase.getPurchaseToken());
        Request<AddFundsResponse> request = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.LONG, RequestWrapper.IpMode.IPv4);
        request.start(api -> api.addFunds(requestBody), new RequestListener<AddFundsResponse>() {

            @Override
            public void onSuccess(AddFundsResponse response) {
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
                LOGGER.info("On update session success");
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

    private void setPurchaseState(PurchaseState newState) {
        for (BillingListener listener : listeners) {
            listener.onPurchaseStateChanged(newState);
        }
    }

    public void setSkuDetails(SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
    }

    public Purchase getPurchase() {
        return purchase;
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