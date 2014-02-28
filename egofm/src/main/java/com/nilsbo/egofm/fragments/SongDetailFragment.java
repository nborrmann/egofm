package com.nilsbo.egofm.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nilsbo.egofm.Interfaces.LastFmArtistResponseListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.LastFmArtistRequest;
import com.nilsbo.egofm.networking.LastFmSongRequest;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.util.IntentView;
import com.nilsbo.egofm.widgets.ObservableScrollView;
import com.nilsbo.egofm.widgets.ResizableImageView;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;

import static com.nilsbo.egofm.util.FragmentUtils.logUIAction;

/**
 * Created by Nils on 15.02.14.
 */
public class SongDetailFragment extends Fragment implements Response.ErrorListener, LastFmArtistResponseListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFragment";

    private static final String SAVED_STATE_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";
    private static final String SAVED_STATE_SONG_TITLE = "com.nilsb.egofm.SAVED_STATE_SONG_TITLE";
    public static final String ARG_SONG_TITLE = "com.nilsb.egofm.ARGUMENT_SONG_TITLE";
    public static final String ARG_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";

    private Context mContext;

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
            }
        }
    }

    private void loadLastfmData() {
        new LastFmSongRequest(mArtist, mTitle, this, null);

        new LastFmArtistRequest(mArtist, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "onErrorResponse: artistRequest");
                artistDescText.setText(mContext.getString(R.string.list_connection_error));
            }
        });
    }

    @Override
    public void onTitleResponse(int duration, int trackAlbumPosition, String albumImageUrl, String albumTitle, ArrayList<String> trackTags) {
        if (albumTitle != null && albumImageUrl != null) {
            albumContainer.setVisibility(View.VISIBLE);
            albumLabel.setVisibility(View.VISIBLE);
            albumImage.setImageUrl(albumImageUrl, MyVolley.getImageLoader());
            albumTitleText.setText(albumTitle);

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
        }

        if (trackTags != null && trackTags.size() > 0) {
            tagsText.setText(StringUtil.join(trackTags, "   "));
            tagsLabel.setVisibility(View.VISIBLE);
            tagsText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onArtistResponse(String artistDescription, String imageUrl) {
        if (artistDescription != null)
            artistDescText.setText(Html.fromHtml(artistDescription));
        else
            artistDescText.setText(mContext.getString(R.string.artist_description_not_found));

        if (imageUrl != null) loadArtistImage(imageUrl);
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

    // This is overwritten in SongDetailFancyFragment!
    protected void setArtistImage(ImageLoader.ImageContainer response) {
        artistImage.setImageBitmap(response.getBitmap());
    }
}
