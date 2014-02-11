package com.nilsbo.egofm.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilsbo.egofm.R;


public class NewsTwoPaneFragment extends Fragment {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsTwoPaneFragment";
    private FragmentManager childFragmentManager;
    private ViewGroup rootView;
    private NewsFragment newsFragment;
    private NewsItemTwoPaneFragment newsItemFragment;
    private static final String SAVED_STATE_NEWS_FRAGMENT = "savedStateNewsFragment";
    private static final String SAVED_STATE_NEWS_ITEM_FRAGMENT = "savedStateNewsItemFragment";

    public static NewsTwoPaneFragment newInstance() {
        NewsTwoPaneFragment fragment = new NewsTwoPaneFragment();
        return fragment;
    }

    public NewsTwoPaneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        childFragmentManager = getChildFragmentManager();

        SavedState newsFragState = null;
        SavedState newsItemFragState = null;
        if (savedInstanceState != null) {
            newsFragState = savedInstanceState.getParcelable(SAVED_STATE_NEWS_FRAGMENT);
            newsItemFragState = savedInstanceState.getParcelable(SAVED_STATE_NEWS_ITEM_FRAGMENT);
        }

        newsFragment = new NewsFragment();
        if (newsFragState != null)
            newsFragment.setInitialSavedState(newsFragState);
        childFragmentManager.beginTransaction().add(R.id.news_list_container, newsFragment).commit();

        if (rootView.findViewById(R.id.news_item_container) != null) {
            newsItemFragment = new NewsItemTwoPaneFragment();
            if (newsItemFragState != null)
                newsItemFragment.setInitialSavedState(newsItemFragState);
            childFragmentManager.beginTransaction().add(R.id.news_item_container, newsItemFragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = container;
        return inflater.inflate(R.layout.fragment_news_container, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_STATE_NEWS_FRAGMENT, childFragmentManager.saveFragmentInstanceState(newsFragment));
        childFragmentManager.beginTransaction().remove(newsFragment).commit();
        if (newsItemFragment != null) {
            outState.putParcelable(SAVED_STATE_NEWS_ITEM_FRAGMENT, childFragmentManager.saveFragmentInstanceState(newsItemFragment));
            childFragmentManager.beginTransaction().remove(newsItemFragment).commit();
        }
    }
}
