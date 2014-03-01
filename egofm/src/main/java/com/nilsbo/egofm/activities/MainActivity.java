package com.nilsbo.egofm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.SectionsPagerAdapter;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import static com.nilsbo.egofm.util.FragmentUtils.logUIAction;

public class MainActivity extends EgofmActivity {
    private static final String TAG = "com.nilsbo.egofm.activities.MainActivity";

    private static final String[] SCREEN_NAMES = {"NewsList", "Playlist", "egoFM 42"};

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                trackFragmentSelected(position);
            }
        });

        PagerTitleStrip pagerTabStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setTextSpacing(10);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.egofm_grey);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals("Music Notification")) {
            logUIAction(this, "Notification clicked", null);
        }
    }

    private void trackFragmentSelected(int position) {
        Tracker easyTracker = EasyTracker.getInstance(this);

        easyTracker.set(Fields.SCREEN_NAME, SCREEN_NAMES[position]);

        easyTracker.send(MapBuilder
                .createAppView()
                .build()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackFragmentSelected(mViewPager.getCurrentItem());
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
