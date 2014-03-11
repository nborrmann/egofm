package com.nilsbo.egofm.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.nilsbo.egofm.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // use PreferenceFragment
            setContentView(R.layout.activity_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.egofm_grey);
    }


}
