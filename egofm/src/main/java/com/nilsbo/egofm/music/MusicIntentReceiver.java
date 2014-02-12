package com.nilsbo.egofm.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.nilsbo.egofm.MediaService;

/**
 * Created by Nils on 07.02.14.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "com.nilsbo.egofm.music.MusicIntentReceiver";

    public static final String NOTIFICATION_CONTROL_CLOSE = "com.nilsbo.egofm.control.CLOSE";
    public static final String NOTIFICATION_CONTROL_STARTSTOP = "com.nilsbo.egofm.control.STARTSTOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(NOTIFICATION_CONTROL_CLOSE)) {
            context.startService(new Intent(MediaService.ACTION_CLOSE));
        } else if (action.equals(NOTIFICATION_CONTROL_STARTSTOP)) {
            context.startService(new Intent(MediaService.ACTION_STARTSTOP));
        } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            context.startService(new Intent(MediaService.ACTION_PAUSE));
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    context.startService(new Intent(MediaService.ACTION_PLAYPAUSE));
                    break;
            }
        }
    }
}