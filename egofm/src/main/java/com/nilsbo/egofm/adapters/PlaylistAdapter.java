package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nilsbo.egofm.R;
import com.nilsbo.egofm.util.PlaylistItem;

import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class PlaylistAdapter extends BaseAdapter {
    private static final String TAG = "com.nilsbo.egofm.adapters.PlaylistAdapter";

    private ArrayList<PlaylistItem> songs = new ArrayList<PlaylistItem>();
    private final LayoutInflater mflater;

    public PlaylistAdapter(ArrayList<PlaylistItem> songs, Context context) {
        this.songs = songs;
        mflater = LayoutInflater.from(context);
    }

    public void setItems(ArrayList<PlaylistItem> items) {
        if (items != null) {
            songs = items;
        } else {
            songs = new ArrayList<PlaylistItem>();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        PlaylistItem song = songs.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mflater.inflate(R.layout.listitem_playlist, null, false);

            holder.title = (TextView) convertView.findViewById(R.id.playlist_title);
            holder.artist = (TextView) convertView.findViewById(R.id.playlist_artist);
            holder.time = (TextView) convertView.findViewById(R.id.playlist_time);
            holder.date = (TextView) convertView.findViewById(R.id.playlist_date);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(song.artist);
        holder.artist.setText(song.title);
        holder.time.setText(song.time);
        holder.date.setText(song.date);

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
        public TextView artist;
        public TextView time;
        public TextView date;

    }
}
