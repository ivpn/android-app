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
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.common.dagger.ApplicationScope;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class BillingManager implements PurchasesUpdatedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingManager.class);
    /**
     * True if billing service is connected now.
     */
    private boolean isServiceConnected;

    private BillingUpdatesListener updatesListener;
    private final BillingClient billingClient;

    @Inject
    BillingManager(Context context) {
        LOGGER.info("Creating Billing client.");
        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();
    }

    public void init(BillingUpdatesListener updatesListener) {
        LOGGER.info("Starting setup.");
        this.updatesListener = updatesListener;
        startServiceConnection(() -> {
            // Notifying the listener that billing client is ready
            updatesListener.onBillingClientSetupFinished();
            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            LOGGER.info("Setup successful. Querying inventory.");
            queryPurchases();
        });
    }

    private void startServiceConnection(final Runnable executeOnSuccess) {
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NotNull BillingResult billingResult) {
                LOGGER.info("Setup finished. Response code: " + billingResult.getResponseCode());
                LOGGER.info("Debug message: " + billingResult.getDebugMessage());

                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    isServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                } else {
                    updatesListener.onBillingError(billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isServiceConnected = false;
            }
        });
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    private void queryPurchases() {
        Runnable queryToExecute = () -> {
            long time = System.currentTimeMillis();
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, purchases) -> {
                LOGGER.info("Querying products elapsed time: " + (System.currentTimeMillis() - time)
                        + "ms");
                LOGGER.info("Querying products result code: "
                        + billingResult.getResponseCode()
                        + " res: " + purchases.size());

                if (billingResult.getResponseCode() != BillingResponseCode.OK) {
                    LOGGER.info("Got an error response trying to query products purchases");
                }

                onQueryPurchasesFinished(billingResult, purchases);
            });
        };

        executeServiceRequest(queryToExecute);
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType, final List<String> skuList,
                              final SkuDetailsResponseListener listener) {
        LOGGER.info("querySkuDetailsAsync");
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = () -> {
            // Query the purchase async
            LOGGER.info("Execute querying sku details");
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(itemType);
            billingClient.querySkuDetailsAsync(params.build(),
                    listener);
        };

        executeServiceRequest(queryRequest);
    }

    public void initiatePurchaseFlow(final Activity activity, final SkuDetails skuDetails, final String oldSku,
                                     int proration) {
        LOGGER.info("initiatePurchaseFlow");
        LOGGER.info("Current SKU = " + oldSku);
        LOGGER.info("New SKU = " + skuDetails.getSku());
        LOGGER.info("proration mode = " + proration);
        Runnable purchaseFlowRequest = () -> {
            LOGGER.info("Launching in-app purchase flow. Replace old SKU? " + (oldSku != null));
            BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            billingClient.launchBillingFlow(activity, purchaseParams);
        };

        executeServiceRequest(purchaseFlowRequest);
    }

    public void consumePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            //Nothing to do here
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    private void executeServiceRequest(Runnable runnable) {
        LOGGER.info("executeServiceRequest runnable = " + runnable);
        if (isServiceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesFinished(BillingResult billingResult, @NonNull List<Purchase> purchases) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (billingClient == null || billingResult.getResponseCode() != BillingResponseCode.OK) {
            LOGGER.warn("Billing client was null or result code (" + billingResult.getResponseCode()
                    + ") was bad - quitting");
            return;
        }

        LOGGER.debug("Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        onPurchasesUpdated(BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(),
                purchases);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        LOGGER.debug("Billing result code = " + billingResult.getResponseCode()
                + " debug message = " + billingResult.getDebugMessage());
        if (purchases == null || purchases.isEmpty()) {
            LOGGER.info("purchases is empty");
            return;
        }
        if (billingResult.getResponseCode() == BillingResponseCode.OK) {
            updatesListener.onPurchasesUpdated(purchases);
        } else if (billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED) {
            LOGGER.debug("onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            LOGGER.debug("onPurchasesUpdated() got unknown resultCode: " + billingResult.getResponseCode());
        }
    }

    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();

        void onPurchasesUpdated(List<Purchase> purchases);

        void onBillingError(int error);
    }
}