package com.nilsbo.egofm.activities;

import android.app.ActionBar;
import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.SongDetailFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SongDetailActivity extends EgofmActivity {

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getResources().getBoolean(R.bool.use_fancy_news_item)) {
            setTheme(R.style.Theme_Egofm_TransparentActionBar);

            mActionBar = getActionBar();
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.egofm_grey);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        tintManager.setStatusBarTintEnabled(true);
//        tintManager.setStatusBarTintResource(R.color.egofm_grey);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            if (getIntent() != null && getIntent().getStringExtra(SongDetailFragment.ARG_SONG_TITLE) != null) {
                arguments.putString(SongDetailFragment.ARG_SONG_TITLE, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_TITLE));
                arguments.putString(SongDetailFragment.ARG_SONG_ARTIST, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_ARTIST));
            } else {
                // TODO sample debug data
                arguments.putString(SongDetailFragment.ARG_SONG_TITLE, "Fancy Footwork");
                arguments.putString(SongDetailFragment.ARG_SONG_ARTIST, "Chromeo");
            }

            SongDetailFragment songDetailFragment = new SongDetailFragment();
            songDetailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, songDetailFragment)
                    .commit();
        }
    }

}
