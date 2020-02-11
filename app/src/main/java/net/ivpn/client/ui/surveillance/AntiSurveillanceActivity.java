package net.ivpn.client.ui.surveillance;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityAntiSurveillanceBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class AntiSurveillanceActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntiSurveillanceActivity.class);
    private ActivityAntiSurveillanceBinding binding;
    @Inject
    AntiSurveillanceViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("AlwaysOnVpnActivity onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_anti_surveillance);
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

    private void init() {
        binding.contentLayout.setViewmodel(viewModel);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.settings_anti_surveillance_title);
    }

    public void readMore(View view) {
        Intent openURL = new Intent(android.content.Intent.ACTION_VIEW);
        openURL.setData(Uri.parse("https://www.ivpn.net/antitracker"));
        startActivity(openURL);
    }

    public void readMoreHardcore(View view) {
        Intent openURL = new Intent(android.content.Intent.ACTION_VIEW);
        openURL.setData(Uri.parse("https://www.ivpn.net/antitracker/hardcore"));
        startActivity(openURL);
    }
}
