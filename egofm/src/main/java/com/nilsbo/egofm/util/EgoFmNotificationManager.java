package com.nilsbo.egofm.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.nilsbo.egofm.activities.MainActivity;
import com.nilsbo.egofm.MediaService;
import com.nilsbo.egofm.R;

/**
 * Created by Nils on 21.01.14.
 */
public class EgoFmNotificationManager {

    private static final int NOTIFICATION_ID = 100;
    private final Context context;
    private final NotificationManager mNotificationManager;
    private final Resources resources;
    private NotificationCompat.Builder mBuilder;

    public EgoFmNotificationManager(Context context, NotificationManager systemService) {
        this.context = context;
        mNotificationManager = systemService;
        mBuilder = new NotificationCompat.Builder(context);
        resources = context.getResources();
    }

    private void displayMediaNotification(String title, String text, String ticker, boolean started) {
        Notification notif = buildMediaNotification(title, text, ticker, started);
        mNotificationManager.notify(NOTIFICATION_ID, notif);
    }

    private void startForeground(Service service, String title, String text, String ticker) {
        service.startForeground(NOTIFICATION_ID, buildMediaNotification(title, text, ticker, true));
    }

    private Notification buildMediaNotification(String title, String text, String ticker, boolean started) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingetent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingetent);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        contentView.setImageViewResource(R.id.notification_image, R.drawable.notification_big);
        contentView.setTextViewText(R.id.notification_title, title);
        contentView.setTextViewText(R.id.notification_text, text);
        if (!started) {
            contentView.setImageViewResource(R.id.notification_startstop, R.drawable.ic_action_play);
        } else {
            contentView.setImageViewResource(R.id.notification_startstop, R.drawable.ic_action_stop);
        }


        Intent closeIntent = new Intent();
        closeIntent.setAction(MediaService.BROADCAST_ID_CLOSE);
        PendingIntent closePendingIntent =
                PendingIntent.getBroadcast(context, 0, closeIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notification_close, closePendingIntent);


        Intent startStopIntent = new Intent();
        startStopIntent.setAction(MediaService.BROADCAST_ID_STARTSTOP);
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

    @Deprecated
    public Notification buildNotification(String title, String text, String ticker) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.notification_small);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setTicker(ticker);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingetent =stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingetent);
        return mBuilder.build();
    }

    public void startService(MediaService mediaService) {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connecting);
        startForeground(mediaService, title, text, text);
    }

    public void displayConnectingNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connecting);
        displayMediaNotification(title, text, text, true);
    }

    public void displayDisconnectedNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_connection_error);
        final String ticker = resources.getString(R.string.notification_disconnected);
        displayMediaNotification(title, text, ticker, false);
    }

    public void displayConnectedNotification() {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_default_text);
        final String ticker = resources.getString(R.string.notification_connected);
        displayMediaNotification(title, text, ticker, true);
    }

    public void displaySongNotification(String artist, String title) {
        displayMediaNotification(artist, title, String.format("%s - %s", artist, title), true);
    }

    public void displayDefaultNotification(boolean started) {
        final String title = resources.getString(R.string.notification_default_title);
        final String text = resources.getString(R.string.notification_default_text);
        displayMediaNotification(title, text, null, started);
    }
}
