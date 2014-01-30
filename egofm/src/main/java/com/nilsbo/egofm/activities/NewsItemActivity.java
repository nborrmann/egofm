package com.nilsbo.egofm.activities;

import android.os.Bundle;
import android.view.Menu;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.NewsItemFragment;

public class NewsItemActivity extends EgofmActivity {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

}
