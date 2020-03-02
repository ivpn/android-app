package net.ivpn.client.ui.network;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityNetworkBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.network.rules.NetworkRuleActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NetworkActivity extends AppCompatActivity implements NetworkNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkActivity.class);
    private static final int LOCATION_PERMISSION_CODE = 132;

    private ActivityNetworkBinding binding;
    private NetworkRecyclerViewAdapter adapter;
    @Inject
    NetworkViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
        LOGGER.info("onResume");
        super.onResume();
        checkLocationPermission();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return false;
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if (grantResults.length == 0) {
                    break;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.applyNetworkFeatureState(true);
                    break;
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    viewModel.applyNetworkFeatureState(false);
                    break;
                }
            }
        }
    }

    public void toRules(View view) {
        LOGGER.info("toRules");
        Intent intent = new Intent(this, NetworkRuleActivity.class);
        startActivity(intent);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.network_title);
    }

    private void init() {
        viewModel.setNavigator(this);
        adapter = new NetworkRecyclerViewAdapter();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_network);
        binding.contentLayout.setViewmodel(viewModel);
        binding.contentLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.contentLayout.recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean shouldAskForLocationPermission() {
        LOGGER.info("shouldAskForLocationPermission");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return false;
        }
        if (isPermissionGranted()) {
            return false;
        }
        if (shouldRequestRationale()) {
            askPermissionRationale();
        } else {
            showInformationDialog();
        }
        return true;
    }

    private void askPermissionRationale() {
        LOGGER.info("askPermissionRationale");
        DialogBuilder.createNonCancelableDialog(this, Dialogs.ASK_LOCATION_PERMISSION,
                (dialog, which) -> goToAndroidAppSettings(),
                dialog -> viewModel.applyNetworkFeatureState(false));
    }

    private void showInformationDialog() {
        LOGGER.info("showInformationDialog");
        DialogBuilder.createNonCancelableDialog(this, Dialogs.LOCATION_PERMISSION_INFO,
                null, dialog -> askPermission());
    }

    private boolean isPermissionGranted() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private boolean shouldRequestRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    private void goToAndroidAppSettings() {
        LOGGER.info("goToAndroidAppSettings");
        String action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
        Uri uri = Uri.parse(getString(R.string.settings_package) + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(action, uri);
        startActivity(intent);
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return;
        }
        boolean isPermissionGranted = isPermissionGranted();
        boolean isEnabled = viewModel.isNetworkFeatureEnabled.get();
        if (!isEnabled) {
            return;
        }
        if (isPermissionGranted) {
            viewModel.applyNetworkFeatureState(true);
            return;
        }
        askPermissionRationale();
    }
}