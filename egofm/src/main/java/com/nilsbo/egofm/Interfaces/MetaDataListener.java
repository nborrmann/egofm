package com.nilsbo.egofm.Interfaces;

/**
 * Created by Nils on 08.02.14.
 */
public interface MetaDataListener {
    public void onMetaDataDownloaded(String artist, String title);

    public void onMetaDataError();
}
