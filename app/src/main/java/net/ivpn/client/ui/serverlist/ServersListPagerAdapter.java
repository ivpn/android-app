package net.ivpn.client.ui.serverlist;

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
