package com.nilsbo.egofm.activities;

import android.os.Bundle;

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
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SongDetailFragment())
                    .commit();
        }
    }
}
