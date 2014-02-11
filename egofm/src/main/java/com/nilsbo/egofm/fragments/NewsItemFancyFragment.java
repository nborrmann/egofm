package com.nilsbo.egofm.fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.widgets.ObservableScrollView;


/**
 * A placeholder fragment containing a simple view.
 */
public class NewsItemFancyFragment extends NewsItemFragment implements Response.ErrorListener, Response.Listener<String>, ObservableScrollView.ScrollViewListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsItemFancyFragment";

    private static final int MAX_HEADER_TRANSPARENCY = 170;

    private int mActionBarHeight;
    private int mHeaderActionBarDiff;
    private int mSubheaderHeight;
    private float mScrollRetardation;

    private TextView customHeaderView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initUI() {
        super.initUI();

        mActionBarHeight = getActionBarHeight();
        customHeaderView = (TextView) mInflater.inflate(R.layout.fragment_news_item_title, null, false);

        mActionBar.setCustomView(customHeaderView);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        mScrollView.setScrollViewListener(this);

        mHeaderImage.setMinimumHeight(mActionBarHeight);
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

                mSubheaderHeight = mSubtitleText.getHeight() + mSubtitleDivider.getHeight() + mDateText.getHeight();
                mHeaderActionBarDiff = mActionBarHeight - mHeaderImage.getHeight();
                mScrollRetardation = 1 + mSubheaderHeight / (-1.0f * mHeaderActionBarDiff);
                scrollHeader(mScrollView.getScrollY());
            }
        });
    }

    protected void setInitialData() {
        super.setInitialData();
        customHeaderView.setText(mNewsItem.title);
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
