package net.ivpn.client.ui.billing;

import android.content.Context;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.android.billingclient.api.SkuDetails;

import net.ivpn.client.R;
import net.ivpn.client.common.billing.BillingListener;
import net.ivpn.client.common.billing.BillingManagerWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

public class BillingViewModel implements BillingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingViewModel.class);

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableField<String> processDescription = new ObservableField<>();

    private BillingNavigator navigator;
    private BillingManagerWrapper billingManagerWrapper;
    private Context context;

    @Inject
    BillingViewModel(Context context, BillingManagerWrapper billingManagerWrapper) {
        LOGGER.info("Creating billing viewmodel");
        this.context = context;
        this.billingManagerWrapper = billingManagerWrapper;
        billingManagerWrapper.setBillingListener(this);
    }

    public void setNavigator(BillingNavigator navigator) {
        this.navigator = navigator;
    }

    public void start() {
        LOGGER.info("Start purchase");
        billingManagerWrapper.validatePurchase();
    }

    @Override
    public void onInitStateChanged(boolean isInit, int error) {
        //Nothing to do here
    }

    @Override
    public void onPurchaseStateChanged(BillingManagerWrapper.PurchaseState state) {
        LOGGER.info("PurchaseState = " + state);
        switch (state) {
            case NONE:
                dataLoading.set(false);
                break;
            case PURCHASING:
            case VALIDATING:
                dataLoading.set(true);
                processDescription.set(context.getString(R.string.billing_validating));
                break;
            case DONE:
                dataLoading.set(false);
                navigator.onSuccessBilling();
                break;
        }
    }

    @Override
    public void onCheckingSkuDetailsSuccess(List<SkuDetails> skuDetailsList) {
        //Nothing to do here
    }

    @Override
    public void onPurchaseError(int errorCode, String errorMessage) {
        navigator.createPurchaseErrorDialog(errorCode, errorMessage);

    }

    @Override
    public void onPurchaseAlreadyDone() {
        navigator.onPurchaseAlreadyDone();
    }

    public void release() {
        billingManagerWrapper.removeBillingListener(this);
    }
}