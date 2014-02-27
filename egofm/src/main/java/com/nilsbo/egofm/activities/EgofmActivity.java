package com.nilsbo.egofm.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.analytics.tracking.android.EasyTracker;
import com.nilsbo.egofm.Interfaces.EgofmActivityInterface;
import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;

public class EgofmActivity extends FragmentActivity implements EgofmActivityInterface {
    private static final String TAG = "com.nilsbo.egofm.activities.EgofmActivity";

    public MediaServiceInterface serviceCallback;
    private Intent mediaServiceIntent;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= 19)
            // set this programmatically so the statusbar in the loading screen isn't white
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        super.onCreate(savedInstanceState);
        mediaServiceIntent = new Intent(this, MediaService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        startService(mediaServiceIntent);
        bindService(mediaServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        EasyTracker.getInstance(this).activityStart(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        // Kill service if it isn't playing
        if (serviceCallback != null && serviceCallback.getPlaybackState() == MediaService.State.Stopped) {
            stopService(mediaServiceIntent);
        }

        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (serviceCallback != null && (serviceCallback.getPlaybackState() == MediaService.State.Playing || serviceCallback.getPlaybackState() == MediaService.State.Preparing)) {
            getMenuInflater().inflate(R.menu.main_playing, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_stopped, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                startService(new Intent(MediaService.ACTION_START));
                return true;
            case R.id.action_stop:
                startService(new Intent(MediaService.ACTION_CLOSE));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaService.MediaServiceBinder binder = (MediaService.MediaServiceBinder) service;
            serviceCallback = binder.getServiceInterface();
            serviceCallback.registerActivityCallback(EgofmActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void playbackStateChanged() {
        invalidateOptionsMenu();
    }
}
