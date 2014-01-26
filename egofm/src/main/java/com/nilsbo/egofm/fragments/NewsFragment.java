package com.nilsbo.egofm.fragments;

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
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.NewsAdapter;
import com.nilsbo.egofm.adapters.PlaylistAdapter;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsListRequest;
import com.nilsbo.egofm.networking.PlaylistRequest;
import com.nilsbo.egofm.util.NewsItem;
import com.nilsbo.egofm.util.PlaylistItem;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;


public class NewsFragment extends ListFragment implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsFragment";

    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private ArrayList<NewsItem> news = new ArrayList<NewsItem>();
    private View parentView;
    private NewsAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        return fragment;
    }

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = "http://egofm.de.ps-server.net/app-news?tmpl=app";
        NewsListRequest playlistRequest = new NewsListRequest(url, this, this);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);

//        adapter.setItems(null);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new NewsAdapter(news, getActivity());
        setListAdapter(adapter);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(SAVED_STATE_PLAYLIST_ARRAY, songs);
//        outState.putIntArray(SAVED_STATE_TIME, new int[]{year, month, day, hour, minute});
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse");
        Log.d(TAG, "onErrorResponse "+error.getMessage());
    }

    @Override
    public void onResponse(ArrayList<NewsItem> response) {
        Log.d(TAG, "onResponse");
        adapter.setItems(response);
    }
}
