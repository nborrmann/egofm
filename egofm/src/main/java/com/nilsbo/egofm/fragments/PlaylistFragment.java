package com.nilsbo.egofm.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.nilsbo.egofm.Interfaces.SongListListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.PlaylistAdapter;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.PlaylistRequest;
import com.nilsbo.egofm.util.FragmentUtils;
import com.nilsbo.egofm.util.PlaylistItem;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;


public class PlaylistFragment extends ListFragment implements Response.ErrorListener, Response.Listener<ArrayList<PlaylistItem>>, View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemClickListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.PlaylistFragment";

    private static final String PLAYLIST_REQUEST = "PLAYLIST_REQUEST";
    private static final String SAVED_STATE_PLAYLIST_ARRAY = "SAVED_STATE_PLAYLIST_ARRAY";
    private static final String SAVED_STATE_TIME = "SAVED_STATE_TIME";
    private static final String SAVED_STATE_LIST_STATE = "savedStateListState";

    private State mState;
    private PlaylistAdapter adapter;
    private ArrayList<PlaylistItem> songs = new ArrayList<PlaylistItem>();
    final RequestQueue requestQueue = MyVolley.getRequestQueue();

    private int year;
    private int month;
    private int day;
    private int minute;
    private int hour;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Button dateBtn;
    private Calendar calendar;
    private Button timeBtn;
    private String time;
    private String date;
    private Button nowBtn;
    private ProgressBar emptyProgress;
    private TextView emptyText;
    private View parentView;
    private SongListListener mCallback;

    private enum State {
        Loading,
        Error,
        Empty,
        NoResults, // loading was successful, but no songs were found.
        ShowingResults
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        return fragment;
    }

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = FragmentUtils.getParent(this, SongListListener.class);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new PlaylistAdapter(songs, getActivity());
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        calendar = Calendar.getInstance();
        parentView = getView();

        dateBtn = (Button) parentView.findViewById(R.id.btn_date);
        dateBtn.setOnClickListener(this);
        timeBtn = (Button) parentView.findViewById(R.id.btn_time);
        timeBtn.setOnClickListener(this);
        nowBtn = (Button) parentView.findViewById(R.id.btn_now);
        nowBtn.setOnClickListener(this);
        emptyProgress = (ProgressBar) parentView.findViewById(R.id.empty_playlist_progress);
        emptyText = (TextView) parentView.findViewById(R.id.empty_playlist_text);

        mState = State.Empty;
        if (savedInstanceState != null) {
            songs = savedInstanceState.getParcelableArrayList(SAVED_STATE_PLAYLIST_ARRAY);
            mState = (State) savedInstanceState.getSerializable(SAVED_STATE_LIST_STATE);

            int[] savedTime = savedInstanceState.getIntArray(SAVED_STATE_TIME);
            year = savedTime[0];
            month = savedTime[1];
            day = savedTime[2];
            hour = savedTime[3];
            minute = savedTime[4];
        } else {
            initDateTimeNow();
        }
        setBtnText();

        switch (mState) {
            case Error:
                showErrorMessage();
                break;
            case Loading:
            case Empty:
                reload();
                break;
            case NoResults:
                showEmptyMessage();
                break;
            case ShowingResults:
                adapter.setItems(songs);
                break;
        }
    }

    private void showEmptyMessage() {
        adapter.setItems(null); // just to be sure.
        emptyProgress.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.playlist_no_results));
    }

    private void showErrorMessage() {
        adapter.setItems(null); // just to be sure.
        emptyProgress.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.list_connection_error));
    }

    private void reload() {
        Log.d(TAG, "Loading Playlist");
        mState = State.Loading;

        emptyProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        requestQueue.cancelAll(PLAYLIST_REQUEST);
        PlaylistRequest playlistRequest = new PlaylistRequest(getUrl(), this, this);
        playlistRequest.setTag(PLAYLIST_REQUEST);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);

        adapter.setItems(null);
    }

    private void initDateTimeNow() {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY) - 1;
        minute = calendar.get(Calendar.MINUTE);

        if (hour < 0) {
            hour = 0;
            minute = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onResponse(ArrayList<PlaylistItem> response) {
        if (response.size() == 0) {
            mState = State.NoResults;
            showEmptyMessage();
        } else {
            mState = State.ShowingResults;
            songs = response;
            adapter.setItems(response);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());
        mState = State.Error;
        showErrorMessage();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_date) {
            datePickerDialog = DatePickerDialog.newInstance(this, year, month, day, false);
            datePickerDialog.setYearRange(calendar.get(Calendar.YEAR) - 1, calendar.get(Calendar.YEAR));
            datePickerDialog.show(getFragmentManager(), "datepicker");
        } else if (v.getId() == R.id.btn_time) {
            timePickerDialog = TimePickerDialog.newInstance(this, hour, minute, true, false);
            timePickerDialog.show(getFragmentManager(), "timepicker");
        } else if (v.getId() == R.id.btn_now) {
            initDateTimeNow();
            setBtnText();
            reload();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
        final String title = songs.get(position).title;
        final String artist = songs.get(position).artist;

        //TODO use callback to parent fragment
//        Intent intent = new Intent(getActivity(), SongDetailActivity.class);
//        intent.putExtra(SongDetailFragment.ARG_SONG_TITLE, title);
//        intent.putExtra(SongDetailFragment.ARG_SONG_ARTIST, artist);
//        getActivity().startActivity(intent);
        mCallback.onSongClicked(artist, title);

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        setBtnText();
        reload();
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        setBtnText();
        reload();
    }

    private void setBtnText() {
        date = String.format("%02d.%02d.%02d", day, month + 1, year);
        dateBtn.setText(date);
        time = String.format("%02d:%02d", hour, minute);
        timeBtn.setText(time);
    }

    private String getUrl() {
        setBtnText();
        return String.format("http://www.egofm.de/musik/play-history/?tmpl=app&start_date=%s&start_time=%s", date, time);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        requestQueue.cancelAll(PLAYLIST_REQUEST);
        if (mState == State.Loading) mState = State.Empty;

        outState.putParcelableArrayList(SAVED_STATE_PLAYLIST_ARRAY, songs);
        outState.putIntArray(SAVED_STATE_TIME, new int[]{year, month, day, hour, minute});
        outState.putSerializable(SAVED_STATE_LIST_STATE, mState);
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(PLAYLIST_REQUEST);
    }
}
