package com.nilsbo.egofm.widgets;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

/**
 * This is a custom implementation of NetworkImageView.
 * This NetworkImageView will takes an url as defaultImage instead of and Id.
 * The defaultImage will only be set if it is already cached by volley.
 */
public class CustomNetworkImageView extends ImageView {
    /**
     * The URL of the network image to load
     */
    protected String mUrl;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;

    private String mDefaultUrl;
    /**
     * Local copy of the ImageLoader.
     */
    private ImageLoader mImageLoader;

    /**
     * Current ImageContainer. (either in-flight or finished)
     */
    private ImageContainer mImageContainer;
    private ImageContainer mDefaultImageContainer;

    public CustomNetworkImageView(Context context) {
        this(context, null);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * NetworkImageView#setDefaultImageResId(int) on the view.
     * <p/>
     * NOTE: If applicable, NetworkImageView#setDefaultImageResId(int) and
     * NetworkImageView#setErrorImageResId(int) should be called prior to calling
     * this function.
     *
     * @param url         The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, String defaultUrl, ImageLoader imageLoader) {
        mUrl = url;
        mDefaultUrl = defaultUrl;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        if (mUrl != null && !mImageLoader.isCached(mUrl, 0, 0)) {
            setDefaultImageOrNull(false);
        }
        loadImageIfNecessary(false);
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     *
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();

        boolean isFullyWrapContent = getLayoutParams() != null
                && getLayoutParams().height == LayoutParams.WRAP_CONTENT
                && getLayoutParams().width == LayoutParams.WRAP_CONTENT;
        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setDefaultImageOrNull(isInLayoutPass);
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
                setDefaultImageOrNull(isInLayoutPass);
            }
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageContainer newContainer = mImageLoader.get(mUrl,
                new ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mErrorImageId != 0) {
                            setImageResource(mErrorImageId);
                        }
                    }

                    @Override
                    public void onResponse(final ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null) {
                            setImageBitmap(response.getBitmap());
                        } else {
                            setDefaultImageOrNull(isInLayoutPass);
                        }
                    }
                });

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
    }

    private void setDefaultImageOrNull(final boolean isInLayoutPass) {
        if (mDefaultUrl != null && mImageLoader.isCached(mDefaultUrl, 0, 0)) {
            // if there was an old request in this view, check if it needs to be canceled.
            if (mDefaultImageContainer != null && mDefaultImageContainer.getRequestUrl() != null) {
                return;
            }

            ImageContainer newDefaultContainer = mImageLoader.get(mDefaultUrl, new ImageListener() {
                @Override
                public void onResponse(final ImageContainer response, boolean isImmediate) {
                    if (isImmediate && isInLayoutPass) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                onResponse(response, false);
                            }
                        });
                        return;
                    }

                    if (response.getBitmap() != null) {
                        setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (mDefaultImageId != 0) setImageResource(mDefaultImageId);
                    else setImageBitmap(null);
                }
            });
            mDefaultImageContainer = newDefaultContainer;
        } else {
            if (mDefaultImageId != 0) setImageResource(mDefaultImageId);
            else setImageBitmap(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }
        if (mDefaultImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mDefaultImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mDefaultImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
