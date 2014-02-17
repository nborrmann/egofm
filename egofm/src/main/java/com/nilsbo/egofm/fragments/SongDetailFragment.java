package com.nilsbo.egofm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.util.IntentView;

/**
 * Created by Nils on 15.02.14.
 */
public class SongDetailFragment extends Fragment {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFragment";

    private static final String SAVED_STATE_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";
    private static final String SAVED_STATE_SONG_TITLE = "com.nilsb.egofm.SAVED_STATE_SONG_TITLE";
    public static final String ARG_SONG_TITLE = "com.nilsb.egofm.ARGUMENT_SONG_TITLE";
    public static final String ARG_SONG_ARTIST = "com.nilsb.egofm.SAVED_STATE_SONG_ARTIST";

    private Context mContext;
    private View rootView;

    private String mTitle;
    private String mArtist;

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

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(SAVED_STATE_SONG_TITLE);
            mArtist = savedInstanceState.getString(SAVED_STATE_SONG_TITLE);
        } else {
            if (getArguments() != null && getArguments().containsKey(ARG_SONG_TITLE)) {
                mTitle = getArguments().getString(ARG_SONG_TITLE);
                mArtist = getArguments().getString(ARG_SONG_ARTIST);
            }
        }

        IntentView intentView = (IntentView) rootView.findViewById(R.id.song_details_intent_container);
        intentView.setQuery(String.format("%s - %s", mArtist, mTitle));
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_SONG_TITLE, mTitle);
        outState.putString(SAVED_STATE_SONG_ARTIST, mArtist);
    }

}
