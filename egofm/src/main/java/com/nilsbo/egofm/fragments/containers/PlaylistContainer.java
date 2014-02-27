package com.nilsbo.egofm.fragments.containers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilsbo.egofm.Interfaces.SongListListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.activities.SongDetailActivity;
import com.nilsbo.egofm.fragments.PlaylistFragment;
import com.nilsbo.egofm.fragments.SongDetailFragment;


public class PlaylistContainer extends Fragment implements SongListListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.containers.PlaylistContainer";

    private static final String SAVED_STATE_TWO_PANE = "savedStateTwoPane";
    private static final String SAVED_STATE_HIDDEN = "savedStateHidden";

    private SongDetailFragment songDetailFragment;
    private PlaylistFragment playlistFragment;
    private FragmentManager childFragmentManager;
    private ViewGroup rootView;

    private boolean isTwoPane;
    private boolean isHidden;

    public static PlaylistContainer newInstance() {
        PlaylistContainer fragment = new PlaylistContainer();
        return fragment;
    }

    public PlaylistContainer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        childFragmentManager = getChildFragmentManager();

        if (savedInstanceState == null) {
            playlistFragment = new PlaylistFragment();
            childFragmentManager.beginTransaction().add(R.id.song_list_container, playlistFragment, "bla").commit();

            if (rootView.findViewById(R.id.song_detail_container) != null) {
                isTwoPane = true;

                songDetailFragment = new SongDetailFragment();
                childFragmentManager.beginTransaction().add(R.id.song_detail_container, songDetailFragment).hide(songDetailFragment).commit();
                isHidden = true;
            }
        } else {
            isTwoPane = savedInstanceState.getBoolean(SAVED_STATE_TWO_PANE);
            isHidden = savedInstanceState.getBoolean(SAVED_STATE_HIDDEN);
            // this is necessary because the Fragment gets instantiated with empty constructor, if
            // it is recreated after onSaveInstanceState
            playlistFragment = (PlaylistFragment) childFragmentManager.findFragmentById(R.id.song_list_container);
            songDetailFragment = (SongDetailFragment) childFragmentManager.findFragmentById(R.id.song_detail_container);

            if (isHidden && isTwoPane)
                childFragmentManager.beginTransaction().hide(songDetailFragment).commit();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = container;
        return inflater.inflate(R.layout.container_fragment_song, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVED_STATE_TWO_PANE, isTwoPane);
        outState.putBoolean(SAVED_STATE_HIDDEN, isHidden);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSongClicked(String artist, String title) {
        if (isTwoPane) {
            if (songDetailFragment.isHidden()) {
                childFragmentManager.beginTransaction().show(songDetailFragment).commit();
                isHidden = false;
            }
            songDetailFragment.setContent(artist, title);
        } else {
            Intent intent = new Intent(getActivity(), SongDetailActivity.class);
            intent.putExtra(SongDetailFragment.ARG_SONG_TITLE, title);
            intent.putExtra(SongDetailFragment.ARG_SONG_ARTIST, artist);
            getActivity().startActivity(intent);
        }
    }
}
