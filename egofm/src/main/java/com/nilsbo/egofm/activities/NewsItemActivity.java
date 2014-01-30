package com.nilsbo.egofm.activities;

import android.app.Activity;
import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.NewsItemFragment;

public class NewsItemActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);

        if (savedInstanceState == null) {
//            NewsItemFragment details = new NewsItemFragment();
//            details.setArguments(getIntent().getExtras());
//            getFragmentManager().beginTransaction().add(
//                    R.id.container, details).commit();

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new NewsItemFragment())
                    .commit();
        }
    }
}
