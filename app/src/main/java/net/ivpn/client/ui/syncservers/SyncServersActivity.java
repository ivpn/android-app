package net.ivpn.client.ui.syncservers;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivitySyncServersBinding;
import net.ivpn.client.ui.connect.ConnectActivity;

import javax.inject.Inject;

public class SyncServersActivity extends AppCompatActivity implements SyncServersNavigator {

    private ActivitySyncServersBinding binding;
    @Inject
    SyncServersViewModel viewmodel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewmodel.syncServers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewmodel.release();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync_servers);

        viewmodel.setNavigator(this);
        binding.setViewmodel(viewmodel);
    }

    public void retry(View view) {
        viewmodel.syncServers();
    }

    @Override
    public void onGetServers() {
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}