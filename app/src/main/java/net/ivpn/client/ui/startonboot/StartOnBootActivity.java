package net.ivpn.client.ui.startonboot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityStartOnBootBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class StartOnBootActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartOnBootActivity.class);
    private ActivityStartOnBootBinding binding;
    @Inject
    StartOnBootViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("AlwaysOnVpnActivity onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start_on_boot);
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
        binding.contentLayout.setViewmodel(viewModel);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.start_on_boot_title);
    }

    public void readMore(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ivpn.net/knowledgebase/239/IVPN-doesnandsharp039t-start-on-boot.html"));
        startActivity(browserIntent);
    }
}