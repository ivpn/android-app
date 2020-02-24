package net.ivpn.client.ui.login;

import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.utils.IntentUtils;
import net.ivpn.client.common.utils.KeyboardUtil;
import net.ivpn.client.databinding.ActivityLoginBinding;
import net.ivpn.client.ui.connect.CreateSessionFragment;
import net.ivpn.client.ui.connect.CreateSessionNavigator;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.signup.SignUpActivity;
import net.ivpn.client.ui.subscription.SubscriptionActivity;
import net.ivpn.client.ui.syncservers.SyncServersActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class LoginActivity extends AppCompatActivity implements LoginNavigator, CreateSessionNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginActivity.class);
    private static final int OFFLINE_LOGIN_REQUEST_CODE = 116;

    private ActivityLoginBinding binding;
    @Inject LoginViewModel viewModel;
    private CreateSessionFragment createSessionFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initToolbar();
        init();
    }

    @Override
    protected void onResume() {
        LOGGER.info("onResume");
        super.onResume();
        binding.contentLayout.login.setFocusable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtil.removeAllKeyboardToggleListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == OFFLINE_LOGIN_REQUEST_CODE) {
            viewModel.login(false);
        }
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
        viewModel.cancel();
    }

    private void init() {
        viewModel.setNavigator(this);
        binding.contentLayout.setViewmodel(viewModel);
        binding.contentLayout.login.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(textView);
                return true;
            }
            return false;
        });

        KeyboardUtil.addKeyboardToggleListener(this, isVisible -> {
            binding.contentLayout.image.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        String string = getString(R.string.login_account_id_description);
        String stringToSpan = "Client Area";
        SpannableStringBuilder spannable = new SpannableStringBuilder(string);
        int startSpanPosition = string.indexOf(stringToSpan);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLink("https://www.ivpn.net/clientarea/login");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                int color = ContextCompat.getColor(LoginActivity.this, R.color.colorPrimary);
                ds.setColor(color);
            }
        };

        spannable.setSpan(span, startSpanPosition,
                startSpanPosition + stringToSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        binding.contentLayout.description.setText(spannable);
        binding.contentLayout.description.setMovementMethod(new LinkMovementMethod());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.sign_in_title);
    }

    public void login(View view) {
        LOGGER.info("login");
        viewModel.login(false);
    }

    public void signUp(View view) {
        LOGGER.info("Navigate to sign up screen");

        if (BuildConfig.BUILD_VARIANT.equals("site")) {
            openWebsite();
        } else {
            Intent intent = new Intent(this, SignUpActivity.class);
            startSingleTopActivity(intent);
            finish();
        }
    }

    private void openWebsite() {
        Intent intent = IntentUtils.INSTANCE.createWebSignUpIntent();

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onLogin() {
        LOGGER.info("onLogin");
        Intent intent = new Intent(this, SyncServersActivity.class);
        startSingleTopActivity(intent);
        finish();
    }

    @Override
    public void openSubscriptionScreen() {
        LOGGER.info("openSubscriptionScreen");
        Intent syncIntent = new Intent(this, SyncServersActivity.class);
        Intent subscriptionIntent = new Intent(this, SubscriptionActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(syncIntent);
        stackBuilder.addNextIntent(subscriptionIntent);

        stackBuilder.startActivities();
        finish();
    }

    @Override
    public void openActivateDialogue() {
        LOGGER.info("openActivateDialogue");
        DialogBuilder.createOptionDialog(this, Dialogs.ACCOUNT_IS_NOT_ACTIVE, (dialog, which) -> {
            if (BuildConfig.BUILD_VARIANT.equals("site")) {
                openLink("https://www.ivpn.net/signup/IVPN%20Pro/Annually");
            } else {
                startSingleTopActivity(new Intent(this, SubscriptionActivity.class));
            }
        });
    }

    @Override
    public void openSite() {
        LOGGER.info("openSite");
        Uri webpage = Uri.parse("https://www.ivpn.net/signup/IVPN%20Pro/Annually");
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        if (webIntent.resolveActivity(getPackageManager()) != null) {
            Intent syncIntent = new Intent(this, SyncServersActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(syncIntent);
            stackBuilder.addNextIntent(webIntent);
            stackBuilder.startActivities();
            finish();
        } else {
            onLogin();
        }
    }

    @Override
    public void openSessionLimitReachedDialogue() {
        createSessionFragment = new CreateSessionFragment();
        createSessionFragment.show(getSupportFragmentManager(), createSessionFragment.getTag());
    }

    @Override
    public void openErrorDialogue(Dialogs dialog) {
        DialogBuilder.createNotificationDialog(this, dialog);
    }

    @Override
    public void openCustomErrorDialogue(String title, String message) {
        DialogBuilder.createFullCustomNotificationDialog(this, title, message);
    }

    @Override
    public void onForceLogout() {
        viewModel.login(true);
        createSessionFragment.dismissAllowingStateLoss();
    }

    @Override
    public void tryAgain() {
        viewModel.login(false);
        createSessionFragment.dismissAllowingStateLoss();
    }

    @Override
    public void cancel() {
        createSessionFragment.dismissAllowingStateLoss();
    }

    private void openLink(String link) {
        Uri webPage = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void startSingleTopActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}