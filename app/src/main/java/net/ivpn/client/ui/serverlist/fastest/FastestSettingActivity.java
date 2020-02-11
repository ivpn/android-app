package net.ivpn.client.ui.serverlist.fastest;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.utils.LogUtil;
import net.ivpn.client.common.utils.ToastUtil;
import net.ivpn.client.databinding.ActivityFastestSettingsBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class FastestSettingActivity extends AppCompatActivity implements FastestSettingNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastestSettingActivity.class);

    @Inject
    FastestSettingViewModel viewModel;
    private ActivityFastestSettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        LOGGER.info("onCreate");
        super.onCreate(savedInstanceState);
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.fastest_setting_title);
    }

    private void init() {
        viewModel.setNavigator(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fastest_settings);
        binding.contentLayout.setViewmodel(viewModel);

        FastestSettingViewAdapter adapter = new FastestSettingViewAdapter();
        binding.contentLayout.recyclerView.setAdapter(adapter);
        binding.contentLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void showToast() {
        ToastUtil.toast(R.string.fastest_setting_toast);
    }
}
