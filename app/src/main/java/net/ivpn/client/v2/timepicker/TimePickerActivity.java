package net.ivpn.client.v2.timepicker;

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

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.v2.dialog.DialogBuilder;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import javax.inject.Inject;

import static net.ivpn.client.v2.timepicker.PauseDelay.FIFTEEN_MINUTES;
import static net.ivpn.client.v2.timepicker.PauseDelay.FIVE_MINUTES;
import static net.ivpn.client.v2.timepicker.PauseDelay.ONE_HOUR;

public class TimePickerActivity extends AppCompatActivity implements OnDelayOptionSelected {

    @Inject
    VpnBehaviorController vpnBehaviorController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        showPredefinedTimePickerDialog();
    }

    private void showPredefinedTimePickerDialog() {
        DialogBuilder.createPredefinedTimePickerDialog(this, this);
    }

    private void showCustomTimePickerDialog() {
        DialogBuilder.createCustomTimePickerDialog(this, this);
    }

    @Override
    public void onDelayOptionSelected(PauseDelay pauseDelay) {
        switch (pauseDelay) {
            case FIVE_MINUTES: {
                vpnBehaviorController.pauseFor(FIVE_MINUTES.getDelay());
                finish();
                break;
            }
            case FIFTEEN_MINUTES: {
                vpnBehaviorController.pauseFor(FIFTEEN_MINUTES.getDelay());
                finish();
                break;
            }
            case ONE_HOUR: {
                vpnBehaviorController.pauseFor(ONE_HOUR.getDelay());
                finish();
                break;
            }
            case CUSTOM_DELAY: {
                showCustomTimePickerDialog();
                break;
            }
        }
    }

    @Override
    public void onCancelAction() {
        finish();
    }

    @Override
    public void onCustomDelaySelected(long delay) {
        if (delay != 0) {
            vpnBehaviorController.pauseFor(delay);
        }
        finish();
    }
}