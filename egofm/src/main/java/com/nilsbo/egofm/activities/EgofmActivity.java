package com.nilsbo.egofm.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nilsbo.egofm.Interfaces.EgofmActivityInterface;
import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;

public class EgofmActivity extends FragmentActivity implements EgofmActivityInterface {
    private static final String TAG = "com.nilsbo.egofm.activities.EgofmActivity";

    public MediaServiceInterface serviceCallback;
    private Intent mediaServiceIntent;
    boolean mStarted = false;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaServiceIntent = new Intent(this, MediaService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startService(mediaServiceIntent);
        bindService(mediaServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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
        switch (item.getItemId()) {
            case R.id.action_play:
                startServiceAndPlayback();
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

    private void startServiceAndPlayback() {
        serviceCallback.startMediaPlayer();
        mStarted = true;
        invalidateOptionsMenu();
    }


    private void stopServiceAndPlayback() {
        serviceCallback.stopMediaPlayer();
        mStarted = false;
        invalidateOptionsMenu();
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
    public void playbackStopped() {
        invalidateOptionsMenu();
    }
}
