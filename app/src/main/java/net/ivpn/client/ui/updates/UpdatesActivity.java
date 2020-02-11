package net.ivpn.client.ui.updates;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityUpdatesBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UpdatesActivity extends AppCompatActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesActivity.class);

    private ActivityUpdatesBinding binding;
    @Inject
    UpdatesViewModel viewmodel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("On create");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_updates);
        initToolbar();
        init();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.updates_info_title);
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
        binding.contentLayout.setViewmodel(viewmodel);
    }
}
