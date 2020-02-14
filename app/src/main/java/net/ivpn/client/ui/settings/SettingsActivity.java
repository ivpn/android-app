package net.ivpn.client.ui.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.Constant;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.databinding.ActivitySettingsBinding;
import net.ivpn.client.ui.alwaysonvpn.AlwaysOnVpnActivity;
import net.ivpn.client.ui.customdns.CustomDNSActivity;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.network.NetworkActivity;
import net.ivpn.client.ui.policy.PrivacyPolicyActivity;
import net.ivpn.client.ui.protocol.ProtocolActivity;
import net.ivpn.client.ui.serverlist.ServersListActivity;
import net.ivpn.client.ui.split.SplitTunnelingActivity;
import net.ivpn.client.ui.startonboot.StartOnBootActivity;
import net.ivpn.client.ui.subscription.SubscriptionActivity;
import net.ivpn.client.ui.surveillance.AntiSurveillanceActivity;
import net.ivpn.client.ui.terms.TermsOfServiceActivity;
import net.ivpn.client.ui.tutorial.TutorialActivity;
import net.ivpn.client.ui.updates.UpdatesActivity;
import net.ivpn.client.vpn.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import javax.inject.Inject;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class SettingsActivity extends AppCompatActivity
        implements SettingsNavigator, AdvancedKillSwitchActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsActivity.class);

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private ActivitySettingsBinding binding;
    @Inject
    SettingsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
        LOGGER.info("onResume");
        super.onResume();
        viewModel.onResume();
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
        viewModel.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: RESULT_CANCELED");
            return;
        }
        Log.d(TAG, "onActivityResult: RESULT_OK");

        switch (requestCode) {
            case ServiceConstants.DISABLE_KILL_SWITCH: {
                Log.d(TAG, "onActivityResult: DISABLE_KILL_SWITCH");
                viewModel.setKillSwitchState(false);
                break;
            }
            case ServiceConstants.ENABLE_KILL_SWITCH: {
                Log.d(TAG, "onActivityResult: ENABLE_KILL_SWITCH");
                viewModel.setKillSwitchState(true);
                break;
            }
        }
    }

    private void init() {
        viewModel.setNavigator(this);
        binding.contentLayout.setViewmodel(viewModel);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_title);
    }

    public void onLogoutClick(View view) {
        LOGGER.info("onLogoutClick");
        DialogBuilder.createOptionDialog(this, Dialogs.LOGOUT, (dialogInterface, which) -> release());
    }

    public void authenticate(View view) {
        LOGGER.info("authenticate");
        authenticate();
    }

    public void skip(View view) {
        LOGGER.info("skip");
        viewModel.cancel();
        logout();
    }

    private void release() {
        Log.d(TAG, "release: ");
        viewModel.logout();
    }

    @Override
    public void authenticate() {
        LOGGER.info("authenticate");
        startSingleTopActivity(new Intent(this, TutorialActivity.class));
    }

    @Override
    public void subscribe() {
        LOGGER.info("subscribe");
        startSingleTopActivity(new Intent(this, SubscriptionActivity.class));
    }

    public void logout() {
        LOGGER.info("logout");
    }

    private void checkVPNPermission(int requestCode) {
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
                startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException ane) {
                Log.d(TAG, "startVpnFromIntent: intent != null, ActivityNotFoundException !!!");
            }
        } else {
            onActivityResult(requestCode, Activity.RESULT_OK, null);
        }
    }

    public void chooseExitServer(View view) {
        viewModel.chooseServer(ServerType.EXIT);
    }

    public void chooseEntryServer(View view) {
        viewModel.chooseServer(ServerType.ENTRY);
    }

    public void splitTunneling(View view) {
        viewModel.splitTunneling();
    }

    public void vpnProtocol(View view) {
        LOGGER.info("vpnProtocol");
        if (!viewModel.authenticated.get()) {
            authenticate();
            return;
        } else if (!viewModel.isActive()) {
            subscribe();
            return;
        }

        if (viewModel.isVpnActive()) {
            notifyUser(R.string.snackbar_to_change_protocol_disconnect,
                    R.string.snackbar_disconnect_first, null);
        } else {
            Intent intent = new Intent(this, ProtocolActivity.class);
            startSingleTopActivity(intent);
        }

    }

    public void trustedWifi(View view) {
        LOGGER.info("trustedWifi");
        if (!viewModel.authenticated.get()) {
            authenticate();
            return;
        } else if (!viewModel.isActive()) {
            subscribe();
            return;
        }

        startSingleTopActivity(new Intent(this, NetworkActivity.class));
    }

    public void readPrivacyPolicy(View view) {
        LOGGER.info("readPrivacyPolicy");
        startSingleTopActivity(new Intent(this, PrivacyPolicyActivity.class));
    }

    public void readTermsOfService(View view) {
        LOGGER.info("readTermsOfService");
        startSingleTopActivity(new Intent(this, TermsOfServiceActivity.class));
    }

    public void checkUpdates(View view) {
        LOGGER.info("check for updates");
        startSingleTopActivity(new Intent(this, UpdatesActivity.class));
    }

    public void sendLogs(View view) {
        LOGGER.info("sendLogs");
        ArrayList<Uri> uris = new ArrayList<>();
        Uri uri = viewModel.getLogFileUri(this);
        uris.add(uri);

        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constant.SUPPORT_EMAIL});
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    public void alwaysOnVpn(View view) {
        LOGGER.info("alwaysOnVpn");
        Intent intent = new Intent(this, AlwaysOnVpnActivity.class);
        startSingleTopActivity(intent);
    }

    public void startOnSystemBoot(View view) {
        LOGGER.info("startOnSystemBoot");
        if (!viewModel.authenticated.get()) {
            authenticate();
            return;
        } else if (!viewModel.isActive()) {
            subscribe();
            return;
        }

        Intent intent = new Intent(this, StartOnBootActivity.class);
        startSingleTopActivity(intent);
    }

    public void antiSurveillance(View view) {
        viewModel.antiTracker();
    }

    public void customDns(View view) {
        viewModel.customDNS();
    }

    public void manageSubscription(View view) {
        LOGGER.info("renew");
        startSingleTopActivity(new Intent(this, SubscriptionActivity.class));
    }

    public void resubscribe(View view) {
        LOGGER.info("resubscribe");
        startActivity(new Intent(Intent.ACTION_VIEW, viewModel.getSubscriptionUri()));
    }

    @Override
    public void splitTunneling() {
        LOGGER.info("splitTunneling");
        startSingleTopActivity(new Intent(this, SplitTunnelingActivity.class));
    }

    @Override
    public void customDNS() {
        LOGGER.info("customDns");
        startSingleTopActivity(new Intent(this, CustomDNSActivity.class));
    }

    @Override
    public void antiTracker() {
        LOGGER.info("antiSurveillance");
        startSingleTopActivity(new Intent(this, AntiSurveillanceActivity.class));
    }

    @Override
    public void chooseServer(ServerType serverType) {
        LOGGER.info("chooseServer");
        Intent intent = new Intent(this, ServersListActivity.class);
        intent.setAction(serverType.toString());
        startSingleTopActivity(intent);
    }

    @Override
    public void notifyUser(int msgId, int actionId, View.OnClickListener listener) {
        SnackbarUtil.show(binding.coordinator, msgId, actionId, listener);
    }

    @Override
    public void enableKillSwitch(boolean value, boolean isAdvancedKillSwitchDialogEnabled) {
        LOGGER.info("enableKillSwitch = " + value + " isAdvancedKillSwitchDialogEnabled = " + isAdvancedKillSwitchDialogEnabled);
        if (value) {
            checkVPNPermission(ServiceConstants.ENABLE_KILL_SWITCH);
            if (isAdvancedKillSwitchDialogEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DialogBuilder.createAdvancedKillSwitchDialog(this, this);
            }
        } else {
            checkVPNPermission(ServiceConstants.DISABLE_KILL_SWITCH);
        }
    }

    @Override
    public void enableAdvancedKillSwitchDialog(boolean enable) {
        LOGGER.info("enableAdvancedKillSwitchDialog");
        viewModel.enableAdvancedKillSwitchDialog(enable);
    }

    @Override
    public void openDeviceSettings() {
        LOGGER.info("openDeviceSettings");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));
            } catch (ActivityNotFoundException exception) {
                DialogBuilder.createNotificationDialog(this, Dialogs.NO_VPN_SETTINGS);
            }
        }
    }

    private void startSingleTopActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}