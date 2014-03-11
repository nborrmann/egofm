package com.nilsbo.egofm.activities;

import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.NewsItemFancyFragment;
import com.nilsbo.egofm.fragments.NewsItemFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class NewsItemActivity extends EgofmActivity {
    private static final String TAG = "com.nilsbo.egofm.activities.NewsItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.use_fancy_news_item) && android.os.Build.VERSION.SDK_INT >= 14) {
            setTheme(R.style.Theme_Egofm_TransparentActionBar);
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.egofm_grey);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);

        if (savedInstanceState == null) {
            NewsItemFragment newsItemFragment;
            if (getResources().getBoolean(R.bool.use_fancy_news_item) && android.os.Build.VERSION.SDK_INT >= 14) {
                newsItemFragment = new NewsItemFancyFragment();
            } else {
                newsItemFragment = new NewsItemFragment();
            }

            if (getIntent() != null && getIntent().getParcelableExtra(NewsItemFragment.ARG_NEWS_ITEM) != null) {
                Bundle arguments = new Bundle();
                arguments.putParcelable(NewsItemFragment.ARG_NEWS_ITEM, getIntent().getParcelableExtra(NewsItemFragment.ARG_NEWS_ITEM));
                newsItemFragment.setArguments(arguments);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, newsItemFragment)
                    .commit();
        }
    }
}
