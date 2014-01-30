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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.activities.NewsItemActivity;
import com.nilsbo.egofm.adapters.NewsAdapter;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.networking.NewsListRequest;
import com.nilsbo.egofm.util.NewsItem;

import java.util.ArrayList;


public class NewsFragment extends Fragment implements Response.ErrorListener, Response.Listener<ArrayList<NewsItem>>, AbsListView.OnScrollListener {
    private static final String TAG = "com.nilsbo.egofm.fragments.NewsFragment";
    private static final String SAVED_STATE_NEWS_ARRAY = "savedstatenews";

    final RequestQueue requestQueue = MyVolley.getRequestQueue();
    private ArrayList<NewsItem> news = new ArrayList<NewsItem>();
    private View parentView;
    private NewsAdapter adapter;
    private String url_pattern = "http://egofm.de.ps-server.net/app-news?tmpl=app&start=%d";
    private int page = 0;
    private boolean isLoading;

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

    private void loadNews() {
        Log.d(TAG, "loading page " + page);
        isLoading = true;
        NewsListRequest playlistRequest = new NewsListRequest(getUrl(), this, this);
        playlistRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(playlistRequest);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new NewsAdapter(news, getActivity());

        if (savedInstanceState != null) {
            news = savedInstanceState.getParcelableArrayList(SAVED_STATE_NEWS_ARRAY);
            adapter.setItems(news);
        } else {
            loadNews();
        }

        GridView view = (GridView) getView().findViewById(R.id.newslist);
        LinearLayout emptyView = (LinearLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_news_empty, null, false);

        view.setAdapter(adapter);
        view.setOnScrollListener(this);
        view.setEmptyView(emptyView);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onClick");
                Intent intent = new Intent(getActivity(), NewsItemActivity.class);
                intent.putExtra("url", news.get(position).link);
                startActivity(intent);

            }
        });

    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount - 3 && totalItemCount > 10 && !isLoading) {
            page++;
            loadNews();
        }
    }

    private String getUrl() {
        return String.format(url_pattern, page * 15);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_STATE_NEWS_ARRAY, news);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse " + error.getMessage());
        isLoading = false;
    }

    @Override
    public void onResponse(ArrayList<NewsItem> response) {
        Log.d(TAG, "onResponse");
        adapter.addItems(response);
        isLoading = false;
    }
}
