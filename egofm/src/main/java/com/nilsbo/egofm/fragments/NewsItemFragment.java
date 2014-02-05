package com.nilsbo.egofm.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsItemRequest;
import com.nilsbo.egofm.util.NewsItem;
import com.nilsbo.egofm.widgets.ObservableScrollView;


/**
 * A placeholder fragment containing a simple view.
 */
public class NewsItemFragment extends Fragment implements Response.ErrorListener, Response.Listener<String>, ObservableScrollView.ScrollViewListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsItemFragment";
    private final String SAVE_STATED_WEBVIEW_TEXT = "SAVE_STATE_WEBVIEW_TEXT";
    private ActionBar mActionBar;

    private View rootView;
    private WebView webView;
    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private String webViewText;
    private ObservableScrollView mScrollView;
    private NetworkImageView mHeaderImage;
    private int mActionBarHeight;
    private RelativeLayout mHeader;

    private LayoutInflater mInflater;
    private TextView customHeaderView;
    private int mHeaderActionBarDiff;
    private NewsItem mNewsItem;
    private TextView mHeaderText;
    private TextView mSubtitleText;

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

        mActionBar = getActivity().getActionBar();
        mActionBarHeight = getActionBarHeight();

        customHeaderView = (TextView) mInflater.inflate(R.layout.fragment_news_item_title, null, false);
        customHeaderView.setText(mNewsItem.title);
        mActionBar.setCustomView(customHeaderView);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        mHeader = (RelativeLayout) rootView.findViewById(R.id.news_item_header);
        mHeaderText = (TextView) rootView.findViewById(R.id.news_item_header_text);
        mHeaderText.setText(mNewsItem.title);
        mSubtitleText = (TextView) rootView.findViewById(R.id.news_item_subtitle);
        mSubtitleText.setText(mNewsItem.subtitle);
        mHeaderImage = (NetworkImageView) rootView.findViewById(R.id.news_item_header_image);
        mHeaderImage.setImageUrl(mNewsItem.imgUrlBig, MyVolley.getImageLoader());

        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.news_item_scrollcontainer);
        mScrollView.setScrollViewListener(this);
        rootView.findViewById(R.id.news_item_header_text).setSelected(true);

        mHeaderImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final int offset = Math.max(mHeaderImage.getHeight(), mActionBarHeight);
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.setPadding(mScrollView.getPaddingLeft(), offset, mScrollView.getPaddingRight(), mScrollView.getPaddingBottom());
                    }
                });

                mHeaderActionBarDiff = -mHeaderImage.getHeight() + mActionBarHeight;
                scrollHeader(mScrollView.getScrollY());
            }
        });


        if (savedInstanceState != null) {
            webViewText = savedInstanceState.getString(SAVE_STATED_WEBVIEW_TEXT);
            webView.loadDataWithBaseURL("http://www.egofm.de/", webViewText, "text/html", "UTF-8", null);
        } else {
            loadPage();
        }

    }

    private void initContent() {
        if (getActivity() != null && getActivity().getIntent().getParcelableExtra("news_header") != null) {
            mNewsItem = getActivity().getIntent().getParcelableExtra("news_header");
        } else {
            // TODO Debug NewsItem
            mNewsItem = new NewsItem();
            mNewsItem.imgUrl = "http://www.egofm.de/images/content/2335/_thumb4/say_lou_lou.jpg";
            mNewsItem.imgUrlBig = "http://www.egofm.de/images/content/2335/_thumb2/say_lou_lou.jpg";
            mNewsItem.subtitle = "DER FREE - DOWNLOAD DES TAGES";
            mNewsItem.title = "SAY LOU LOU - FEELS LIKE WE ONLY GO BACKWARDS (TAME IMPALA COVER)";
            mNewsItem.date = "03.02.2014";
            mNewsItem.link = "http://www.egofm.de/musik/download-des-tages/2335-say-lou-lou-feels-like-we-only-go-backwards-dolo?tmpl=app";
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
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        scrollHeader(y);
    }

    private void scrollHeader(int y) {
        mHeader.setTranslationY(Math.max(-y, mHeaderActionBarDiff));
        mHeader.setScrollY(Math.max(-y, mHeaderActionBarDiff) / 2);
        if (mHeaderActionBarDiff != 0) {
            mHeaderImage.setColorFilter(clamp((180 * -y / mHeaderActionBarDiff), 180, 0) << 24, PorterDuff.Mode.SRC_ATOP);
            customHeaderView.setAlpha(clamp(((5 * (1.0f * -y / mHeaderActionBarDiff) - 4)), 1, 0)); // I have no idea what I'm doing here.
        }
    }

    private void loadPage() {
        NewsItemRequest playlistRequest = new NewsItemRequest(mNewsItem.link, this, this);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(20000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse " + error.getMessage());
        //TODO error handling
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "onResponse");
        webView.loadDataWithBaseURL("http://www.egofm.de/", response, "text/html", "UTF-8", null);
        webViewText = response;
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
            int result = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
            actionBarHeight += result;
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
