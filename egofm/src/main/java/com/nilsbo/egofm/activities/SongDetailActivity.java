package com.nilsbo.egofm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.SongDetailFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SongDetailActivity extends EgofmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.egofm_grey);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(SongDetailFragment.ARG_SONG_TITLE, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_TITLE));
            arguments.putString(SongDetailFragment.ARG_SONG_ARTIST, getIntent().getStringExtra(SongDetailFragment.ARG_SONG_ARTIST));

            SongDetailFragment songDetailFragment = new SongDetailFragment();
            songDetailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, songDetailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent mIntent = new Intent(this, SettingsActivity.class);
                startActivity(mIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
}
