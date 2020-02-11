package net.ivpn.client.ui.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TutorialPageAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 3;

    public TutorialPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        TutorialPageFragment page = new TutorialPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TutorialPages.PAGE_POSITION, position);
        page.setArguments(bundle);
        return page;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}