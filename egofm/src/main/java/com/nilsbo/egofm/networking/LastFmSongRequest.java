package com.nilsbo.egofm.networking;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nilsbo.egofm.Interfaces.LastFmArtistResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import static com.nilsbo.egofm.util.FragmentUtils.logTiming;

/**
 * Created by Nils on 21.01.14.
 */
public class LastFmSongRequest {
    private static final String TAG = "com.nilsbo.egofm.networking.LastFmArtistRequest";

    private final LastFmArtistResponseListener mListener;
    private RequestQueue mRequestQueue = MyVolley.getRequestQueue();
    private Date startDate;
    private final String url;

    public LastFmSongRequest(String artist, String title, LastFmArtistResponseListener listener, ErrorListener errorListener) {
        mListener = listener;
        startDate = new Date();

        url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=63e6e4b1a4db2dda7585d9e82b2f723e&artist=" + artist + "&track=" + title + "&format=json";

        JsonObjectRequest artistRequest = new JsonObjectRequest(Request.Method.GET, url, null, new VolleySongRequestListener(), errorListener);
        mRequestQueue.add(artistRequest);
    }

    private class VolleySongRequestListener implements Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            int duration = 0;
            int trackAlbumPosition = 0;
            String albumImageUrl = null;
            String albumTitle = null;
            ArrayList<String> trackTags = null;

            try {
                duration = response.getJSONObject("track").getInt("duration");
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing duration from last.fm response " + url, e);
            }

            try {
                JSONObject album = response.getJSONObject("track").getJSONObject("album");
                albumTitle = album.getString("title");
                JSONArray albumImages = album.getJSONArray("image");
                albumImageUrl = albumImages.getJSONObject(Math.min(albumImages.length(), 2)).getString("#text");
                trackAlbumPosition = album.getJSONObject("@attr").getInt("position");
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing album from last.fm response", e);
            }

            try {
                JSONArray trackTagsJSON = response.getJSONObject("track").getJSONObject("toptags").getJSONArray("tag");
                trackTags = new ArrayList<String>();

                for (int i = 0; i < trackTagsJSON.length(); i++) {
                    trackTags.add(trackTagsJSON.getJSONObject(i).getString("name"));
                }
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing tags from last.fm response", e);
            }

            logTiming("egoFM Request", "LastFM Song Info", startDate);
            mListener.onSongResponse(duration, trackAlbumPosition, albumImageUrl, albumTitle, trackTags);
        }
    }
}
