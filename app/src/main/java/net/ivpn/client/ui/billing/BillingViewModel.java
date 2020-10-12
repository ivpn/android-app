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
            case INITIAL_PAYMENT:
            case PURCHASING:
            case VALIDATING:
                dataLoading.set(true);
                processDescription.set(context.getString(R.string.billing_validating));
                break;
            case CREATE_ACCOUNT:
                dataLoading.set(true);
                processDescription.set(context.getString(R.string.billing_creating_account));
                break;
            case CREATE_SESSION:
                dataLoading.set(true);
                processDescription.set(context.getString(R.string.billing_creating_new_session));
                break;
            case INITIAL_PAYMENT_ERROR:
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while creating your account. Contact our support or reopen the application to try again.");
                break;
            case ADD_FUNDS_ERROR:
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while adding funds to your account. Contact our support or reopen the application to try again.");
                break;
            case UPDATE_SESSION_ERROR:
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while updating your session. Contact our support or reopen the application to try again.");
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
        navigator.createPurchaseErrorDialog(String.valueOf(errorCode), errorMessage);
    }

    @Override
    public void onPurchaseAlreadyDone() {
        navigator.onPurchaseAlreadyDone();
    }

    @Override
    public void onCreateAccountFinish() {
        if (navigator != null) {
            navigator.onAccountCreated();
        }
    }

    @Override
    public void onAddFundsFinish() {
        if (navigator != null) {
            navigator.onAddFundsFinish();
        }
    }

    public void release() {
        billingManagerWrapper.removeBillingListener(this);
    }
}