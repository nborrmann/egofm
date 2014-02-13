package com.nilsbo.egofm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.SectionsPagerAdapter;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends EgofmActivity {
    private static final String TAG = "com.nilsbo.egofm.activities.MainActivity";

    private MediaServiceInterface serviceCallback;
    boolean mBound = false;

    boolean mStarted = false;
    private Intent mediaServiceIntent;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaServiceIntent = new Intent(this, MediaService.class);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerTitleStrip pagerTabStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setTextSpacing(200);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.egofm_grey);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

//        new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH, Uri.parse("com.google.android.music.VoiceActionsActivity"));
//        Intent intent = new Intent();
//        intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
//        intent.putExtra(SearchManager.QUERY, "It's my life");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        intent.setPackage("com.google.android.music.VoiceActionsActivity");
//        startActivity(intent);

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
