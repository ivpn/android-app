package net.ivpn.client.ui.subscription;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.android.billingclient.api.SkuDetails;

import net.ivpn.client.R;
import net.ivpn.client.ui.subscription.monthly.MonthlySubscriptionFragment;
import net.ivpn.client.ui.subscription.yearly.YearlySubscriptionFragment;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPagerAdapter extends FragmentStatePagerAdapter {

    private static final int ITEM_COUNT = 2;
    public static final String SKU_DETAILS = "SKU_DETAILS";

    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private Context context;

    SubscriptionPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return getMonthlySubscriptionFragment();
        }
        return getYearlySubscriptionFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.subscription_title_monthly);
        }
        return context.getString(R.string.subscription_title_annual);
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    private Fragment getMonthlySubscriptionFragment() {
        return new MonthlySubscriptionFragment();
    }

    private Fragment getYearlySubscriptionFragment() {
        return new YearlySubscriptionFragment();
    }
}