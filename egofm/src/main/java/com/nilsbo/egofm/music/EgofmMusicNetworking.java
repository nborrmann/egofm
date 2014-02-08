package com.nilsbo.egofm.music;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.Interfaces.MetaDataListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.TrackRequest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Nils on 08.02.14.
 */
public class EgofmMusicNetworking {
    private static final String TAG = "com.nilsbo.egofm.music.EgofmMusicNetworking";

    private final String streamUrlHQ;
    private final String streamUrlLQ;

    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Context context;
    private final MetaDataListener mCallback;
    private ScheduledFuture metaDataHandler;

    public EgofmMusicNetworking(Context context, MetaDataListener mCallback) {
        this.context = context;
        this.mCallback = mCallback;
        streamUrlHQ = context.getResources().getString(R.string.url_stream_high);
        streamUrlLQ = context.getResources().getString(R.string.url_stream_low);
    }

    public void startMetaDataDownloader() {
        metaDataHandler = scheduler.scheduleAtFixedRate(metaDataDownloader, 0, getUpdateInterval(), SECONDS);
    }

    public void stopMetadataDownload() {
        if (metaDataHandler != null) metaDataHandler.cancel(true);
    }

    final Runnable metaDataDownloader = new Runnable() {
        public void run() {
            TrackRequest metaDataRequest = new TrackRequest("http://www.egofm.de/templates/egofm/get_track.php", new Response.Listener<String[]>() {
                @Override
                public void onResponse(String[] response) {
                    mCallback.onMetaDataDownloaded(response[0], response[1]);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mCallback.onMetaDataError();
                }
            }
            );

            requestQueue.add(metaDataRequest);
        }
    };

    public long getUpdateInterval() {
        final String intervalStr = PreferenceManager.getDefaultSharedPreferences(context).getString("metadata_interval", "20");
        int interval = 20;
        try {
            interval = Integer.parseInt(intervalStr);
        } catch (NumberFormatException e) {
        }
        return interval;
    }

    public String getUrl() {
        final String streamQuality = PreferenceManager.getDefaultSharedPreferences(context).getString("streamquality", "wifihigh");
        if (streamQuality.equals("high")) {
            return streamUrlHQ;
        } else if (streamQuality.equals("wifihigh")) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi != null && mWifi.isConnected()) return streamUrlHQ;
            else return streamUrlLQ;
        } else {
            return streamUrlLQ;
        }
    }
}
