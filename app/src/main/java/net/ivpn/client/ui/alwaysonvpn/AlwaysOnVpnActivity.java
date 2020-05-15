package net.ivpn.client.ui.alwaysonvpn;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.FragmentAlwaysOnVpnBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class AlwaysOnVpnActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlwaysOnVpnActivity.class);
    private FragmentAlwaysOnVpnBinding binding;
    @Inject
    AlwaysOnVpnViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("AlwaysOnVpnActivity onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_always_on_vpn);
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
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

    private void init() {
//        binding.contentLayout.setViewmodel(viewModel);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_always_on_vpn_title);
    }

    public void deviceVpnSettings(View view) {
        LOGGER.info("Navigate to VPNs list device settings screen");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));
            } catch (ActivityNotFoundException exception) {
                exception.printStackTrace();
                LOGGER.error("Error while navigating to VPN device settings");
                DialogBuilder.createNotificationDialog(this, Dialogs.ALWAYS_ON_VPN_NOT_SUPPORTED);
            }
        }
    }
}