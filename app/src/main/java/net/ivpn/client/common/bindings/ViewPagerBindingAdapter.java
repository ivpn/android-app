package net.ivpn.client.common.bindings;

import androidx.databinding.BindingAdapter;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerBindingAdapter {

    @BindingAdapter("isFavouritesEmpty")
    public static void setSwipeRefreshLayoutOnRefreshListener(ViewPager viewPager,
                                                              boolean isFavouritesEmpty) {
        if (isFavouritesEmpty) {
            viewPager.setCurrentItem(1, false);
        }
    }
}