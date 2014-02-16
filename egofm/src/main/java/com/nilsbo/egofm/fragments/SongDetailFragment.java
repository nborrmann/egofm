package com.nilsbo.egofm.fragments;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.util.IntentView;

/**
 * Created by Nils on 15.02.14.
 */
public class SongDetailFragment extends Fragment {
    private static final String TAG = "com.nilsbo.egofm.fragments.SongDetailFragment";

    private Context mContext;
    private PackageManager mPackageManager;
    private View rootView;

    private String query = "Chromeo - Fancy Footwork";
    private LayoutInflater mInflater;


    public SongDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_song_detail, container, false);
        mContext = getActivity();
        mPackageManager = mContext.getPackageManager();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button debugBtn = (Button) rootView.findViewById(R.id.song_details_debug);
        Button debugBtn2 = (Button) rootView.findViewById(R.id.song_details_debug2);
        Button debugBtn3 = (Button) rootView.findViewById(R.id.song_details_debug3);

        IntentView intentView = (IntentView) rootView.findViewById(R.id.song_details_intent_container);
        intentView.setQuery("Torpedo Boyz - Are you talking to me");

        debugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        debugBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        debugBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
