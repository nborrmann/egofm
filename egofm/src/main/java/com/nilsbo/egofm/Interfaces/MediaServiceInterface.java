package com.nilsbo.egofm.Interfaces;

/**
 * Created by Nils on 20.01.14.
 */
public interface MediaServiceInterface {
    public boolean isStarted();

    public void startMediaPlayer();

    public void stopMediaPlayer();

    public void registerActivityCallback(EgofmActivityInterface activityCallback);
}
