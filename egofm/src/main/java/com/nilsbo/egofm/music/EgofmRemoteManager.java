package com.nilsbo.egofm.music;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.activities.MainActivity;

/**
 * Created by Nils on 21.01.14.
 */
public class EgofmRemoteManager {

    private static final int NOTIFICATION_ID = 100;
    private final Context context;
    private final NotificationManager mNotificationManager;
    private final AudioManager mAudioManager;
    private final Resources resources;
    private NotificationCompat.Builder mBuilder;

    private RemoteControlClient myRemoteControlClient;
    private ComponentName myEventReceiver;

    /**
     * This class manages the notification and lockscreen widget.
     *
     * @param context
     * @param systemService
     * @param mAudioManager
     */
    public EgofmRemoteManager(Context context, NotificationManager systemService, AudioManager mAudioManager) {
        this.context = context;
        mNotificationManager = systemService;
        this.mAudioManager = mAudioManager;
        mBuilder = new NotificationCompat.Builder(context);
        resources = context.getResources();

        myEventReceiver = new ComponentName(context, MusicIntentReceiver.class);
    }

    private void displayMediaNotification(String title, String text, String ticker, MediaService.State started) {
        Notification notif = buildMediaNotification(title, text, ticker, started);
        mNotificationManager.notify(NOTIFICATION_ID, notif);
    }

    private void startForeground(Service service, String title, String text, String ticker) {
        service.startForeground(NOTIFICATION_ID, buildMediaNotification(title, text, ticker, MediaService.State.Playing));
    }

    private Notification buildMediaNotification(String title, String text, String ticker, MediaService.State state) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingetent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingetent);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        contentView.setImageViewResource(R.id.notification_image, R.drawable.notification_big);
        contentView.setTextViewText(R.id.notification_title, title);
        contentView.setTextViewText(R.id.notification_text, text);
        if (state == MediaService.State.Stopped) {
            contentView.setImageViewResource(R.id.notification_startstop, R.drawable.ic_action_play);
        } else {
            contentView.setImageViewResource(R.id.notification_startstop, R.drawable.ic_action_stop);
        }


        Intent closeIntent = new Intent();
        closeIntent.setAction(MusicIntentReceiver.NOTIFICATION_CONTROL_CLOSE);
        PendingIntent closePendingIntent =
                PendingIntent.getBroadcast(context, 0, closeIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notification_close, closePendingIntent);


        Intent startStopIntent = new Intent();
        startStopIntent.setAction(MusicIntentReceiver.NOTIFICATION_CONTROL_STARTSTOP);
        PendingIntent startStopPendingIntent =
                PendingIntent.getBroadcast(context, 0, startStopIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notification_startstop, startStopPendingIntent);


        mBuilder.setContent(contentView);
        mBuilder.setSmallIcon(R.drawable.notification_small);
        mBuilder.setTicker(ticker);
        mBuilder.setPriority(1);
        mBuilder.setOngoing(true);

        return mBuilder.build();
    }

    public void startService(MediaService mediaService) {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connecting);
        startForeground(mediaService, title, text, text);

        mAudioManager.registerMediaButtonEventReceiver(myEventReceiver);
        createRemoteControlClientIfNeeded();
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_BUFFERING);
    }

    private void updateRemoteControlClient(String title, String text, int state) {
        myRemoteControlClient.setPlaybackState(state);
        myRemoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY);

        Bitmap mDummyAlbumArt = BitmapFactory.decodeResource(resources, R.drawable.egofm_icon_large);

        myRemoteControlClient.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, text)
                .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                        mDummyAlbumArt)
                .apply();

    }

    public void displayConnectingNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connecting);

        displayMediaNotification(title, text, text, MediaService.State.Playing);
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_BUFFERING);
    }

    public void displayErrorNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connection_error);
        final String ticker = resources.getString(R.string.notification_disconnected);

        displayMediaNotification(title, text, ticker, MediaService.State.Stopped);
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_PAUSED);
    }

    public void displayDisconnectedNotification(MediaService.State mState) {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_disconnected);

        displayMediaNotification(title, text, null, mState);
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_PAUSED);
    }

    public void displayConnectedNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_default_text);
        final String ticker = resources.getString(R.string.notification_connected);

        displayMediaNotification(title, text, ticker, MediaService.State.Playing);
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_BUFFERING);
    }

    public void displaySongNotification(String artist, String title) {
        displayMediaNotification(artist, title, String.format("%s - %s", artist, title), MediaService.State.Playing);
        updateRemoteControlClient(artist, title, RemoteControlClient.PLAYSTATE_BUFFERING);
    }

    public void displayDefaultNotification(MediaService.State started) {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_default_text);

        displayMediaNotification(title, text, null, started);
        updateRemoteControlClient(title, text, RemoteControlClient.PLAYSTATE_BUFFERING);
    }

    private void createRemoteControlClientIfNeeded() {
        if (myRemoteControlClient == null) {
            // build the Pendingetent for the remote control client
            Intent mediaButtontent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtontent.setComponent(myEventReceiver);

            PendingIntent mediaPendingetent = PendingIntent.getBroadcast(context, 0, mediaButtontent, 0);
            // create and register the remote control client
            myRemoteControlClient = new RemoteControlClient(mediaPendingetent);
            mAudioManager.registerRemoteControlClient(myRemoteControlClient);
        }
    }
}
