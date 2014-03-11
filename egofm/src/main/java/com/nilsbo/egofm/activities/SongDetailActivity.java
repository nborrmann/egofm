package com.nilsbo.egofm.activities;

import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.SongDetailFancyFragment;
import com.nilsbo.egofm.fragments.SongDetailFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SongDetailActivity extends EgofmActivity {
    private static final String TAG = "com.nilsbo.egofm.activities.SongDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.use_fancy_news_item) && android.os.Build.VERSION.SDK_INT >= 14) {
            setTheme(R.style.Theme_Egofm_TransparentActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        if (!getResources().getBoolean(R.bool.use_fancy_news_item) || android.os.Build.VERSION.SDK_INT < 14) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.egofm_grey);
        }

        if (savedInstanceState == null) {
            SongDetailFragment songDetailFragment;
            if (getResources().getBoolean(R.bool.use_fancy_news_item) && android.os.Build.VERSION.SDK_INT >= 14) {
                songDetailFragment = new SongDetailFancyFragment();
            } else {
                songDetailFragment = new SongDetailFragment();
            }

            if (getIntent() != null && getIntent().getStringExtra(SongDetailFragment.ARG_SONG_TITLE) != null) {
                Bundle arguments = new Bundle();
                arguments.putString(SongDetailFragment.ARG_SONG_TITLE, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_TITLE));
                arguments.putString(SongDetailFragment.ARG_SONG_ARTIST, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_ARTIST));
                songDetailFragment.setArguments(arguments);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, songDetailFragment)
                    .commit();
        }
    }

}
