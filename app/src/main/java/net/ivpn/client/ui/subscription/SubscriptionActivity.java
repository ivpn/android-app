package net.ivpn.client.ui.subscription;

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

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.dagger.ActivityComponent;
import net.ivpn.client.databinding.ActivitySubscriptionBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class SubscriptionActivity extends AppCompatActivity implements SubscriptionNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionActivity.class);

    private ActivitySubscriptionBinding binding;
    @Inject SubscriptionViewModel viewModel;
    public ActivityComponent activityComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        activityComponent = IVPNApplication.getApplication().appComponent.provideActivityComponent().create();
        activityComponent.inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("On creating");
        init();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    protected void onDestroy() {
        super.onDestroy();
        viewModel.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LOGGER.info("On Activity Result");
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.subscription_title);
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription);
        viewModel.setActivity(this);
        viewModel.setNavigator(this);
        viewModel.start();
        binding.contentLayout.setViewmodel(viewModel);
        binding.slidingTabs.setupWithViewPager(binding.contentLayout.pager);
        SubscriptionPagerAdapter adapter = new SubscriptionPagerAdapter(this, getSupportFragmentManager());
        binding.contentLayout.pager.setAdapter(adapter);
        binding.contentLayout.pager.setCurrentItem(1, false);
    }

    @Override
    public void showBillingManagerError() {
        DialogBuilder.createFullCustomNotificationDialog(this, getString(R.string.dialogs_error),
                getString(R.string.billing_error_message), dialog -> finish());

    }
}