package net.ivpn.client.ui.split;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.todtenkopf.mvvm.MenuCommandBindings;
import com.todtenkopf.mvvm.ViewModelActivity;
import com.todtenkopf.mvvm.ViewModelBase;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivitySplitTunnelingBinding;

import javax.inject.Inject;

public class SplitTunnelingActivity extends ViewModelActivity {

    @Inject
    SplitTunnelingViewModel viewModel;

    private ActivitySplitTunnelingBinding binding;

    @Nullable
    @Override
    protected ViewModelBase createViewModel() {
        init();
        addMenuBinding(R.id.action_select_all, viewModel.selectAllCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_deselect_all, viewModel.deselectAllCommand, MenuCommandBindings.EnableBinding.Visible);
        return viewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        initToolbar();
    }

    @Override
    protected void onResume() {
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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_split_tunneling, menu);
        return true;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.split_tunneling_title);
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_split_tunneling);
        binding.contentLayout.setViewmodel(viewModel);
        binding.contentLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getAllApplications();
    }

    private void getAllApplications() {
        viewModel.getApplicationsList(getPackageManager());
    }

}