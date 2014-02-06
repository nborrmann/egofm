package com.nilsbo.egofm.networking;

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
            doc.select("script, meta, .hidden, div.app_date, div.app_title, div.app_hl, div.app_image, div.cck_headline, a.app_back, div.itp-share").remove(); //
            doc.select("div#inner-wrapper").attr("style", "margin-bottom: 0px; text-align: justify;");
            doc.head().append("    <style type=\"text/css\"> ::selection {background: #96c11f; color: #fff;} </style>\n");
            doc.head().append("<meta name=\"viewport\" content=\"width=560, user-scalable=0;\" />");

            for (Element e : doc.select("div.app_text [width]")) {
                String width = e.attr("width");
                String height = e.attr("height");
                if (width.matches("\\d+") && Integer.valueOf(width) > 560) {
                    int w = Integer.valueOf(width);
                    e.attr("width", "560");

                    if (height.matches("\\d+")) {
                        int h = Integer.valueOf(height);
                        e.attr("height", String.valueOf(h * 560 / w));
                    } else {
                        e.removeAttr("height");
                    }
                }
            }

            content = doc.html();

        } catch (Exception e) {
            return null;
        }

        return Response.success(content, HttpHeaderParser.parseCacheHeaders(response));
    }
}
