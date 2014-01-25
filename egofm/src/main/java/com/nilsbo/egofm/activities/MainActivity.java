package com.nilsbo.egofm.activities;

import android.preference.PreferenceManager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nilsbo.egofm.Interfaces.MainActivityInterface;
import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.fragments.SectionsPagerAdapter;

import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements MainActivityInterface {
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

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }

    @Override
    protected void onStart() {
        super.onStart();

        startService(mediaServiceIntent);
        bindService(mediaServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        // Kill service if it isn't playing
        if (!serviceCallback.isStarted()) {
            stopService(mediaServiceIntent);
        }
    }

    private void startServiceAndPlayback() {
        serviceCallback.startMediaPlayer();
        mStarted = true;
    }
    private void stopServiceAndPlayback() {
        serviceCallback.stopMediaPlayer();
        mStarted = false;
        invalidateOptionsMenu();
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaService.MediaServiceBinder binder = (MediaService.MediaServiceBinder) service;
            serviceCallback = binder.getServiceInterface();
            serviceCallback.registerActivityCallback(MainActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (serviceCallback != null && serviceCallback.isStarted()) {
            getMenuInflater().inflate(R.menu.main_playing, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_stopped, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_play:
                startServiceAndPlayback();
                invalidateOptionsMenu();
                return true;
            case R.id.action_stop:
                stopServiceAndPlayback();
                return true;
            case R.id.action_settings:
                Intent mIntent = new Intent(this, SettingsActivity.class);
                startActivity(mIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void playbackStopped() {
        invalidateOptionsMenu();
    }
}
