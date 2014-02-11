package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.ChartsFragment;
import com.nilsbo.egofm.fragments.NewsContainer;
import com.nilsbo.egofm.fragments.PlaylistFragment;

import java.util.Locale;

/**
 * Created by Nils on 22.01.14.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "com.nilsbo.egofm.adapters.SectionsPagerAdapter";

    private final Context context;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return NewsContainer.newInstance();
//                return NewsListFragment.newInstance();
            case 1:
                return PlaylistFragment.newInstance();
            case 2:
                return ChartsFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.tab_news).toUpperCase(l);
            case 1:
                return context.getString(R.string.tab_playlist).toUpperCase(l);
            case 2:
                return context.getString(R.string.tab_42).toUpperCase(l);
        }
        return null;
    }
}