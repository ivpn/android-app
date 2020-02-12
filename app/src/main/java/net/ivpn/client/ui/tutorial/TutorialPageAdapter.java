package net.ivpn.client.ui.tutorial;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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