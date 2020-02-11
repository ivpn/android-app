package net.ivpn.client.ui.customdns;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityCustomDnsBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class CustomDNSActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDNSActivity.class);
    private ActivityCustomDnsBinding binding;
    @Inject
    CustomDNSViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("AlwaysOnVpnActivity onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_dns);
        init();
        initToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void changeDNS(View view) {
        DialogBuilder.createCustomDNSDialogue(this, dns -> {
            viewModel.setDnsAs(dns);
        });
    }

    private void init() {
        binding.contentLayout.setViewmodel(viewModel);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.custom_dns_title);
    }
}