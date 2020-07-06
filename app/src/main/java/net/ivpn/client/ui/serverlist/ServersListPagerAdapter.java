package net.ivpn.client.ui.serverlist;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import net.ivpn.client.R;
import net.ivpn.client.ui.serverlist.all.ServerListFragment;
import net.ivpn.client.ui.serverlist.favourites.FavouriteServersListFragment;

public class ServersListPagerAdapter extends FragmentStatePagerAdapter {

    private static final int ITEM_COUNT = 2;

    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
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
        switch (position) {
            case 0:
                return context.getString(R.string.servers_list_favorites);
            default:
                return context.getString(R.string.servers_list_all);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FavouriteServersListFragment();
            default:
                return new ServerListFragment();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
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

    public void applyPendingAction() {
        if (registeredFragments.size() != ITEM_COUNT) {
            return;
        }

        ((FavouriteServersListFragment) registeredFragments.get(0)).applyPendingAction();
        ((ServerListFragment) registeredFragments.get(1)).applyPendingAction();
    }
}
