package net.ivpn.client.vpn.local;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class PermissionActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionActivity.class);
    private static final String TAG = PermissionActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 12;

    @Inject VpnBehaviorController vpnBehaviorController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        LOGGER.info("onCreate");
        super.onCreate(savedInstanceState);
        checkVpnPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == REQUEST_CODE) {
            startVpn();
        }
        finish();
    }

    private void startVpn() {
        vpnBehaviorController.connectActionByRules();
    }

    private void checkVpnPermission() {
        Intent intent;
        try {
            intent = VpnService.prepare(this);
        } catch (Exception exception) {
            exception.printStackTrace();
            DialogBuilder.createNotificationDialog(this, Dialogs.FIRMWARE_ERROR);
            return;
        }

        if (intent != null) {
            try {
                startActivityForResult(intent, REQUEST_CODE);
            } catch (ActivityNotFoundException ane) {
                Log.d(TAG, "startVpnFromIntent: intent != null, ActivityNotFoundException !!!");
            }
        } else {
            onActivityResult(REQUEST_CODE, Activity.RESULT_OK, null);
        }
    }
}