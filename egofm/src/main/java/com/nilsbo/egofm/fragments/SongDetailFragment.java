package com.nilsbo.egofm.fragments;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.util.IntentView;
import com.nilsbo.egofm.widgets.ObservableScrollView;
import com.nilsbo.egofm.widgets.ResizableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;

import static com.nilsbo.egofm.util.FragmentUtils.clamp;
import static com.nilsbo.egofm.util.FragmentUtils.getActionBarHeight;

/**
 * Created by Nils on 15.02.14.
 */
public class SongDetailFragment extends Fragment implements Response.ErrorListener, ObservableScrollView.ScrollViewListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFragment";

    private static final String SAVED_STATE_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";
    private static final String SAVED_STATE_SONG_TITLE = "com.nilsb.egofm.SAVED_STATE_SONG_TITLE";
    public static final String ARG_SONG_TITLE = "com.nilsb.egofm.ARGUMENT_SONG_TITLE";
    public static final String ARG_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";

    private Context mContext;
    private RequestQueue mRequestQueue = MyVolley.getRequestQueue();

    private View rootView;

    private String mTitle;
    private String mArtist;
    private ResizableImageView artistImage;
    private NetworkImageView albumImage;
    private TextView titleText;
    private TextView artistText;
    private TextView albumTitleText;
    private TextView albumSubtitleText;
    private TextView artistDescText;
    private RelativeLayout headerContainer;
    private ObservableScrollView scrollView;
    private TextView artistDescLabel;
    private LinearLayout albumContainer;
    private TextView albumLabel;

    private int mWidth;
    private int mHeight;
    private int placeholderImageHeight;
    private TextView tagsText;
    private TextView tagsLabel;
    private View actionbarBg;
    private LinearLayout titleContainer;
    private int mCurrentHeaderHeight;
    private int mMinHeaderHeight;

    public SongDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_song_detail, container, false);
        mContext = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        artistImage = (ResizableImageView) rootView.findViewById(R.id.song_details_artist_image);
        albumImage = (NetworkImageView) rootView.findViewById(R.id.song_details_album_image);
        titleText = (TextView) rootView.findViewById(R.id.song_details_title);
        artistText = (TextView) rootView.findViewById(R.id.song_details_artist);
        albumTitleText = (TextView) rootView.findViewById(R.id.song_details_album_title);
        albumSubtitleText = (TextView) rootView.findViewById(R.id.song_details_album_subtitle);
        artistDescText = (TextView) rootView.findViewById(R.id.song_details_artist_desc);
        artistDescLabel = (TextView) rootView.findViewById(R.id.song_details_artist_label);
        albumContainer = (LinearLayout) rootView.findViewById(R.id.song_details_album_container);
        albumLabel = (TextView) rootView.findViewById(R.id.song_details_album_label);
        tagsText = (TextView) rootView.findViewById(R.id.song_details_tags);
        tagsLabel = (TextView) rootView.findViewById(R.id.song_details_tags_label);
        actionbarBg = rootView.findViewById(R.id.song_details_actionbar_bg);
        titleContainer = (LinearLayout) rootView.findViewById(R.id.song_details_title_container);

        scrollView = (ObservableScrollView) rootView.findViewById(R.id.song_details_scrollview);
        scrollView.setScrollViewListener(this);
        scrollView.setFocusable(false);

        headerContainer = (RelativeLayout) rootView.findViewById(R.id.song_details_header_container);

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(SAVED_STATE_SONG_TITLE);
            mArtist = savedInstanceState.getString(SAVED_STATE_SONG_ARTIST);
        } else {
            if (getArguments() != null && getArguments().containsKey(ARG_SONG_TITLE)) {
                mTitle = getArguments().getString(ARG_SONG_TITLE);
                mArtist = getArguments().getString(ARG_SONG_ARTIST);
            } else {
                // TODO sample debug data
                mTitle = "Fancy Footwork";
                mArtist = "Chromeo";
            }
        }

        titleText.setText(mTitle);
        artistText.setText(mArtist);
        artistImage.setImageResource(R.drawable.artist_placeholder);
        artistDescLabel.setText(String.format(mContext.getString(R.string.song_details_about_label), mArtist));
        albumContainer.setVisibility(View.GONE);
        albumLabel.setVisibility(View.GONE);
        tagsLabel.setVisibility(View.GONE);
        tagsText.setVisibility(View.GONE);
        artistDescText.setText(mContext.getString(R.string.song_details_loading));

        final ViewGroup.LayoutParams layoutParams = actionbarBg.getLayoutParams();
        layoutParams.height = getActionBarHeight(getActivity());
        actionbarBg.setLayoutParams(layoutParams);

        titleContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMinHeaderHeight = bottom - top + getActionBarHeight(getActivity());
            }
        });

        artistImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // if the manual measuring failed (for example in landscape mode), correct it by posting a runnable.
                if (mCurrentHeaderHeight != bottom - top) {
                    Log.d(TAG, "oldHeaderHeight " + mCurrentHeaderHeight + "; new height: " + (bottom - top));
                    mCurrentHeaderHeight = bottom - top;
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.setPadding(scrollView.getPaddingLeft(), mCurrentHeaderHeight, scrollView.getPaddingRight(), scrollView.getPaddingBottom());

                        }
                    });
                }
            }
        });

        IntentView intentView = (IntentView) rootView.findViewById(R.id.song_details_intent_container);
        intentView.setQuery(String.format("%s - %s", mArtist, mTitle));

        Drawable artistPlaceholder = getResources().getDrawable(R.drawable.artist_placeholder);
        mCurrentHeaderHeight = Math.min((int) (1.0f * mWidth / artistPlaceholder.getIntrinsicWidth() * artistPlaceholder.getIntrinsicHeight()), mHeight);
        scrollView.setPadding(scrollView.getPaddingLeft(), mCurrentHeaderHeight, scrollView.getPaddingRight(), scrollView.getPaddingBottom());

        String trackUrl = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=63e6e4b1a4db2dda7585d9e82b2f723e&artist=" + mArtist + "&track=" + mTitle + "&format=json";
        JsonObjectRequest trackRequest = new JsonObjectRequest(Request.Method.GET, trackUrl, null, new VolleyTrackRequestListener(), this);
        mRequestQueue.add(trackRequest);

        String artistUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&artist=" + mArtist + "&api_key=63e6e4b1a4db2dda7585d9e82b2f723e&format=json";
        JsonObjectRequest artistRequest = new JsonObjectRequest(Request.Method.GET, artistUrl, null, new VolleyArtistRequestListener(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "onErrorResponse: artistRequest");
                artistDescText.setText(mContext.getString(R.string.list_connection_error));
            }
        });
        mRequestQueue.add(artistRequest);

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

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_SONG_TITLE, mTitle);
        outState.putString(SAVED_STATE_SONG_ARTIST, mArtist);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.w(TAG, "onErrorResponse");
    }

    private class VolleyArtistRequestListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONArray images = response.getJSONObject("artist").getJSONArray("image");
                int maxIndex = images.length() - 1;
                String imgUrl = images.getJSONObject(maxIndex).getString("#text");
                if (imgUrl.contains("Keep+stats+clean")) {
                    throw new JSONException("keep stats clean image returned");
                }
                loadArtistImage(imgUrl);
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing image url from last.fm response", e);
            }

            try {
                String artistDescription = response.getJSONObject("artist").getJSONObject("bio").getString("summary");
                if (TextUtils.isEmpty(artistDescription))
                    throw new JSONException("empty description");
                artistDescText.setText(Html.fromHtml(Jsoup.parse(artistDescription).text()));
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing artist description from last.fm response", e);
                artistDescText.setText(mContext.getString(R.string.artist_description_not_found));
            }

        }
    }

    private void loadArtistImage(String imgUrl) {
        ImageLoader.ImageContainer newContainer = MyVolley.getImageLoader().get(imgUrl,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // just leave the default image.
                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null) {
                            artistImage.setImageBitmap(response.getBitmap());
                            int imageHeight = Math.min((int) (1.0f * mWidth / response.getBitmap().getWidth() * response.getBitmap().getHeight()), mHeight);

                            final int oldHeight = mCurrentHeaderHeight;
                            mCurrentHeaderHeight = imageHeight; // we need this updated for the scrolling interpolation to work properly
                            scrollView.setPadding(scrollView.getPaddingLeft(), mCurrentHeaderHeight, scrollView.getPaddingRight(), scrollView.getPaddingBottom());
                            scrollView.scrollBy(0, imageHeight - oldHeight);
                        }
                    }
                });
    }

    private class VolleyTrackRequestListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            int duration = 0;
            try {
                duration = response.getJSONObject("track").getInt("duration");
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing duration from last.fm response", e);
            }

            int trackAlbumPosition = 0;
            try {
                JSONObject album = response.getJSONObject("track").getJSONObject("album");
                String albumTitle = album.getString("title");
                JSONArray albumImages = album.getJSONArray("image");
                String albumImageUrl = albumImages.getJSONObject(Math.min(albumImages.length(), 2)).getString("#text");
                trackAlbumPosition = album.getJSONObject("@attr").getInt("position");

                albumContainer.setVisibility(View.VISIBLE);
                albumLabel.setVisibility(View.VISIBLE);
                albumImage.setImageUrl(albumImageUrl, MyVolley.getImageLoader());
                albumTitleText.setText(albumTitle);
                albumSubtitleText.setText("track #" + trackAlbumPosition);
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing album from last.fm response", e);
            }

            if (duration != 0 && trackAlbumPosition != 0) {
                albumSubtitleText.setText(String.format(
                        mContext.getString(R.string.song_details_album_subtitle_full),
                        trackAlbumPosition, duration / 60000, (duration % 60000) / 1000));
            } else if (duration != 0) {
                albumSubtitleText.setText(String.format(
                        mContext.getString(R.string.song_details_album_subtitle_duration),
                        duration / 60000, (duration % 60000) / 1000));
            } else if (trackAlbumPosition != 0) {
                albumSubtitleText.setText(String.format(
                        mContext.getString(R.string.song_details_album_subtitle_track),
                        trackAlbumPosition));
            }

            try {
                JSONArray trackTagsJSON = response.getJSONObject("track").getJSONObject("toptags").getJSONArray("tag");
                ArrayList<String> trackTags = new ArrayList<String>();

                for (int i = 0; i < trackTagsJSON.length(); i++) {
                    trackTags.add(trackTagsJSON.getJSONObject(i).getString("name"));
                }

                tagsText.setText(StringUtil.join(trackTags, "   "));
                tagsLabel.setVisibility(View.VISIBLE);
                tagsText.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing tags from last.fm response", e);
            }
        }
    }
}
