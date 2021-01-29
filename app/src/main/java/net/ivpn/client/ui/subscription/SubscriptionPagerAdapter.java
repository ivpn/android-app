package net.ivpn.client.ui.subscription;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import net.ivpn.client.R;
import net.ivpn.client.ui.subscription.monthly.MonthlySubscriptionFragment;
import net.ivpn.client.ui.subscription.yearly.YearlySubscriptionFragment;

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