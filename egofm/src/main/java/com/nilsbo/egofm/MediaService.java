package com.nilsbo.egofm;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.Interfaces.EgofmActivityInterface;
import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.TrackRequest;
import com.nilsbo.egofm.util.EgoFmNotificationManager;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;


public class MediaService extends Service implements MediaServiceInterface, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "com.nilsbo.egofm.MediaService";

    public static final String BROADCAST_ID_CLOSE = "com.nilsbo.egofm.control.close";
    public static final String BROADCAST_ID_STARTSTOP = "com.nilsbo.egofm.control.stop";

    private final IBinder mBinder = new MediaServiceBinder();
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture metaDataHandler;
    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private EgoFmNotificationManager notificationManager;
    private EgofmActivityInterface activityCallback;
    private MediaService.IntentReceiver receiver = new IntentReceiver();
    private AudioManager audioManager;

    private boolean isBound = false;
    private boolean started = false;
    private int connectionTries = 0;

    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = new EgoFmNotificationManager(this, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        IntentFilter intentFilter = new IntentFilter(BROADCAST_ID_STARTSTOP);
        intentFilter.addAction(BROADCAST_ID_CLOSE);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(receiver, intentFilter);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        return START_STICKY;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (started) mMediaPlayer.setVolume(1.0f, 1.0f);
                else startMediaPlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                cleanup();
                notificationManager.displayDefaultNotification(started);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }

    }

    public class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BROADCAST_ID_CLOSE)) {
                cleanup();
                if (isBound) stopForeground(true);
                else stopSelf();

            } else if (action.equals(BROADCAST_ID_STARTSTOP)) {
                if (started) {
                    cleanup();
                    notificationManager.displayDefaultNotification(started);
                } else {
                    startMediaPlayer();
                }

            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                cleanup();
                notificationManager.displayDisconnectedNotification();
            }
        }
    }


    public void startMediaPlayer() {
        connectionTries = 0;
        notificationManager.startService(this);
        tryConnect();
    }

    public void stopMediaPlayer() {
        stopForeground(true);
        cleanup();
    }

    private void tryConnect() {
        started = true;
        notificationManager.displayConnectingNotification();
        try {
            if (connectionTries < 3) {
                connect();
            }
        } catch (IOException e) {
            tryConnect();
        }
        if (connectionTries >= 3) {
            displayError();
        }
        connectionTries++;
    }

    private void displayError() {
        cleanup();
        if (isBound) {
            Toast toast = Toast.makeText(this, getString(R.string.notification_connection_error), Toast.LENGTH_SHORT);
            toast.show();
            stopForeground(true);
        } else { // keep the notification in case the user wants to retry manually later on.
            notificationManager.displayDisconnectedNotification();
        }
    }

    private void connect() throws IOException {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(getUrl());
        Log.d(TAG, "connecting to " + getUrl());

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.prepareAsync();

        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
    }

    /**
     * you have to stop the service manually.
     */
    private void cleanup() {
        if (metaDataHandler != null) metaDataHandler.cancel(true);
        if (mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
        started = false;
        if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
        if (activityCallback != null) activityCallback.playbackStopped();
        if (audioManager != null) audioManager.abandonAudioFocus(this);
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
        connectionTries = 0;

        notificationManager.displayConnectedNotification();
        metaDataHandler = scheduler.scheduleAtFixedRate(metaDataDownloader, 0, getUpdateInterval(), SECONDS);

        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    final Runnable metaDataDownloader = new Runnable() {
        public void run() {
            TrackRequest metaDataRequest = new TrackRequest("http://www.egofm.de/templates/egofm/get_track.php", new Response.Listener<String[]>() {
                @Override
                public void onResponse(String[] response) {
                    if (started) {
                        Log.d(TAG, String.format("now playing: %s - %s", response[0], response[1]));
                        notificationManager.displaySongNotification(response[0], response[1]);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (started) {
                        notificationManager.displayDefaultNotification(started);
                    }
                }
            }
            );

            requestQueue.add(metaDataRequest);
        }
    };

    private long getUpdateInterval() {
        final String intervalStr = PreferenceManager.getDefaultSharedPreferences(this).getString("metadata_interval", "20");
        int interval = 20;
        try {
            interval = Integer.parseInt(intervalStr);
        } catch (NumberFormatException e) {
            Log.w(TAG, "error parsing metadata update interval");
        }
        return interval;
    }

    private String getUrl() {
        final String streamQuality = PreferenceManager.getDefaultSharedPreferences(this).getString("streamquality", "wifihigh");
        if (streamQuality.equals("high")) {
            return getResources().getString(R.string.url_stream_high);
        } else if (streamQuality.equals("wifihigh")) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                return getResources().getString(R.string.url_stream_high);
            } else {
                return getResources().getString(R.string.url_stream_low);
            }
        } else {
            return getResources().getString(R.string.url_stream_low);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError. what " + what + "; extra " + extra);
        handleAllErrors();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        handleAllErrors();
    }

    private void handleAllErrors() {
        if (metaDataHandler != null) metaDataHandler.cancel(true);
        mMediaPlayer.release();
        mMediaPlayer = null;
        tryConnect();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onInfo; what: " + what + "; extra: " + extra);
        return false;
    }

    public boolean isStarted() {
        return started;
    }

    public class MediaServiceBinder extends Binder {
        // Return this instance of LocalService so clients can call public methods
        public MediaServiceInterface getServiceInterface() {
            return MediaService.this;
        }
    }

    @Override
    public void registerActivityCallback(EgofmActivityInterface activityCallback) {
        this.activityCallback = activityCallback;
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        unregisterReceiver(receiver);
        cleanup();
        super.onDestroy();
    }
}

