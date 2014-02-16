package com.nilsbo.egofm.fragments;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsItemRequest;
import com.nilsbo.egofm.util.NewsItem;
import com.nilsbo.egofm.widgets.CustomNetworkImageView;
import com.nilsbo.egofm.widgets.ObservableScrollView;


/**
 * A placeholder fragment containing a simple view.
 */
public class NewsItemFragment extends Fragment implements Response.ErrorListener, Response.Listener<String> {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsItemFancyFragment";

    protected static final String SAVED_STATED_WEBVIEW_TEXT = "SAVE_STATE_WEBVIEW_TEXT";
    protected static final String SAVED_STATE_NEWS_ITEM = "SAVED_STATE_NEWS_ITEM";
    public static final String ARG_NEWS_ITEM = "ARGUMENT_NEWS_ITEM";

    protected final RequestQueue requestQueue = MyVolley.getRequestQueue();

    protected String webViewText;

    protected ActionBar mActionBar;
    protected LayoutInflater mInflater;
    protected View rootView;
    protected WebView webView;
    protected CustomNetworkImageView mHeaderImage;
    protected RelativeLayout mHeader;
    protected NewsItem mNewsItem;
    protected TextView mHeaderText;
    protected TextView mSubtitleText;
    protected TextView mDateText;
    protected LinearLayout mEmptyView;
    protected TextView mErrorText;
    protected ProgressBar mProgressBar;
    protected ObservableScrollView mScrollView;
    protected View mSubtitleDivider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_news_item, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initWebView();
        initUI();

        if (savedInstanceState != null) {
            preLoadUISetup();
            mNewsItem = savedInstanceState.getParcelable(SAVED_STATE_NEWS_ITEM);
            setInitialData();
            webViewText = savedInstanceState.getString(SAVED_STATED_WEBVIEW_TEXT);
            showWebView();
        } else {
            if (getArguments() != null && getArguments().containsKey(ARG_NEWS_ITEM)) {
                mNewsItem = getArguments().getParcelable(ARG_NEWS_ITEM);

                loadPage();
                setInitialData();
            }
        }
    }

    protected void initUI() {
        mActionBar = getActivity().getActionBar();

        mHeader = (RelativeLayout) rootView.findViewById(R.id.news_item_header);
        mHeaderText = (TextView) rootView.findViewById(R.id.news_item_header_text);
        mHeaderText.setSelected(true);
        mSubtitleText = (TextView) rootView.findViewById(R.id.news_item_subtitle);
        mSubtitleDivider = rootView.findViewById(R.id.news_item_seperator);
        mDateText = (TextView) rootView.findViewById(R.id.news_item_date);

        mEmptyView = (LinearLayout) rootView.findViewById(R.id.news_item_empty);
        mErrorText = (TextView) rootView.findViewById(R.id.news_item_empty_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.news_item_empty_progress);
        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.news_item_scrollcontainer);

        mHeaderImage = (CustomNetworkImageView) rootView.findViewById(R.id.news_item_header_image);
        mHeaderImage.setDefaultImageResId(R.drawable.default_news_image);

        mSubtitleDivider.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mHeader.setVisibility(View.GONE);
    }

    protected void setInitialData() {
        if (mNewsItem == null) return;

        mHeaderText.setText(mNewsItem.title);
        mSubtitleText.setText(mNewsItem.subtitle);
        if (TextUtils.isEmpty(mNewsItem.subtitle)) mSubtitleText.setVisibility(View.GONE);
        else mSubtitleText.setVisibility(View.VISIBLE);
        mDateText.setText(mNewsItem.date);
        mHeaderImage.setImageUrl(mNewsItem.imgUrlBig, mNewsItem.imgUrl, MyVolley.getImageLoader());
    }

    private void preLoadUISetup() {
        webView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        mSubtitleDivider.setVisibility(View.VISIBLE);
        mHeader.setVisibility(View.VISIBLE);

//        mScrollView.fullScroll(ScrollView.FOCUS_UP);
//        mScrollView.computeScroll();
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0, 0);
            }
        });
    }


    private void showWebView() {
        webView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        webView.clearView();
        webView.loadDataWithBaseURL("http://www.egofm.de/", webViewText, "text/html", "UTF-8", null);
    }

    private void initWebView() {
        webView = (WebView) rootView.findViewById(R.id.news_item_webview);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            webView.getSettings().setUseWideViewPort(true);
        } else {
            webView.getSettings().setTextZoom(90);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.getSettings().setUseWideViewPort(false);
        }

        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setAppCacheMaxSize(2 * 1024 * 1024);
    }

    protected void loadPage() {
        preLoadUISetup();

        NewsItemRequest playlistRequest = new NewsItemRequest(mNewsItem.link, this, this);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(20000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse " + error.getMessage());

        webView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(String response) {
        webViewText = response;
        showWebView();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATED_WEBVIEW_TEXT, webViewText);
        outState.putParcelable(SAVED_STATE_NEWS_ITEM, mNewsItem);
    }

    public void setContent(NewsItem item) {
        mNewsItem = item;

        if (getActivity() != null) {
            setInitialData();
            loadPage();
        }
    }
}
