package com.nilsbo.egofm.networking;

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
public class NewsItemRequest extends Request<String> {
    private static final String TAG = "com.nilsbo.egofm.volley.PlaylistRequest";

    private final Listener<String> mListener;

    public NewsItemRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;

    }

    public NewsItemRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }


    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        String content;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        try {
            final Document doc = Jsoup.parse(parsed);
            doc.select("script, .hidden, div.app_date, div.app_image, div.app_title, a.app_back, div.app_h1, div.itp-share, div.cck_headline").remove();
            doc.select("div#inner-wrapper").attr("style", "margin-bottom: 0px; text-align: justify;");
            content = doc.html();

        } catch (Exception e) {
            return null;
        }

        return Response.success(content, HttpHeaderParser.parseCacheHeaders(response));
    }
}
