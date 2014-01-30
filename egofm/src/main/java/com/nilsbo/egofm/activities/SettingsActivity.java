package com.nilsbo.egofm.activities;

import android.os.Bundle;
import android.view.Menu;

import com.nilsbo.egofm.R;

public class SettingsActivity extends EgofmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (serviceCallback != null && serviceCallback.isStarted()) {
            getMenuInflater().inflate(R.menu.main_playing, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_stopped, menu);
        }
        return true;
    }

}
