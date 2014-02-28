package com.nilsbo.egofm.networking;

import android.app.ActivityManager;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.net.URI;

public class MyVolley {
    private static final String TAG = "com.nilsbo.egofm.networking.MyVolley";

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private MyVolley() {
        // no instances
    }

    public static class MyUrlRewriter implements HurlStack.UrlRewriter {
        @Override
        public String rewriteUrl(String originalUrl) {
            originalUrl = originalUrl.replaceAll(" ", "%20");
            return URI.create(originalUrl).toASCIIString();
        }
    }

    public static void init(Context context) {
        HurlStack myHurl = new HurlStack(new MyUrlRewriter());
        mRequestQueue = Volley.newRequestQueue(context, myHurl);

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        int cacheSize = 1024 * 1024 * memClass / 8;

        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
}
