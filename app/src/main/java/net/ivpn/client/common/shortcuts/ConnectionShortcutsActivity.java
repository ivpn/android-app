package net.ivpn.client.common.shortcuts;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ConnectionShortcutsActivity extends AppCompatActivity {

    public static final String TAG = ConnectionShortcutsActivity.class.getSimpleName();
    public static final String SHORTCUT_CONNECTION = "net.ivpn.client.SHORTCUT_CONNECTION";
    public static final String SHORTCUT_DISCONNECTION = "net.ivpn.client.SHORTCUT_DISCONNECTION";
    private static final int REQUEST_CODE = 42;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionShortcutsActivity.class);

    @Inject
    ConnectionShortcutsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LOGGER.info("ConnectionShortcutsActivity onCreate");
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        if (!viewModel.isCredentialsExist()) {
            LOGGER.info("No credentials");
            navigateToLogin();
            finish();
        } else if (SHORTCUT_CONNECTION.equals(getIntent().getAction())) {
            LOGGER.info("handle connection action");
            checkVpnPermission();
        } else if (SHORTCUT_DISCONNECTION.equals(getIntent().getAction())) {
            LOGGER.info("handle disconnection action");
            viewModel.stopVpn();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == REQUEST_CODE) {
            viewModel.startVpn();
        }
        finish();
    }

    private void checkVpnPermission() {
        LOGGER.info("Check VPN permission");
        Intent intent;
        try {
            intent = VpnService.prepare(this);
        } catch (Exception exception) {
            LOGGER.error("Error while getting VPN permission", exception);
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

    private void navigateToLogin() {
    }
}
