package com.nilsbo.egofm.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.nilsbo.egofm.util.PlaylistItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Nils on 21.01.14.
 */
public class PlaylistRequest extends Request<ArrayList<PlaylistItem>> {
    private static final String TAG = "com.nilsbo.egofm.volley.PlaylistRequest";

    private final Listener<ArrayList<PlaylistItem>> mListener;

    public PlaylistRequest(int method, String url, Listener<ArrayList<PlaylistItem>> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;

    }

    public PlaylistRequest(String url, Listener<ArrayList<PlaylistItem>> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }
    @Override
    protected void deliverResponse(ArrayList<PlaylistItem> response) {
        mListener.onResponse(response);
    }


    @Override
    protected Response<ArrayList<PlaylistItem>> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        ArrayList<PlaylistItem> playlist = new ArrayList<PlaylistItem>();
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        try {

            final Document doc = Jsoup.parse(parsed);
            Elements playlistElements = doc.select("div.playlist > div.playlist_row");

            for (Element e : playlistElements) {
                PlaylistItem p = new PlaylistItem();
                p.artist = e.getElementsByClass("artist").text();
                p.artist = p.artist.substring(0, p.artist.length() - 2);
                p.title = e.getElementsByClass("name").text().substring(1);
                p.date = e.getElementsByClass("start-date").text();
                p.time = e.getElementsByClass("start-time").text();
                playlist.add(p);
            }

        } catch (Exception e) {
            return null;
        }

        return Response.success(playlist, HttpHeaderParser.parseCacheHeaders(response));
    }
}
