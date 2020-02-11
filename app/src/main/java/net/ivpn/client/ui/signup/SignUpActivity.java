package net.ivpn.client.ui.signup;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.utils.KeyboardUtil;
import net.ivpn.client.databinding.ActivitySignupBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.login.LoginActivity;
import net.ivpn.client.ui.subscription.SubscriptionActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class SignUpActivity extends AppCompatActivity implements SignUpNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUpActivity.class);
    @Inject SignUpViewModel viewModel;
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        initToolbar();
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

    private void init() {
        viewModel.setNavigator(this);
        binding.contentLayout.setViewmodel(viewModel);
        binding.contentLayout.signUpPass.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signUp(textView);
                return true;
            }
            return false;
        });

        KeyboardUtil.addKeyboardToggleListener(this, isVisible -> {
            binding.contentLayout.image.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.sign_up_title);
    }

    public void logIn(View view) {
        LOGGER.info("logIn");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signUp(View view) {
        LOGGER.info("signUp");
        viewModel.signUp();
    }

    @Override
    public void onSignUp() {
        Intent intent = new Intent(this, SubscriptionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEmailFormatError() {
        DialogBuilder.createNotificationDialog(this, Dialogs.EMAIL_FORMAT_ERROR);
    }

    @Override
    public void onError(String errorCode, String errorMessage) {
        DialogBuilder.createFullCustomNotificationDialog(this, "Registration Error " + errorCode, errorMessage);
    }
}