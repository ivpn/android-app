package net.ivpn.client.common.billing;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.common.dagger.ApplicationScope;

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
    private BillingClient billingClient;

    private final List<Purchase> purchases = new ArrayList<>();

    /* BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    private static final String BASE_64_ENCODED_PUBLIC_KEY = BuildConfig.BILLING_PUBLIC_KEY;

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
            public void onBillingSetupFinished(BillingResult billingResult) {
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
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
            LOGGER.info("Querying subscription elapsed time: " + (System.currentTimeMillis() - time)
                    + "ms");
            LOGGER.info("Querying subscriptions result code: "
                    + purchasesResult.getResponseCode()
                    + " res: " + (purchasesResult.getPurchasesList() != null ? purchasesResult.getPurchasesList().size() : null));

            if (purchasesResult.getResponseCode() != BillingResponseCode.OK) {
                LOGGER.info("Got an error response trying to query subscription purchases");
            }

            onQueryPurchasesFinished(purchasesResult);
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
                    .setOldSku(oldSku)
                    .setReplaceSkusProrationMode(proration)
                    .build();
            billingClient.launchBillingFlow(activity, purchaseParams);
        };

        executeServiceRequest(purchaseFlowRequest);
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
    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (billingClient == null || result.getResponseCode() != BillingResponseCode.OK) {
            LOGGER.warn("Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad - quitting");
            return;
        }

        LOGGER.debug("Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        purchases.clear();
        onPurchasesUpdated(BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(),
                result.getPurchasesList());
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * See {@link Security#verifyPurchase(String, String, String)}
     * </p>
     *
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            LOGGER.error("Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return;
        }

        LOGGER.debug("Got a verified purchase: " + purchase);

        purchases.add(purchase);
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
//        if (BASE_64_ENCODED_PUBLIC_KEY.contains("CONSTRUCT_YOUR")) {
//            throw new RuntimeException("Please update your app's public key at: "
//                    + "BASE_64_ENCODED_PUBLIC_KEY");
//        }

        try {
            return Security.verifyPurchase(BASE_64_ENCODED_PUBLIC_KEY, signedData, signature);
        } catch (IOException e) {
            LOGGER.debug("Got an exception trying to validate a purchase: " + e);
            return false;
        }
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
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
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