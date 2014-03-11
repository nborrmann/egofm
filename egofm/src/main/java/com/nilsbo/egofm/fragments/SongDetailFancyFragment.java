package com.nilsbo.egofm.fragments;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.widgets.ObservableScrollView;

import static com.nilsbo.egofm.util.FragmentUtils.clamp;
import static com.nilsbo.egofm.util.FragmentUtils.getActionBarHeight;

public class SongDetailFancyFragment extends SongDetailFragment implements ObservableScrollView.ScrollViewListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFancyFragment";

    private int mWidth;
    private int mHeight;

    private int mCurrentHeaderHeight;
    private int mMinHeaderHeight;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;
    }

    protected void initUI() {
        super.initUI();

        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        actionbarBg = rootView.findViewById(R.id.song_details_actionbar_bg);
        scrollView = (ObservableScrollView) rootView.findViewById(R.id.song_details_scrollview);
        headerContainer = (RelativeLayout) rootView.findViewById(R.id.song_details_header_container);

        scrollView.setScrollViewListener(this);
        scrollView.setFocusable(false);

        final ViewGroup.LayoutParams layoutParams = actionbarBg.getLayoutParams();
        layoutParams.height = getActionBarHeight(getActivity());
        actionbarBg.setLayoutParams(layoutParams);

        titleContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMinHeaderHeight = bottom - top + getActionBarHeight(getActivity());
            }
        });

        artistImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // if the manual measuring failed (for example in landscape mode), correct it by posting a runnable.
                if (mCurrentHeaderHeight != bottom - top) {
                    mCurrentHeaderHeight = bottom - top;
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            setScrollViewPadding();

                        }
                    });
                }
            }
        });

        Drawable artistPlaceholder = getResources().getDrawable(R.drawable.artist_placeholder);
        mCurrentHeaderHeight = Math.min((int) (1.0f * mWidth / artistPlaceholder.getIntrinsicWidth() * artistPlaceholder.getIntrinsicHeight()), mHeight);
        setScrollViewPadding();
    }

    private void setScrollViewPadding() {
        scrollView.setPadding(scrollView.getPaddingLeft(),
                mCurrentHeaderHeight, scrollView.getPaddingRight(), scrollView.getPaddingBottom());
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        float interpolation = clamp((1.0f * y) / (mCurrentHeaderHeight - mMinHeaderHeight), 1.0f, 0f);
        scrollHeader(interpolation, y);
    }

    private void scrollHeader(float interpolation, int y) {
        artistImage.setTranslationY(-1.0f * interpolation * (mCurrentHeaderHeight - mMinHeaderHeight));
        artistImage.setScrollY((int) (interpolation * (mCurrentHeaderHeight - mMinHeaderHeight) / -2.0f));
        titleContainer.setTranslationY(-1.0f * interpolation * (mCurrentHeaderHeight - mMinHeaderHeight));
        actionbarBg.setAlpha(1 - 20 * clamp(-1.0f * (y - mCurrentHeaderHeight + mMinHeaderHeight) / mWidth, 0.05f, 0f));
    }

    protected void setArtistImage(ImageLoader.ImageContainer response) {
        super.setArtistImage(response);

        int imageHeight = Math.min((int) (1.0f * mWidth / response.getBitmap().getWidth() * response.getBitmap().getHeight()), mHeight);

        final int oldHeight = mCurrentHeaderHeight;
        mCurrentHeaderHeight = imageHeight; // we need this updated for the scrolling interpolation to work properly
        setScrollViewPadding();
        scrollView.scrollBy(0, imageHeight - oldHeight);
    }
}
