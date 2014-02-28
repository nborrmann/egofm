package com.nilsbo.egofm.networking;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nilsbo.egofm.Interfaces.LastFmArtistResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.nilsbo.egofm.util.FragmentUtils.logTiming;

/**
 * Created by Nils on 21.01.14.
 */
public class LastFmSongSearchRequest {
    private static final String TAG = "com.nilsbo.egofm.networking.LastFmArtistRequest";

    private final LastFmArtistResponseListener mListener;
    private RequestQueue mRequestQueue = MyVolley.getRequestQueue();
    private Date startDate;
    private final String url;

    public LastFmSongSearchRequest(String artist, String title, LastFmArtistResponseListener listener, ErrorListener errorListener) {
        mListener = listener;
        startDate = new Date();

        url = "http://ws.audioscrobbler.com/2.0/?method=track.search&api_key=63e6e4b1a4db2dda7585d9e82b2f723e&artist=" + artist + "&track=" + title + "&format=json&limit=1";

        JsonObjectRequest artistRequest = new JsonObjectRequest(Request.Method.GET, url, null, new VolleySongRequestListener(), errorListener);
        mRequestQueue.add(artistRequest);
    }

    private class VolleySongRequestListener implements Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            String artist = null;
            String title = null;

            try {
                JSONObject track = response.getJSONObject("results").getJSONObject("trackmatches").getJSONObject("track");
                artist = track.getString("artist");
                title = track.getString("name");
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing Last.fm Song Search", e);
            }

            logTiming("egoFM Request", "LastFM Song Search", startDate);
            mListener.onSongSearchResponse(artist, title);
        }
    }
}
