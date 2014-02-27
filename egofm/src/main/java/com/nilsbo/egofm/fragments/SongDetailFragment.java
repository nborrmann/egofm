package com.nilsbo.egofm.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import static com.nilsbo.egofm.util.FragmentUtils.logUIAction;

/**
 * Created by Nils on 15.02.14.
 */
public class SongDetailFragment extends Fragment implements Response.ErrorListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFragment";

    private static final String SAVED_STATE_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";
    private static final String SAVED_STATE_SONG_TITLE = "com.nilsb.egofm.SAVED_STATE_SONG_TITLE";
    public static final String ARG_SONG_TITLE = "com.nilsb.egofm.ARGUMENT_SONG_TITLE";
    public static final String ARG_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";

    private Context mContext;
    private RequestQueue mRequestQueue = MyVolley.getRequestQueue();

    protected View rootView;

    protected String mTitle;
    protected String mArtist;
    protected ResizableImageView artistImage;
    protected NetworkImageView albumImage;
    protected TextView titleText;
    protected TextView artistText;
    protected TextView albumTitleText;
    protected TextView albumSubtitleText;
    protected TextView artistDescText;
    protected RelativeLayout headerContainer;
    protected ObservableScrollView scrollView;
    protected TextView artistDescLabel;
    protected LinearLayout albumContainer;
    protected TextView albumLabel;
    protected TextView tagsText;
    protected TextView tagsLabel;
    protected View actionbarBg;
    protected LinearLayout titleContainer;
    private IntentView intentView;
    private ImageView lastfmBtn;


    public SongDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_song_detail_fancy, container, false);
        mContext = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initUI();

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(SAVED_STATE_SONG_TITLE);
            mArtist = savedInstanceState.getString(SAVED_STATE_SONG_ARTIST);
            setDefaultUI();
            loadLastfmData();
        } else {
            if (getArguments() != null && getArguments().containsKey(ARG_SONG_TITLE)) {
                mTitle = getArguments().getString(ARG_SONG_TITLE);
                mArtist = getArguments().getString(ARG_SONG_ARTIST);
                setDefaultUI();
                loadLastfmData();
            } else {
                setEmptyUI();
            }
        }
    }

    private void setEmptyUI() {
//        this.setV

    }

    private void loadLastfmData() {
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

    protected void initUI() {
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
        titleContainer = (LinearLayout) rootView.findViewById(R.id.song_details_title_container);
        intentView = (IntentView) rootView.findViewById(R.id.song_details_intent_container);
        lastfmBtn = (ImageView) rootView.findViewById(R.id.song_details_lastfm_btn);

        lastfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(String.format(mContext.getString(R.string.lastfm_btn_url), mArtist)));
                startActivity(i);

                logUIAction(getActivity(), "Open Last.fm", mArtist + " - " + mTitle);
            }
        });
    }

    private void setDefaultUI() {
        titleText.setText(mTitle);
        artistText.setText(mArtist);
        artistImage.setImageResource(R.drawable.artist_placeholder);
        artistDescLabel.setText(String.format(mContext.getString(R.string.song_details_about_label), mArtist));
        albumContainer.setVisibility(View.GONE);
        albumLabel.setVisibility(View.GONE);
        tagsLabel.setVisibility(View.GONE);
        tagsText.setVisibility(View.GONE);
        artistDescText.setText(mContext.getString(R.string.song_details_loading));
        intentView.setQuery(String.format("%s - %s", mArtist, mTitle));

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

    public void setContent(String artist, String title) {
        mTitle = title;
        mArtist = artist;
        setDefaultUI();
        loadLastfmData();
    }

    private class VolleyArtistRequestListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONArray images = response.getJSONObject("artist").getJSONArray("image");
                int maxIndex = images.length() - 1;
                String imgUrl = images.getJSONObject(maxIndex).getString("#text");
                if (imgUrl.contains("Keep+stats+clean") || imgUrl.contains("Wrong+Tag")) {
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
                            setArtistImage(response);
                        }
                    }
                });
    }

    protected void setArtistImage(ImageLoader.ImageContainer response) {
        artistImage.setImageBitmap(response.getBitmap());
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
