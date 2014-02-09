package com.nilsbo.egofm.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.adapters.NewsAdapter;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsListRequest;
import com.nilsbo.egofm.util.NewsItem;

import java.util.ArrayList;


public class NewsFragment extends Fragment implements AbsListView.OnScrollListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsFragment";

    private static final String NEWS_LIST_REQUEST = "NEWS_LIST_REQUEST";
    private static final String SAVED_STATE_PAGE = "savedStatePage";
    private static final String SAVED_STATE_NEWS_ARRAY = "savedstatenews";
    private static final String SAVED_STATE_LIST_STATE = "listState";

    private static final String url_pattern = "http://egofm.de.ps-server.net/app-news?tmpl=app&start=%d";

    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private ArrayList<NewsItem> news = new ArrayList<NewsItem>();

    private State mState;
    private boolean isLoading;  // used for toggling loading in onScroll
    private boolean isError;    // used for toggling loading in onScroll
    private int page = 0;

    private View parentView;
    private NewsAdapter adapter;
    private PullToRefreshGridView gridView;
    private ProgressBar emptyProgress;
    private TextView emptyText;

    private enum State {
        Error, // An error message is currently displayed.
        Empty, // Whenever there are no news shown. This also applies when we are currently loading.
        ShowingResults // Any number of news are displayed.
    }

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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new NewsAdapter(news, getActivity());
        parentView = getView();

        gridView = (PullToRefreshGridView) getView().findViewById(R.id.newslist);
        LinearLayout emptyView = (LinearLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_news_empty, null, false);
        emptyProgress = (ProgressBar) emptyView.findViewById(R.id.empty_news_progress);
        emptyText = (TextView) emptyView.findViewById(R.id.empty_news_text);

        mState = State.Empty;
        if (savedInstanceState != null) {
            news = savedInstanceState.getParcelableArrayList(SAVED_STATE_NEWS_ARRAY);
            page = savedInstanceState.getInt(SAVED_STATE_PAGE);
            mState = (State) savedInstanceState.getSerializable(SAVED_STATE_LIST_STATE);
        }

        switch (mState) {
            case Empty:
                loadNews(0, new LoadListener());
                break;
            case Error:
                showErrorMessage();
                break;
            case ShowingResults:
                adapter.setItems(news);
                adapter.notifyDataSetChanged();
                break;
        }

        gridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<GridView>() {
            @Override
            public void onRefresh(PullToRefreshBase<GridView> refreshView) {
                loadNews(0, new RefreshListener());
            }
        });

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        gridView.setEmptyView(emptyView);
    }

    private void loadNews(int page, VolleyListener listener) {
        Log.d(TAG, "Loading news page " + page);

        showProgressBar();
        isLoading = true;

        NewsListRequest playlistRequest = new NewsListRequest(String.format(url_pattern, page * 15), listener, listener);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(20000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        playlistRequest.setTag(NEWS_LIST_REQUEST);
        requestQueue.add(playlistRequest);
    }

    private void showProgressBar() {
        emptyProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
    }

    private void showErrorMessage() {
        emptyProgress.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.list_connection_error));
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount - 3 && totalItemCount > 10
                && !isLoading && !isError) {
            loadNews(page, new LoadListener());
        }
        if (isError && firstVisibleItem + visibleItemCount < totalItemCount - 2 && totalItemCount > 10) {
            isError = false;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    private abstract class VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {
    }

    private class LoadListener extends VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());

            mState = State.Error;
            showErrorMessage();

            isLoading = false;
            isError = true;
        }
        @Override
        public void onResponse(ArrayList<NewsItem> response) {
            if (response.size() == 0) {
                mState = State.Error;
                showErrorMessage();
            } else {
                mState = State.ShowingResults;
                news.addAll(response);
                adapter.setItems(news);
                adapter.notifyDataSetChanged();
                page++;
            }
            isLoading = false;
            isError = false;
        }
    }

    private class RefreshListener extends VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());

            showErrorMessage();
            gridView.onRefreshComplete();

            isLoading = false;
            isError = true;
        }

        @Override
        public void onResponse(ArrayList<NewsItem> response) {
            if (news.size() == 0 || !response.get(0).equals(news.get(0))) {
                // don't merge the lists. This is too much of a hassle and will yield duplicate news when loading additional pages
                news.clear();
                news = response;
                adapter.setItems(response);
                adapter.notifyDataSetChanged();
                page = 1;
            }
            gridView.onRefreshComplete();

            isLoading = false;
            isError = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        requestQueue.cancelAll(NEWS_LIST_REQUEST);

        outState.putParcelableArrayList(SAVED_STATE_NEWS_ARRAY, news);
        outState.putInt(SAVED_STATE_PAGE, page);
        outState.putSerializable(SAVED_STATE_LIST_STATE, mState);
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(NEWS_LIST_REQUEST);
    }
}
