package net.ivpn.client.ui.timepicker;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.vpn.controller.VpnBehaviorController;

import javax.inject.Inject;

import static net.ivpn.client.ui.timepicker.PauseDelay.FIFTEEN_MINUTES;
import static net.ivpn.client.ui.timepicker.PauseDelay.FIVE_MINUTES;
import static net.ivpn.client.ui.timepicker.PauseDelay.ONE_HOUR;

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