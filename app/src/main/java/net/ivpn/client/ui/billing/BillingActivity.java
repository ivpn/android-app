package net.ivpn.client.ui.billing;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.billing.BillingManager;
import net.ivpn.client.databinding.ActivityBillingBinding;
import net.ivpn.client.ui.connect.ConnectActivity;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.login.LoginActivity;

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
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    @Override
    public void onCredentialsError() {

    }

    @Override
    public void onPurchaseAlreadyDone() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    @Override
    public void createPurchaseErrorDialog(int errorCode, String errorMessage) {
        DialogBuilder.createFullCustomNotificationDialog(this,
                this.getString(R.string.dialogs_error) + " " + errorCode,
                errorMessage != null ? errorMessage : "", dialog -> {
                    BillingActivity.this.finish();
                });
    }
}
