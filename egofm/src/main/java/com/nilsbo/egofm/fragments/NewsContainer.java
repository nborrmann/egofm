package com.nilsbo.egofm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilsbo.egofm.Interfaces.NewsListListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.activities.NewsItemActivity;
import com.nilsbo.egofm.util.NewsItem;


public class NewsContainer extends Fragment implements NewsListListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsContainer";

    private static final String SAVED_STATE_TWO_PANE = "savedStateTwoPane";

    private NewsItemFragment newsItemFragment;
    private NewsListFragment newsListFragment;
    private FragmentManager childFragmentManager;
    private ViewGroup rootView;

    private boolean isTwoPane;

    public static NewsContainer newInstance() {
        NewsContainer fragment = new NewsContainer();
        return fragment;
    }

    public NewsContainer() {
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

        if (savedInstanceState == null) {
            newsListFragment = new NewsListFragment();
            newsListFragment.registerCallback(this);
            childFragmentManager.beginTransaction().add(R.id.news_list_container, newsListFragment, "bla").commit();

            if (rootView.findViewById(R.id.news_item_container) != null) {
                isTwoPane = true;

                newsItemFragment = new NewsItemFragment();
                childFragmentManager.beginTransaction().add(R.id.news_item_container, newsItemFragment).commit();
            }
        } else {
            // this is necessary because the Fragment gets instantiated with empty constructor, if
            // it is recreated after onSaveInstanceState
            newsListFragment = ((NewsListFragment) childFragmentManager.findFragmentById(R.id.news_list_container));
            newsListFragment.registerCallback(this);
            newsItemFragment = (NewsItemFragment) childFragmentManager.findFragmentById(R.id.news_item_container);

            isTwoPane = savedInstanceState.getBoolean(SAVED_STATE_TWO_PANE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = container;
        return inflater.inflate(R.layout.fragment_news_container, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVED_STATE_TWO_PANE, isTwoPane);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClicked(NewsItem item) {
        if (isTwoPane) {
            newsItemFragment.setContent(item);
        } else {
            Intent intent = new Intent(getActivity(), NewsItemActivity.class);
            intent.putExtra("news_header", item);
            getActivity().startActivity(intent);
        }
    }
}
