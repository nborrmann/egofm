package com.nilsbo.egofm.networking;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
            doc.head().append("    <style type=\"text/css\"> ::selection {background: #96c11f; color: #fff;} </style>\n");

            for (Element e : doc.select("div.app_text [width]")) {
                String width = e.attr("width");
                if (width.matches("\\d+") && Integer.valueOf(width) > 560) {
                    e.attr("width", "100%");
                    e.removeAttr("height");
                    Log.d(TAG, e.outerHtml());
                }

            }

            content = doc.html();
            Log.d(TAG, content);

        } catch (Exception e) {
            return null;
        }

        return Response.success(content, HttpHeaderParser.parseCacheHeaders(response));
    }
}
