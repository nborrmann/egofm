package com.nilsbo.egofm.activities;

import android.os.Bundle;
import android.util.Log;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.NewsItemFancyFragment;
import com.nilsbo.egofm.util.NewsItem;

public class NewsItemActivity extends EgofmActivity {
    private static final String TAG = "com.nilsbo.egofm.activities.NewsItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);

        if (savedInstanceState == null) {
            final NewsItemFancyFragment newsItemFancyFragment = new NewsItemFancyFragment();
            Log.d(TAG, "set Content");
            newsItemFancyFragment.setContent((NewsItem) getIntent().getParcelableExtra("news_header"), true);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, newsItemFancyFragment)
                    .commit();
        }
    }
}
