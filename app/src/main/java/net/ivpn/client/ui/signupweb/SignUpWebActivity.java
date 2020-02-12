package net.ivpn.client.ui.signupweb;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivitySignupWebBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUpWebActivity extends AppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignUpWebActivity.class);

    private ActivitySignupWebBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LOGGER.info("onCreate");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup_web);
        initToolbar();
        init();
    }

    private void init() {
        binding.webview.setWebViewClient(new Browser());
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.loadUrl("https://www.ivpn.net/signup/VPN%20account/Annually");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            if (binding.webview.canGoBack()) {
                binding.webview.goBack();
            } else {
                onBackPressed();
            }
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.sign_up_title);
    }

    private class Browser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
