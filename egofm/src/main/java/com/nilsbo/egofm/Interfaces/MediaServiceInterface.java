package com.nilsbo.egofm.Interfaces;

import com.nilsbo.egofm.MediaService;

/**
 * Created by Nils on 20.01.14.
 */
public interface MediaServiceInterface {
    public MediaService.State getPlaybackState();

    public void registerActivityCallback(EgofmActivityInterface activityCallback);
}
