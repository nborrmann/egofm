package com.nilsbo.egofm.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.nilsbo.egofm.R;

import java.util.Locale;

/**
 * Created by Nils on 22.01.14.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final Context context;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return PlaylistFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.tab_playlist).toUpperCase(l);
        }
        return null;
    }
}