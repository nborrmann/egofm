package com.nilsbo.egofm.activities;

import android.os.Bundle;

import com.nilsbo.egofm.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class SettingsActivity extends EgofmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.egofm_grey);
    }


}
