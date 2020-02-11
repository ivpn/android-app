package net.ivpn.client.ui.tutorial;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.ivpn.client.BuildConfig;
import net.ivpn.client.R;
import net.ivpn.client.databinding.ActivityTutorialBinding;
import net.ivpn.client.ui.login.LoginActivity;
import net.ivpn.client.ui.signup.SignUpActivity;
import net.ivpn.client.ui.tutorial.data.TutorialPage;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager pager;

    private PagerAdapter pagerAdapter;
    private ActivityTutorialBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial);
        binding.backArrow.setOnClickListener(view -> {
            onBackPressed();
        });
        init();
    }

    private void init() {
        pager = findViewById(R.id.pager);

        pagerAdapter = new TutorialPageAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changePageTo(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void changePageTo(int position) {
        binding.setPage(new TutorialPage(position));
    }

    public void startTrial(View view) {
        if (BuildConfig.BUILD_VARIANT.equals("site")) {
            openWebsite();
        } else {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
    }

    public void logIn(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void openWebsite() {
        Uri webpage = Uri.parse("https://www.ivpn.net/signup/IVPN%20Pro/Annually");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
