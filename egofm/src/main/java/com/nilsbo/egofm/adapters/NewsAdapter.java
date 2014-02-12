package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.nilsbo.egofm.Interfaces.NewsListListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.networking.MyVolley;
import com.nilsbo.egofm.util.NewsItem;
import com.nilsbo.egofm.widgets.ResizableNetworkImageView;

import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class NewsAdapter extends BaseAdapter implements View.OnTouchListener, View.OnClickListener {
    private static final String TAG = "com.nilsbo.egofm.adapters.NewsAdapter";

    private ArrayList<NewsItem> newsItems = new ArrayList<NewsItem>();
    private final Context context;
    private final NewsListListener mCallback;
    private final LayoutInflater mflater;
    final ImageLoader imageLoader = MyVolley.getImageLoader();

    public NewsAdapter(ArrayList<NewsItem> newsItems, Context context, NewsListListener mCallback) {
        this.newsItems = newsItems;
        this.context = context;
        this.mCallback = mCallback;
        mflater = LayoutInflater.from(context);
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
            holder.imageView = (ResizableNetworkImageView) convertView.findViewById(R.id.newslistitem_image);

            holder.imageView.setDefaultImageResId(R.drawable.default_news_image);
            holder.imageView.setErrorImageResId(R.drawable.default_news_image);
            convertView.setOnTouchListener(this);
            convertView.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(newsItem.title);
        holder.subtitle.setText(newsItem.subtitle);
        holder.imageView.setImageUrl(newsItem.imgUrl, imageLoader);
        if (holder.imageView.getDrawable() != null)
            holder.imageView.getDrawable().clearColorFilter();
        holder.position = position;

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

    public void setItems(ArrayList<NewsItem> news) {
        newsItems = news;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.newslistitem_container:
                return onTouchImageView(v, event);
        }
        return false;
    }

    private boolean onTouchImageView(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setItemPressedOverlay(v);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isInsideViewBounds(v, event)) {
                    clearItemPressedOverlay(v);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isInsideViewBounds(v, event)) {
                    v.performClick();
                }
            case MotionEvent.ACTION_CANCEL: {
                clearItemPressedOverlay(v);
                return true;
            }

        }
        return false;
    }

    private void setItemPressedOverlay(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        int color = context.getResources().getColor(R.color.egofm_green_transparent);
        holder.imageView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        holder.imageView.invalidate();
        holder.title.setTextColor(context.getResources().getColor(R.color.white));
    }

    private void clearItemPressedOverlay(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.imageView.getDrawable().clearColorFilter();
        holder.imageView.invalidate();
        holder.title.setTextColor(context.getResources().getColor(R.color.egofm_green));
    }

    private boolean isInsideViewBounds(View v, MotionEvent event) {
        return event.getX() > 0 && event.getX() < v.getWidth() && event.getY() > 0
                && event.getY() < v.getHeight();
    }

    @Override
    public void onClick(View v) {
        int position = ((ViewHolder) v.getTag()).position;
        mCallback.onItemClicked(newsItems.get(position));

//        Intent intent = new Intent(context, NewsItemActivity.class);
//        intent.putExtra("news_header", newsItems.get(position));
//        context.startActivity(intent);
    }

    private class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ResizableNetworkImageView imageView;
        public int position;
    }
}
