package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.ChartsFragment;
import com.nilsbo.egofm.fragments.NewsContainer;
import com.nilsbo.egofm.fragments.PlaylistContainer;

import java.lang.reflect.Field;
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
                return PlaylistContainer.newInstance();
//                return NewsListFragment.newInstance();
            case 1:
                return NewsContainer.newInstance();
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

//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment f = (Fragment)super.instantiateItem(container, position);
//        Bundle savedFragmentState = f.mSavedFragmentState;
//        if (savedFragmentState != null) {
//            savedFragmentState.setClassLoader(f.getClass().getClassLoader());
//        }
//        return f;
//    }

    /**
     * See http://stackoverflow.com/questions/11381470/classnotfoundexception-when-unmarshalling-android-support-v4-view-viewpagersav
     *
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Object fragment = super.instantiateItem(container, position);
        try {
            final Field saveFragmentStateField = Fragment.class.getDeclaredField("mSavedFragmentState");
            saveFragmentStateField.setAccessible(true);
            final Bundle savedFragmentState = (Bundle) saveFragmentStateField.get(fragment);
            if (savedFragmentState != null) {
                savedFragmentState.setClassLoader(fragment.getClass().getClassLoader());
//                savedFragmentState.setClassLoader(Fragment.class.getClassLoader());
            }
        } catch (Exception e) {
            Log.w("CustomFragmentStatePagerAdapter", "Could not get mSavedFragmentState field: " + e);
        }
        return fragment;
    }

}