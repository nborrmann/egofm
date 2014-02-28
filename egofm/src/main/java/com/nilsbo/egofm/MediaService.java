package com.nilsbo.egofm;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.nilsbo.egofm.Interfaces.EgofmActivityInterface;
import com.nilsbo.egofm.Interfaces.MediaServiceInterface;
import com.nilsbo.egofm.Interfaces.MetaDataListener;
import com.nilsbo.egofm.Interfaces.MusicFocusable;
import com.nilsbo.egofm.music.AudioFocusHelper;
import com.nilsbo.egofm.music.EgofmMusicNetworking;
import com.nilsbo.egofm.music.EgofmRemoteManager;
import com.nilsbo.egofm.music.MusicIntentReceiver;

import java.io.IOException;
import java.util.Date;

import static com.nilsbo.egofm.util.FragmentUtils.logStreamStart;
import static com.nilsbo.egofm.util.FragmentUtils.logStreamStop;


public class MediaService extends Service implements MediaServiceInterface, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicFocusable, MetaDataListener {
    private static final String TAG = "com.nilsbo.egofm.MediaService";

    public static final String ACTION_START = "com.nilsbo.egofm.action.PLAY";
    public static final String ACTION_STOP = "com.nilsbo.egofm.action.STOP";
    public static final String ACTION_CLOSE = "com.nilsbo.egofm.action.CLOSE";
    public static final String ACTION_STARTSTOP = "com.nilsbo.egofm.action.STARTSTOP";
    public static final String ACTION_PLAYPAUSE = "com.nilsbo.egofm.action.PLAYPAUSE";
    public static final String ACTION_PAUSE = "com.nilsbo.egofm.action.PAUSE";

    private final IBinder mBinder = new MediaServiceBinder();
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    private EgofmRemoteManager mRemoteManager;
    private EgofmActivityInterface activityCallback;
    private AudioFocusHelper mAudioFocusHelper;

    private AudioManager mAudioManager;
    private boolean isBound = false;
    private int connectionTries = 0;
    private EgofmMusicNetworking mMusicNetworkingHelper;
    private MusicIntentReceiver mAudioBecomingNoisyReceiver;
    private Date startTime;

