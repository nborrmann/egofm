package com.nilsbo.egofm.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.nilsbo.egofm.util.NewsItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Nils on 21.01.14.
 */
public class NewsListRequest extends Request<ArrayList<NewsItem>> {
    private static final String TAG = "com.nilsbo.egofm.volley.PlaylistRequest";

    private final Listener<ArrayList<NewsItem>> mListener;
    private static final String BASE_URL = "http://www.egofm.de%s";

    public NewsListRequest(int method, String url, Listener<ArrayList<NewsItem>> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;

    }

    public NewsListRequest(String url, Listener<ArrayList<NewsItem>> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(ArrayList<NewsItem> response) {
        mListener.onResponse(response);
    }


    @Override
    protected Response<ArrayList<NewsItem>> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        ArrayList<NewsItem> newsList = new ArrayList<NewsItem>();
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        try {
            final Document doc = Jsoup.parse(parsed);
            Elements newsElements = doc.select("div.blog-featured div.blog_content");

            for (Element news : newsElements) {
                NewsItem n = new NewsItem();
                n.date = news.getElementsByClass("app_date").text();
                n.title = news.getElementsByClass("app_title").text();
                n.subtitle = news.getElementsByClass("app_hl").text();
                n.imgUrl = String.format(BASE_URL, news.select("div.app_image a > img").first().attr("src"));
                n.imgUrlBig = n.imgUrl.replace("_thumb4", "_thumb2");
                n.link = String.format(BASE_URL, news.select("div.app_image a").first().attr("href"));
                newsList.add(n);
            }

        } catch (Exception e) {
            return null;
        }

        return Response.success(newsList, HttpHeaderParser.parseCacheHeaders(response));
    }
}



















