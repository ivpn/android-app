package net.ivpn.client.ui.billing;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityBillingBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class BillingActivity extends AppCompatActivity implements BillingNavigator{

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingActivity.class);
    @Inject BillingViewModel viewModel;
    private ActivityBillingBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_billing);
        init();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.release();
    }

    private void init() {
        viewModel.setNavigator(this);
        binding.setViewmodel(viewModel);
        viewModel.start();
    }

    @Override
    public void onSuccessBilling() {
        LOGGER.info("onSuccessBilling");
        finish();
    }

    @Override
    public void onCredentialsError() {

    }

    @Override
    public void onPurchaseAlreadyDone() {
        finish();
    }

    @Override
    public void createPurchaseErrorDialog(String errorCode, String errorMessage) {
        DialogBuilder.createFullCustomNotificationDialog(this,
                this.getString(R.string.dialogs_error) + " " + errorCode,
                errorMessage != null ? errorMessage : "", dialog -> {
                    BillingActivity.this.finish();
                });
    }

    @Override
    public void onAccountCreated() {
        finish();
    }

    @Override
    public void onAddFundsFinish() {
        finish();
    }
}
