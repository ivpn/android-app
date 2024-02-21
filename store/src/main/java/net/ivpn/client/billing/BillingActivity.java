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

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import net.ivpn.client.R;
import net.ivpn.client.StoreIVPNApplication;
import net.ivpn.client.databinding.ActivityBillingBinding;
import net.ivpn.core.v2.dialog.DialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class BillingActivity extends AppCompatActivity implements BillingNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingActivity.class);
    @Inject
    BillingViewModel viewModel;

    private ActivityBillingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        StoreIVPNApplication.instance.billingComponent.inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_billing);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.release();
    }

    private void init() {
        viewModel.setNavigator(this);
        binding.setViewmodel(viewModel);
        viewModel.start();
    }

    @Override
    public void onSuccessBilling() {
        LOGGER.info("onSuccessBilling");
        finish();
    }

    @Override
    public void onCredentialsError() {

    }

    @Override
    public void onPurchaseAlreadyDone() {
        finish();
    }

    @Override
    public void createPurchaseErrorDialog(String errorCode, String errorMessage) {
        DialogBuilder.createFullCustomNotificationDialog(this,
                this.getString(net.ivpn.core.R.string.dialogs_error) + " " + errorCode,
                errorMessage != null ? errorMessage : "", dialog -> {
                    BillingActivity.this.finish();
                });
    }

    @Override
    public void createDialog(String title, String message) {
        DialogBuilder.createFullCustomNotificationDialog(this,
                title,
                message, dialog -> {
                    BillingActivity.this.finish();
                });
    }

    @Override
    public void onAccountCreated() {
        finish();
    }

    @Override
    public void onAddFundsFinish() {
        finish();
    }
}
