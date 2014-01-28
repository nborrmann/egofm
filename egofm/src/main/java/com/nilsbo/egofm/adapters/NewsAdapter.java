package com.nilsbo.egofm.adapters;

import android.content.Context;
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

import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class NewsAdapter extends BaseAdapter {
    private static final String TAG = "com.nilsbo.egofm.adapters.NewsAdapter";

    private ArrayList<NewsItem> newsItems = new ArrayList<NewsItem>();
    private final LayoutInflater mflater;
    final ImageLoader imageLoader = MyVolley.getImageLoader();

    public NewsAdapter(ArrayList<NewsItem> newsItems, Context context) {
        this.newsItems = newsItems;
        mflater = LayoutInflater.from(context);
    }

    public void addItems(ArrayList<NewsItem> items) {
        if (items == null) {
            newsItems = new ArrayList<NewsItem>();
        } else {
            newsItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<NewsItem> items) {
        if (items != null) {
            newsItems = items;
        } else {
            newsItems = new ArrayList<NewsItem>();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        NewsItem newsItem = newsItems.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mflater.inflate(R.layout.listitem_news, null, false);

            holder.title = (TextView) convertView.findViewById(R.id.newslistitem_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.newslistitem_subtitle);
            holder.imageView = (NetworkImageView) convertView.findViewById(R.id.newslistitem_image);

            holder.imageView.setDefaultImageResId(R.drawable.default_news_image);
            holder.imageView.setErrorImageResId(R.drawable.default_news_image);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(newsItem.title);
        holder.subtitle.setText(newsItem.subtitle);
        holder.imageView.setImageUrl("http://egofm.de" + newsItem.imgUrl, imageLoader);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public int getCount() {
        return newsItems.size();
    }

    @Override
    public Object getItem(int position) {
        return newsItems.get(position);
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
