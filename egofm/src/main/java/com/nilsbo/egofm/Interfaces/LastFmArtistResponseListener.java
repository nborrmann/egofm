package com.nilsbo.egofm.Interfaces;

import java.util.ArrayList;

/**
 * Created by Nils on 28.02.14.
 */
public interface LastFmArtistResponseListener {
    public void onArtistResponse(String artistDescription, String imageUrl);

    public void onTitleResponse(int duration, int trackAlbumPosition, String albumImageUrl, String albumTitle, ArrayList<String> trackTags);
}
