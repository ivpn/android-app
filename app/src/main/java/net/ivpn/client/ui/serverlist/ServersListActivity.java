package net.ivpn.client.ui.serverlist;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.common.prefs.ServerType;
import net.ivpn.client.databinding.ActivityServerListBinding;
import net.ivpn.client.rest.data.model.Server;
import net.ivpn.client.ui.serverlist.fastest.FastestSettingActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ServersListActivity extends AppCompatActivity implements ServersListNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServersListActivity.class);

    private ActivityServerListBinding binding;
    private ServersListPagerAdapter adapter;
    @Inject
    ServersListCommonViewModel viewModel;

    private ServerType serverType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        initExtras();
        init();
        initToolbar();

//        Sentry.captureException(new IllegalArgumentException("Test"));
//        throw new IllegalArgumentException("Test");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGGER.info("onResume");
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
        if (adapter != null) {
            adapter.cancel();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.servers_list_title);
    }

    private void initExtras() {
        String action = getIntent().getAction();
        if (action != null && ServerType.contains(action)) {
            serverType = ServerType.valueOf(action);
        } else {
            LOGGER.info("incorrect action = " + action);
            finish();
        }
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_server_list);
        binding.setViewmodel(viewModel);
        adapter = new ServersListPagerAdapter(this, getSupportFragmentManager());
        binding.pager.setAdapter(adapter);
        binding.slidingTabs.setupWithViewPager(binding.pager);

        viewModel.start(this, serverType);
    }

    public void notifyFavouritesChanged(boolean isAdded) {
        int msgId = isAdded ? R.string.favourites_added : R.string.favourites_removed;
        SnackbarUtil.show(binding.coordinator, msgId, R.string.favourites_undo, view -> {
            if (adapter != null) {
                adapter.applyPendingAction();
            }
        });
    }

    @Override
    public void onServerSelected(Server server, Server forbiddenServer) {
        finish();
    }

    @Override
    public void onServerLongClick(Server server) {
    }

    @Override
    public void onFastestServerSelected() {
        finish();
    }

    @Override
    public void onFastestServerSettings() {
        Intent intent = new Intent(this, FastestSettingActivity.class);
        intent.setAction(serverType.toString());
        startActivity(intent);
    }

    public ServerType getServerType() {
        return serverType;
    }
}