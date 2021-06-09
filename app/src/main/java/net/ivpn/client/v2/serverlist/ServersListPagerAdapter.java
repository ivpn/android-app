package net.ivpn.client.v2.serverlist;

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
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import net.ivpn.client.R;
import net.ivpn.client.v2.serverlist.all.ServerListFragment;
import net.ivpn.client.v2.serverlist.favourite.FavouriteServersListFragment;

public class ServersListPagerAdapter extends FragmentStatePagerAdapter {

    private static final int ITEM_COUNT = 2;

    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private Context context;

    public ServersListPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.servers_list_all);
        }
        return context.getString(R.string.servers_list_favorites);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new ServerListFragment();
        }
        return new FavouriteServersListFragment();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public void cancel() {
        if (registeredFragments.size() != ITEM_COUNT) {
            return;
        }

        ((FavouriteServersListFragment) registeredFragments.get(0)).cancel();
        ((ServerListFragment) registeredFragments.get(1)).cancel();
    }
}
