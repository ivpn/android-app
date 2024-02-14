package net.ivpn.client.billing;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.android.billingclient.api.ProductDetails;

import net.ivpn.client.StoreIVPNApplication;
import net.ivpn.core.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

public class BillingViewModel implements BillingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingViewModel.class);

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableField<String> processDescription = new ObservableField<>();

    private BillingNavigator navigator;
    private final BillingManagerWrapper billingManagerWrapper;

    @Inject
    BillingViewModel(BillingManagerWrapper billingManagerWrapper) {
        LOGGER.info("Creating billing viewmodel");
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
            case NONE -> dataLoading.set(false);
            case INITIAL_PAYMENT, PURCHASING, VALIDATING -> {
                dataLoading.set(true);
                processDescription.set(getString(R.string.billing_validating));
            }
            case CREATE_ACCOUNT -> {
                dataLoading.set(true);
                processDescription.set(getString(R.string.billing_creating_account));
            }
            case CREATE_SESSION -> {
                dataLoading.set(true);
                processDescription.set(getString(R.string.billing_creating_new_session));
            }
            case INITIAL_PAYMENT_ERROR -> {
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while creating your account. Contact our support or reopen the application to try again.");
            }
            case ADD_FUNDS_ERROR -> {
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while adding funds to your account. Contact our support or reopen the application to try again.");
            }
            case PURCHASE_PENDING -> {
                dataLoading.set(false);
                navigator.createDialog("Pending payment", "Payment is pending for approval. We will complete the transaction as soon as payment is approved.");
            }
            case UPDATE_SESSION_ERROR -> {
                dataLoading.set(false);
                navigator.createPurchaseErrorDialog("", "There was an error while updating your session. Contact our support or reopen the application to try again.");
            }
            case DONE -> {
                dataLoading.set(false);
                navigator.onSuccessBilling();
            }
        }
    }

    @Override
    public void onCheckingProductDetailsSuccess(List<ProductDetails> productDetailsList) {
        // Nothing to do here
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

    private String getString(int resId) {
        return StoreIVPNApplication.instance.getString(resId);
    }
}