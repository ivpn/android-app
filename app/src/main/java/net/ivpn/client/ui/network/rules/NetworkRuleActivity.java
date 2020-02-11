package net.ivpn.client.ui.network.rules;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.utils.LogUtil;
import net.ivpn.client.databinding.ActivityNetworkRuleBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class NetworkRuleActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkRuleActivity.class);

    private ActivityNetworkRuleBinding binding;
    @Inject
    NetworkRuleViewModel viewModel;

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

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.network_rule_title);
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_network_rule);
        binding.contentLayout.setViewmodel(viewModel);
    }
}
