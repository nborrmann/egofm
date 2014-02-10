package com.nilsbo.egofm.fragments;

import android.app.ActionBar;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
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
public class NewsItemTwoPaneFragment extends Fragment implements Response.ErrorListener, Response.Listener<String>, ObservableScrollView.ScrollViewListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsItemFragment";

    private static final String SAVE_STATED_WEBVIEW_TEXT = "SAVE_STATE_WEBVIEW_TEXT";
    private static final int MAX_HEADER_TRANSPARENCY = 170;

    final RequestQueue requestQueue = MyVolley.getRequestQueue();

    private String webViewText;
    private int mActionBarHeight;
    private int mHeaderActionBarDiff;
    private int mSubheaderHeight;
    private float mScrollRetardation;

    private ActionBar mActionBar;
    private View rootView;
    private WebView webView;
    private ObservableScrollView mScrollView;
    private CustomNetworkImageView mHeaderImage;
    private RelativeLayout mHeader;
    private LayoutInflater mInflater;
    private TextView customHeaderView;
    private NewsItem mNewsItem;
    private TextView mHeaderText;
    private TextView mSubtitleText;
    private TextView mDateText;
    private LinearLayout mEmptyView;
    private TextView mErrorText;
    private ProgressBar mProgressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_news_item, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initContent();
        initWebView();
        initActionBar();

        if (savedInstanceState != null) {
            webViewText = savedInstanceState.getString(SAVE_STATED_WEBVIEW_TEXT);
            displayWebview();
        } else {
            loadPage();
        }
    }

    private void initActionBar() {
        mActionBar = getActivity().getActionBar();
        mActionBarHeight = getActionBarHeight();

//        customHeaderView = (TextView) mInflater.inflate(R.layout.fragment_news_item_title, null, false);
//        customHeaderView.setText(mNewsItem.title);

//        mActionBar.setCustomView(customHeaderView);
//        mActionBar.setDisplayShowCustomEnabled(true);
//        mActionBar.setDisplayHomeAsUpEnabled(false);
//        mActionBar.setDisplayShowHomeEnabled(false);
//        mActionBar.setDisplayShowTitleEnabled(false);

//        mHeader = (RelativeLayout) rootView.findViewById(R.id.news_item_header);
        mHeaderText = (TextView) rootView.findViewById(R.id.news_item_header_text);
        mHeaderText.setText(mNewsItem.title);
        mHeaderText.setSelected(true);
        mSubtitleText = (TextView) rootView.findViewById(R.id.news_item_subtitle);
        mSubtitleText.setText(mNewsItem.subtitle);
        if (TextUtils.isEmpty(mNewsItem.subtitle)) mSubtitleText.setVisibility(View.GONE);
        mDateText = (TextView) rootView.findViewById(R.id.news_item_date);
        mDateText.setText(mNewsItem.date);
//        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.news_item_scrollcontainer);
//        mScrollView.setScrollViewListener(this);

        mEmptyView = (LinearLayout) rootView.findViewById(R.id.news_item_empty);
        mErrorText = (TextView) rootView.findViewById(R.id.news_item_empty_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.news_item_empty_progress);

        mHeaderImage = (CustomNetworkImageView) rootView.findViewById(R.id.news_item_header_image);
        mHeaderImage.setMinimumHeight(mActionBarHeight);
        mHeaderImage.setDefaultImageResId(R.drawable.default_news_image);
//        mHeaderImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                final int offset = Math.max(mHeaderImage.getHeight(), mActionBarHeight);
//                mScrollView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mScrollView.setPadding(mScrollView.getPaddingLeft(), offset, mScrollView.getPaddingRight(), mScrollView.getPaddingBottom());
//                    }
//                });
//
//                mSubheaderHeight = mSubtitleText.getHeight() + rootView.findViewById(R.id.news_item_seperator).getHeight() + mDateText.getHeight();
//                mHeaderActionBarDiff = mActionBarHeight - mHeaderImage.getHeight();
//                mScrollRetardation = 1 + mSubheaderHeight / (-1.0f * mHeaderActionBarDiff);
//                scrollHeader(mScrollView.getScrollY());
//            }
//        });
        mHeaderImage.setImageUrl(mNewsItem.imgUrlBig, mNewsItem.imgUrl, MyVolley.getImageLoader());
    }

    private void initContent() {
        if (getActivity() != null && getActivity().getIntent().getParcelableExtra("news_header") != null) {
            mNewsItem = getActivity().getIntent().getParcelableExtra("news_header");
        } else {
            mNewsItem = new NewsItem();
            mNewsItem.imgUrl = "http://www.egofm.de/images/content/2408/_thumb4/kygo_fb.jpg";
            mNewsItem.imgUrlBig = "http://www.egofm.de/images/content/2408/_thumb2/kygo_fb.jpg";
            mNewsItem.link = "http://www.egofm.de/musik/download-des-tages/2408-freedownload-seinabo-sey-younger-kygo-remix?tmpl=app";
            mNewsItem.title = "SEINABO SEY - YOUNGER (KYGO REMIX)";
            mNewsItem.subtitle = "DER FREE - DOWNLOAD DES TAGES";
            mNewsItem.date = "10.02.2014";
//            getActivity().finish();
        }
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

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        float interpolation = clamp((-1.0f * y) / mHeaderActionBarDiff / mScrollRetardation, 1.0f, 0f);
        scrollHeader(interpolation);
    }

    private void scrollHeader(float interpolation) {
        mHeader.setTranslationY((int) (interpolation * mHeaderActionBarDiff));
        mHeader.setScrollY((int) (interpolation * mHeaderActionBarDiff / 2.0f));
        mHeaderImage.setColorFilter(clamp((int) (interpolation * MAX_HEADER_TRANSPARENCY), MAX_HEADER_TRANSPARENCY, 0) << 24, PorterDuff.Mode.SRC_ATOP);
        customHeaderView.setAlpha(5 * interpolation - 4);
    }

    private void loadPage() {
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
        displayWebview();
    }

    private void displayWebview() {
        webView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        webView.clearView();
        webView.loadDataWithBaseURL("http://www.egofm.de/", webViewText, "text/html", "UTF-8", null);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STATED_WEBVIEW_TEXT, webViewText);
    }

    public int getActionBarHeight() {
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
                actionBarHeight += getResources().getDimensionPixelSize(resourceId);
        }
        return actionBarHeight;
    }

    public static int clamp(int value, int max, int min) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float max, float min) {
        return Math.min(Math.max(value, min), max);
    }
}
