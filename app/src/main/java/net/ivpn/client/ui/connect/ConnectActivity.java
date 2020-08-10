package net.ivpn.client.ui.connect;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.databinding.DataBindingUtil;

import com.todtenkopf.mvvm.MenuCommandBindings;
import com.todtenkopf.mvvm.ViewModelActivity;
import com.todtenkopf.mvvm.ViewModelBase;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.common.utils.IntentUtils;
import net.ivpn.client.databinding.ActivityConnectBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.privateemails.PrivateEmailsActivity;
import net.ivpn.client.ui.subscription.SubscriptionActivity;
import net.ivpn.client.ui.tutorial.TutorialActivity;
import net.ivpn.client.ui.updates.UpdatesJobServiceUtil;
import net.ivpn.client.vpn.ServiceConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ConnectActivity extends ViewModelActivity implements ConnectionNavigator, CreateSessionNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectActivity.class);

    private ActivityConnectBinding binding;
    @Inject
    ConnectViewModel viewModel;
    @Inject
    UpdatesJobServiceUtil updatesJobServiceUtil;

    private GestureDetectorCompat gestureDetector;
    private CreateSessionFragment createSessionFragment;

    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (binding.contentLayout.connectionView.validateMainTouchEvent(event)) {
                handleTapToConnect();
            } else if (binding.contentLayout.connectionView.validatePauseTouchEvent(event)) {
                handleTapToPause();
            } else if (binding.contentLayout.connectionView.validateStopTouchEvent(event)) {
                handleTapToStop();
            }

            return super.onSingleTapConfirmed(event);
        }
    };

    @Nullable
    @Override
    protected ViewModelBase createViewModel() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect);
        gestureDetector = new GestureDetectorCompat(this, gestureListener);
        viewModel.setNavigator(this);
        binding.contentLayout.setViewmodel(viewModel);
        binding.contentLayout.connectionView.setOnTouchListener((view, event) -> {
            ConnectActivity.this.gestureDetector.onTouchEvent(event);
            // Be sure to call the superclass implementation
            return true;
        });

        return viewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");

        initToolbar();
        tryKillSwitch();
        tryWifiWatcher();

        updatesJobServiceUtil.pushUpdateJob(IVPNApplication.getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGGER.info("onResume");
        viewModel.onResume();
        checkLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOGGER.info("onStart");
        viewModel.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            LOGGER.info("onActivityResult: RESULT_CANCELED");
            return;
        }
        LOGGER.info("onActivityResult: RESULT_OK");

        switch (requestCode) {
            case ServiceConstants.IVPN_REQUEST_CODE: {
//                viewModel.onConnectRequest();
                break;
            }
            case ServiceConstants.KILL_SWITCH_REQUEST_CODE: {
                viewModel.startKillSwitch();
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        addMenuBinding(R.id.action_private_emails, viewModel.privateEmailCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_new_private_emails, viewModel.newPrivateEmailCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_settings, viewModel.settingsCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_info, viewModel.infoCommand, MenuCommandBindings.EnableBinding.Visible);

        return true;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(" ");
    }

    @Override
    public void askConnectionPermission() {

    }

    public void openSettings() {
    }

    public void openPrivateEmails() {
        LOGGER.info("openPrivateEmails");
        Intent intent = new Intent(this, PrivateEmailsActivity.class);
        startSingleTopActivity(intent);
    }

    private void tryWifiWatcher() {
        viewModel.tryWifiWatcher();
    }

    private void handleTapToConnect() {
        LOGGER.info("handleTapToConnect");
        if (!viewModel.isCredentialsExist()) {
            authenticate();
        } else if (!viewModel.isActive()) {
            subscribe();
        } else if (viewModel.isVpnActive()) {
//            viewModel.onConnectRequest();
        } else {
            checkVPNPermission(ServiceConstants.IVPN_REQUEST_CODE);
        }
    }

    private void authenticate() {
        LOGGER.info("authenticate");
        Intent intent = new Intent(this, TutorialActivity.class);
        startSingleTopActivity(intent);
    }

    private void subscribe() {
        LOGGER.info("subscribe");
        if (BuildConfig.BUILD_VARIANT.equals("site") || BuildConfig.BUILD_VARIANT.equals("fdroid")) {
            openWebsite();
        } else {
            Intent intent = new Intent(this, SubscriptionActivity.class);
            startSingleTopActivity(intent);
        }
    }

    private void openWebsite() {
        Intent intent = IntentUtils.INSTANCE.createWebSignUpIntent();

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void handleTapToPause() {
        LOGGER.info("handleTapToPause");
        viewModel.onPauseRequest();
    }

    private void handleTapToStop() {
        LOGGER.info("handleTapToStop");
        viewModel.onStopRequest();
    }

    private boolean isPermissionGranted() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void askPermissionRationale() {
        DialogBuilder.createNonCancelableDialog(this, Dialogs.ASK_LOCATION_PERMISSION,
                (dialog, which) -> goToAndroidAppSettings(),
                dialog -> viewModel.applyNetworkFeatureState(false));
    }

    private void goToAndroidAppSettings() {
        LOGGER.info("goToAndroidAppSettings");
        String action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
        Uri uri = Uri.parse(getString(R.string.settings_package) + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(action, uri);
        startActivity(intent);
    }

    private void checkVPNPermission(int requestCode) {
        LOGGER.info("checkVPNPermission");
        Intent intent;
        try {
            intent = VpnService.prepare(this);
        } catch (Exception exception) {
            exception.printStackTrace();
            openErrorDialog(Dialogs.FIRMWARE_ERROR);
            return;
        }

        if (intent != null) {
            try {
                startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException ane) {
                LOGGER.error("Error while checking VPN permission", ane);
            }
        } else {
            onActivityResult(requestCode, Activity.RESULT_OK, null);
        }
    }

    public void logout() {
        LOGGER.info("logout");
        viewModel.logout();

        Intent intent = new Intent(this, TutorialActivity.class);
        startSingleTopActivity(intent);
        finish();
    }

    @Override
    public void openSessionLimitReachedDialogue() {
        createSessionFragment = new CreateSessionFragment();
        createSessionFragment.show(getSupportFragmentManager(), createSessionFragment.getTag());
    }

    @Override
    public void accountVerificationFailed() {
        LOGGER.info("accountVerificationFailed");
        DialogBuilder.createNonCancelableDialog(this, Dialogs.SESSION_HAS_EXPIRED,
                (dialogInterface, i) -> {
                    LOGGER.info( "onClick: ");
                    logout();
                },
                dialogInterface -> {
                    LOGGER.info( "onCancel: ");
                    logout();
                });
    }

//    @Override
//    public void onConnectionStateChanged(ConnectionState state) {
//        binding.contentLayout.connectionView.updateConnectionState(state);
//    }

    @Override
    public void openNoNetworkDialog() {
        openErrorDialog(Dialogs.CONNECTION_ERROR);
    }

    @Override
    public void openErrorDialog(Dialogs dialogs) {
        DialogBuilder.createNotificationDialog(this, dialogs);
    }

    @Override
    public void onChangeConnectionStatus(ConnectionState state) {

    }

    public void chooseExitServer(View view) {
        LOGGER.info("chooseExitServer");
        viewModel.chooseServer(ServerType.EXIT);
    }

    public void chooseEntryServer(View view) {
        LOGGER.info("chooseEntryServer");
        viewModel.chooseServer(ServerType.ENTRY);
    }

//    @Override
//    public void chooseServer(ServerType serverType) {
//        LOGGER.info("chooseServer serverType = " + serverType);
//        Intent intent = new Intent(this, ServersListActivity.class);
//        intent.setAction(serverType.toString());
//        startSingleTopActivity(intent);
//    }
//
//    @Override
//    public void openInfoDialogue() {
//        LOGGER.info("openInfoDialogue");
//        DialogBuilder.createConnectionInfoDialog(this);
//    }

    @Override
    public void onAuthFailed() {
        LOGGER.info("onAuthFailed");
        disconnectVpnService(true, Dialogs.ON_CONNECTION_AUTHENTICATION_ERROR,
                (dialogInterface, i) -> {
                    LOGGER.info( "onClick: ");
                    logout();
                });
    }

    @Override
    public void onTimeOut() {
//        LOGGER.info("onTimeOut");
//        disconnectVpnService(true, Dialogs.TRY_RECONNECT,
//                (dialogInterface, i) -> viewModel.onConnectRequest());
    }

    public void disconnect(View view) {
        LOGGER.info("disconnect");
        viewModel.disconnect();
    }

    @Override
    public void notifyAnotherPortUsedToConnect() {
        LOGGER.info("notifyAnotherPortUsedToConnect");
        binding.contentLayout.connectionView.reset();
        new Handler().postDelayed(() -> SnackbarUtil.show(binding.coordinator, R.string.snackbar_new_try_with_different_port,
                R.string.snackbar_disconnect_first, null), 500);
    }

    private void disconnectVpnService(boolean needToReset, Dialogs dialog,
                                      DialogInterface.OnClickListener listener) {
        if (needToReset) {
            binding.contentLayout.connectionView.reset();
        }

        if (dialog != null) {
            DialogBuilder.createOptionDialog(this, dialog, listener);
        }
    }

    private void tryKillSwitch() {
        if (viewModel.isKillSwitchShouldBeStarted()) {
            checkVPNPermission(ServiceConstants.KILL_SWITCH_REQUEST_CODE);
        }
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return;
        }
        boolean isPermissionGranted = isPermissionGranted();
        boolean isEnabled = viewModel.areNetworkRulesEnabled.get();
        if (!isEnabled) {
            return;
        }
        if (isPermissionGranted) {
            viewModel.applyNetworkFeatureState(true);
            return;
        }
        askPermissionRationale();
    }

    @Override
    public void onForceLogout() {
//        viewModel.createNewSession(true);
        createSessionFragment.dismissAllowingStateLoss();
    }

    @Override
    public void tryAgain() {
//        viewModel.createNewSession(false);
        createSessionFragment.dismissAllowingStateLoss();
    }

    @Override
    public void cancel() {
        createSessionFragment.dismissAllowingStateLoss();
    }

    private void startSingleTopActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}