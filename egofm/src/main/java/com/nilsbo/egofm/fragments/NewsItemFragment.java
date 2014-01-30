package com.nilsbo.egofm.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsItemRequest;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewsItemFragment extends Fragment implements Response.ErrorListener, Response.Listener<String> {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsItemFragment";

    private View rootView;
    private WebView webView;
    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_news_item, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity().getIntent().getStringExtra("url") == null) {
            url = "http://www.egofm.de/themen/allgemein/2265-das-egofm-winterkino-2014?tmpl=app";
        } else {
            url = String.format("http://www.egofm.de%s", getActivity().getIntent().getStringExtra("url"));
        }

        webView = (WebView) rootView.findViewById(R.id.news_item_webview);

        NewsItemRequest playlistRequest = new NewsItemRequest(url, this, this);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);


        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setTextZoom(150);
        webView.getSettings().setUseWideViewPort(true);
//        webView.setInitialScale(200);
//        webView.setInitialScale(100);

//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true; //overwrite LongClick so the text isn't selectable
//            }
//        });
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "onResponse");
        webView.loadDataWithBaseURL("http://www.egofm.de/", response, "text/html", "UTF-8", null);
//        webView.setInitialScale(100);
    }
}
