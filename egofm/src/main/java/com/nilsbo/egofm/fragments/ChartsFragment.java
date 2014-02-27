package com.nilsbo.egofm.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.Interfaces.SongListListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.ChartsAdapter;
import com.nilsbo.egofm.networking.ChartsRequest;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.util.ChartItem;
import com.nilsbo.egofm.util.FragmentUtils;

import java.util.ArrayList;


public class ChartsFragment extends ListFragment implements Response.ErrorListener, Response.Listener<ArrayList<ChartItem>> {
    private static final String TAG = "com.nilsbo.egofm.fragments.ChartsFragment";

    private static final String CHARTS_REQUEST = "CHARTS_REQUEST";
    private static final String SAVED_STATE_CHARTS_ARRAY = "SAVED_STATE_CHARTS_ARRAY";
    private static final String SAVED_STATE_LIST_STATE = "listState";

    private ChartsAdapter adapter;
    private ArrayList<ChartItem> songs = new ArrayList<ChartItem>();
    final RequestQueue requestQueue = MyVolley.getRequestQueue();

    private ProgressBar emptyProgress;
    private TextView emptyText;
    private View parentView;
    private State mState;
    private Button emptyBtn;
    private SongListListener mCallback;

    private enum State {
        Loading,
        Error,
        Empty,
        ShowingResults
    }

    public static ChartsFragment newInstance() {
        ChartsFragment fragment = new ChartsFragment();
        return fragment;
    }

    public ChartsFragment() {
        // Required empty public constructor
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = FragmentUtils.getParent(this, SongListListener.class);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ChartsAdapter(songs, getActivity(), mCallback);
        setListAdapter(adapter);

        parentView = getView();
        emptyProgress = (ProgressBar) parentView.findViewById(R.id.empty_charts_progress);
        emptyText = (TextView) parentView.findViewById(R.id.empty_charts_text);
        emptyBtn = (Button) parentView.findViewById(R.id.empty_charts_reload);

        emptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCharts();
            }
        });

        mState = State.Empty;
        if (savedInstanceState != null) {
            mState = (State) savedInstanceState.getSerializable(SAVED_STATE_LIST_STATE);
            songs = savedInstanceState.getParcelableArrayList(SAVED_STATE_CHARTS_ARRAY);
        }
        if (mState == State.Empty || mState == State.Loading) {
            loadCharts();
        } else if (mState == State.Error) {
            showError();
        } else if (mState == State.ShowingResults) {
            adapter.setItems(songs);
        }
    }

    private void loadCharts() {
        Log.d(TAG, "Loading egoFM 42");

        emptyProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        emptyBtn.setVisibility(View.GONE);

        requestQueue.cancelAll(CHARTS_REQUEST);
        ChartsRequest playlistRequest = new ChartsRequest(getUrl(), this, this);
        playlistRequest.setTag(CHARTS_REQUEST);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);

        mState = State.Loading;
        adapter.setItems(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onResponse(ArrayList<ChartItem> response) {
        if (response.size() == 0) {
            showError();
        } else {
            mState = State.ShowingResults;
            songs = response;
            adapter.setItems(response);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());
        showError();
    }

    private void showError() {
        adapter.setItems(null);
        mState = State.Error;
        emptyProgress.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyBtn.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.list_connection_error));
    }


    private String getUrl() {
        return "http://www.egofm.de/musik/egofm-42?tmpl=app";
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        requestQueue.cancelAll(CHARTS_REQUEST);
        if (mState == State.Loading) mState = State.Empty;

        outState.putParcelableArrayList(SAVED_STATE_CHARTS_ARRAY, songs);
        outState.putSerializable(SAVED_STATE_LIST_STATE, mState);
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(CHARTS_REQUEST);
    }
}
