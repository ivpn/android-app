package net.ivpn.client.common.bindings;

import android.databinding.BindingAdapter;
import android.support.v4.view.ViewPager;

public class ViewPagerBindingAdapter {

    @BindingAdapter("app:isFavouritesEmpty")
    public static void setSwipeRefreshLayoutOnRefreshListener(ViewPager viewPager,
                                                              boolean isFavouritesEmpty) {
        if (isFavouritesEmpty) {
            viewPager.setCurrentItem(1, false);
        }
    }
}