package com.nilsbo.egofm.networking;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;

/**
 * Created by Nils on 21.01.14.
 */
public class TrackRequest extends Request<String[]> {
    private final Listener<String[]> mListener;

    public TrackRequest(int method, String url, Listener<String[]> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;

    }

    public TrackRequest(String url, Response.Listener<String[]> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }
    @Override
    protected void deliverResponse(String[] response) {
        mListener.onResponse(response);
    }


    @Override
    protected Response<String[]> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        final String[] results = new String[2];
        try {
            parsed = new String(response.data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        try {
            final Document doc = Jsoup.parse(parsed);
            results[0] = doc.select("div#current > span.artist").text();
            results[1] = doc.select("div#current > span.song").text();
        } catch (Exception e) {
            return null;
        }

        if (TextUtils.isEmpty(results[0]) || TextUtils.isEmpty(results[1])) {
            return null;
        }
        return Response.success(results, HttpHeaderParser.parseCacheHeaders(response));
    }

}
