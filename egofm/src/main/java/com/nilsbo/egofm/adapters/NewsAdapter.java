package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.util.NewsItem;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class NewsAdapter extends BaseAdapter {
    private static final String TAG = "com.nilsbo.egofm.adapters.PlaylistAdapter";

    private ArrayList<NewsItem> songs = new ArrayList<NewsItem>();
    private final LayoutInflater mflater;
    final ImageLoader imageLoader = MyVolley.getImageLoader();

    public NewsAdapter(ArrayList<NewsItem> songs, Context context) {
        this.songs = songs;
        mflater = LayoutInflater.from(context);
    }

    public void setItems(ArrayList<NewsItem> items) {
        if (items != null) {
            songs = items;
        } else {
            songs = new ArrayList<NewsItem>();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        NewsItem newsItem = songs.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mflater.inflate(R.layout.listitem_news, null, false);

            holder.title = (TextView) convertView.findViewById(R.id.newslistitem_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.newslistitem_subtitle);
            holder.imageView = (NetworkImageView) convertView.findViewById(R.id.newslistitem_image);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Log.d(TAG, newsItem.imgUrl);
        holder.title.setText(newsItem.title);
        holder.subtitle.setText(newsItem.subtitle);
        holder.imageView.setImageUrl("http://egofm.de" + newsItem.imgUrl, imageLoader);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public TextView time;
        public TextView date;
        public NetworkImageView imageView;
    }
}
