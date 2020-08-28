package net.ivpn.client.common.billing;

import com.android.billingclient.api.SkuDetails;
import net.ivpn.client.common.billing.BillingManagerWrapper.PurchaseState;

import java.util.List;

public interface BillingListener {

    void onInitStateChanged(boolean isInit, int error);

    void onPurchaseStateChanged(PurchaseState state);

    void onCheckingSkuDetailsSuccess(List<SkuDetails> skuDetailsList);

    void onPurchaseError(int errorStatus, String errorMessage);

    void onPurchaseAlreadyDone();

    void onCreateAccountFinish();

    void onAddFundsFinish();

}