    public enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
    }

    ;

    State mState = State.Stopped;

    @Override
    public void onCreate() {
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteManager = new EgofmRemoteManager(this,
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE),
                mAudioManager);

        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

        mMusicNetworkingHelper = new EgofmMusicNetworking(this, this);

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mAudioBecomingNoisyReceiver = new MusicIntentReceiver();
        registerReceiver(mAudioBecomingNoisyReceiver, intentFilter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand with action " + action);
        if (action.equals(ACTION_START)) processStartRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_CLOSE)) processCloseRequest();
        else if (action.equals(ACTION_STARTSTOP)) processStartStopRequest();
        else if (action.equals(ACTION_PLAYPAUSE)) processPlayPauseRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();

        return START_STICKY;
    }

    private void processStartRequest() {
        logStreamStart(getApplicationContext(), "Actionbar");
        startPlayback();
    }

    private void processStopRequest() {
        logStop("Actionbar");
        stopPlayback();
    }

    private void processPauseRequest() {
        logStop("AudioBecomingNoisy");
        pausePlayback();
    }

    // PlayPause is coming from the lockscreen, hence we don't want to give up AudioFocus, so the widget doesn't disappear
    private void processPlayPauseRequest() {
        if (mState == State.Stopped) {
            logStreamStart(getApplicationContext(), "Lockscreen");
            startPlayback();
        } else {
            logStop("Lockscreen");
            pausePlayback();
        }

    }

    // StartStop is coming from the notification. Start and Stop as usual, but don't close the service.
    private void processStartStopRequest() {
        if (mState == State.Stopped) {
            logStreamStart(getApplicationContext(), "Notification");
            startPlayback();
        } else {
            logStop("Notification Stop");
            stopPlayback();
        }
    }

    private void processCloseRequest() {
        logStop("Notification Close");
        cleanup();
        giveUpAudioFocus();
        mRemoteManager.cancelAll();
        stopForeground(true);
        stopSelf();
    }

    private void pausePlayback() {
        cleanup();
        mRemoteManager.displayDisconnectedNotification(mState);
    }

    private void stopPlayback() {
        cleanup();
        giveUpAudioFocus();
        mRemoteManager.displayDisconnectedNotification(mState);
    }

    private void startPlayback() {
        connectionTries = 0;
        mRemoteManager.startService(this);
        tryConnect();
    }

    @Override
    public void onGainedAudioFocus() {
        if (mState == State.Preparing || mState == State.Playing)
            mMediaPlayer.setVolume(1.0f, 1.0f);
        else {
            logStreamStart(getApplicationContext(), "Gained AudioFocus");
            startPlayback();
        }
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        if (!canDuck) {
            logStop("Lost AudioFocus");
            cleanup();
            mRemoteManager.displayDefaultNotification(mState);
        } else {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
        }
    }

    private void tryConnect() {
        if (activityCallback != null) activityCallback.playbackStateChanged();

        mRemoteManager.displayConnectingNotification();
        try {
            if (connectionTries < 3) {
                connect();
            }
        } catch (IOException e) {
            connectionTries++;
            tryConnect();
        }
        if (connectionTries >= 3) {
            connectFailed();
        }
        connectionTries++;
    }

    private void connectFailed() {
        logStop("Reconnect failed");
        cleanup();
        if (isBound) {
            Toast toast = Toast.makeText(this, getString(R.string.notification_connection_error), Toast.LENGTH_SHORT);
            toast.show();
            stopForeground(true);
            stopSelf();
        } else { // keep the notification in case the user wants to retry manually later on.
            mRemoteManager.displayErrorNotification();
        }
    }

    void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    private void connect() throws IOException {
        createMediaPlayerIfNeeded();

        Log.d(TAG, "connecting to " + mMusicNetworkingHelper.getUrl());
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(mMusicNetworkingHelper.getUrl());
        mMediaPlayer.prepareAsync();

        mWifiLock.acquire();

        mState = State.Preparing;

        startTime = new Date();
    }

    private void handleError() {
        Log.d(TAG, "Connection error.");
        logStop("Disconnect");
        mMusicNetworkingHelper.stopMetadataDownload();
        tryConnect();
        logStreamStart(getApplicationContext(), "Reconnect");
    }

    /**
     * The service needs to be stopped manually.
     * AudioFocus needs to be abandoned manually.
     */
    private void cleanup() {
        mMusicNetworkingHelper.stopMetadataDownload();
        if (mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
        mState = State.Stopped;
        if (mWifiLock != null && mWifiLock.isHeld()) mWifiLock.release();
        if (activityCallback != null) activityCallback.playbackStateChanged();
    }

    private void giveUpAudioFocus() {
        if (mAudioFocusHelper != null) mAudioFocusHelper.abandonFocus();
    }

    public void onPrepared(MediaPlayer player) {
        connectionTries = 0;
        mState = State.Playing;

        mRemoteManager.displayConnectedNotification();
        mAudioFocusHelper.requestFocus();
        mMusicNetworkingHelper.startMetaDataDownloader();

        player.start();
    }

    @Override
    public void onMetaDataDownloaded(String artist, String title) {
        if (mState == State.Playing) {
            Log.d(TAG, String.format("now playing: %s - %s", artist, title));
            mRemoteManager.displaySongNotification(artist, title);
        }
    }

    @Override
    public void onMetaDataError() {
        if (mState == State.Playing) {
            mRemoteManager.displayDefaultNotification(mState);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        handleError();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        handleError();
    }

    public class MediaServiceBinder extends Binder {
        // Return this instance of LocalService so clients can call public methods
        public MediaServiceInterface getServiceInterface() {
            return MediaService.this;
        }
    }

    @Override
    public State getPlaybackState() {
        return mState;
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
        if (mState == State.Playing || mState == State.Preparing) {
            logStop("Service destroyed");
            Log.d(TAG, "onDestroy");
        }

        unregisterReceiver(mAudioBecomingNoisyReceiver);
        mRemoteManager.cancelAll();
        cleanup();
        giveUpAudioFocus();
        super.onDestroy();
    }

    public void logStop(String label) {
        int timeDiff = (int) (new Date().getTime() - startTime.getTime()) / 1000;
        logStreamStop(getApplicationContext(), label, timeDiff);
    }
}

