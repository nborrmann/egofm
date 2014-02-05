package com.nilsbo.egofm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.nilsbo.egofm.activities.NewsItemActivity;
import com.nilsbo.egofm.adapters.NewsAdapter;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsListRequest;
import com.nilsbo.egofm.util.NewsItem;

import java.util.ArrayList;


public class NewsFragment extends Fragment implements AbsListView.OnScrollListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsFragment";

    private static final String SAVED_STATE_PAGE = "savedStatePage";
    private static final String SAVED_STATE_NEWS_ARRAY = "savedstatenews";

    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private ArrayList<NewsItem> news = new ArrayList<NewsItem>();
    private View parentView;
    private NewsAdapter adapter;
    private String url_pattern = "http://egofm.de.ps-server.net/app-news?tmpl=app&start=%d";
    private int page = 0;
    private boolean isLoading;
    private PullToRefreshGridView gridView;
    private ProgressBar emptyProgress;
    private TextView emptyText;
    private boolean isError = false;

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

    private void loadNews(int page, VolleyListener listener) {
        setLoadingStatus(true);

        Log.d(TAG, "loading page " + page);
        NewsListRequest playlistRequest = new NewsListRequest(String.format(url_pattern, page * 15), listener, listener);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(20000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);
    }

    private class LoadListener extends VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());
            emptyText.setText(getResources().getString(R.string.list_connection_error));
            setLoadingStatus(false);
            isError = true;
        }

        @Override
        public void onResponse(ArrayList<NewsItem> response) {
            if (response.size() == 0) {
                emptyProgress.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText(getResources().getString(R.string.empty_news));
            } else {
                news.addAll(response);
                adapter.setItems(news);
                adapter.notifyDataSetChanged();
                page++;
            }
            setLoadingStatus(false);
        }
    }

    private class RefreshListener extends VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse " + error.getLocalizedMessage());
            emptyText.setText(getResources().getString(R.string.list_connection_error));
            gridView.onRefreshComplete();
            setLoadingStatus(false);
        }
        @Override
        public void onResponse(ArrayList<NewsItem> response) {
            if (news.size() == 0 || !response.get(0).equals(news.get(0))) {
                // don't merge the lists. This is too much of a hassle and will yield duplicate news when loading additional pages
                news = response;
                adapter.setItems(response);
                adapter.notifyDataSetChanged();
                page = 1;
            }
            gridView.onRefreshComplete();
            setLoadingStatus(false);
        }
    }

    private abstract class VolleyListener implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>> {
    }


    private void setLoadingStatus(boolean loading) {
        isLoading = loading;
        if (loading) {
            emptyProgress.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        } else {
            emptyProgress.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }
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

        if (savedInstanceState != null) {
            news = savedInstanceState.getParcelableArrayList(SAVED_STATE_NEWS_ARRAY);
            page = savedInstanceState.getInt(SAVED_STATE_PAGE);
            adapter.setItems(news);
            adapter.notifyDataSetChanged();
        } else {
            loadNews(0, new LoadListener());
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
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), NewsItemActivity.class);
                intent.putExtra("news_header", news.get(position));
                startActivity(intent);

            }
        });

    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount - 3 && totalItemCount > 10 && !isLoading && !isError) {
            loadNews(page, new LoadListener());
        }
        if (isError && firstVisibleItem + visibleItemCount < totalItemCount - 2 && totalItemCount > 10) {
            isError = false;
            Log.d(TAG, "isError false");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_STATE_NEWS_ARRAY, news);
        outState.putInt(SAVED_STATE_PAGE, page);
    }
}
