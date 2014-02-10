package com.nilsbo.egofm.activities;

import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.NewsItemFragment;

public class NewsItemActivity extends EgofmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new NewsItemFragment())
                    .commit();
        }
    }
}
