package com.nilsbo.egofm.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nilsbo.egofm.Interfaces.ChartVoteListener;
import com.nilsbo.egofm.R;
import com.nilsbo.egofm.activities.SongDetailActivity;
import com.nilsbo.egofm.fragments.SongDetailFragment;
import com.nilsbo.egofm.networking.ChartsVoteRequester;
import com.nilsbo.egofm.util.ChartItem;

import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class ChartsAdapter extends BaseAdapter implements View.OnClickListener, ChartVoteListener {
    private static final String TAG = "com.nilsbo.egofm.adapters.ChartsAdapter";

    private final Drawable iconFav;
    private final Drawable iconNotFav;

    private ArrayList<ChartItem> songs = new ArrayList<ChartItem>();
    private final Context context;
    private final LayoutInflater mflater;

    public ChartsAdapter(ArrayList<ChartItem> songs, Context context) {
        this.songs = songs;
        this.context = context;
        mflater = LayoutInflater.from(context);
        iconFav = context.getResources().getDrawable(R.drawable.icon_fav);
        iconNotFav = context.getResources().getDrawable(R.drawable.icon_unfav);
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

    public void setItems(ArrayList<ChartItem> items) {
        if (items != null) {
            songs = items;
        } else {
            songs = new ArrayList<ChartItem>();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChartItem song = songs.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mflater.inflate(R.layout.listitem_42, null, false);

            holder.position = (TextView) convertView.findViewById(R.id.charts_position);
            holder.artist = (TextView) convertView.findViewById(R.id.charts_artist);
            holder.title = (TextView) convertView.findViewById(R.id.charts_title);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.charts_vote_progress);
            holder.positionDelta = (ImageView) convertView.findViewById(R.id.charts_position_delta);
            holder.chartsContainer = (LinearLayout) convertView.findViewById(R.id.charts_title_container);
            holder.chartsContainer.setOnClickListener(this);

            holder.voteButton = (ImageButton) convertView.findViewById(R.id.charts_vote_button);
            holder.voteButton.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.position.setText(song.position);
        holder.artist.setText(song.artist);
        holder.title.setText(song.title);

        holder.voteButton.setTag(position);
        holder.chartsContainer.setTag(position);

        switch (song.positionDelta) {
            case Up:
                holder.position.setVisibility(View.VISIBLE);
                holder.positionDelta.setImageResource(R.drawable.icon_arrow_up);
                break;
            case Down:
                holder.position.setVisibility(View.VISIBLE);
                holder.positionDelta.setImageResource(R.drawable.icon_arrow_down);
                break;
            case Same:
                holder.position.setVisibility(View.VISIBLE);
                holder.positionDelta.setImageResource(R.drawable.icon_arrow_right);
                break;
            case New:
                holder.position.setVisibility(View.VISIBLE);
                holder.positionDelta.setImageResource(R.drawable.icon_arrow_plus);
                break;
            case NewNoPosition:
                holder.position.setVisibility(View.GONE);
                holder.positionDelta.setImageResource(R.drawable.icon_arrow_right_big);
                break;
            case Unknown:
                holder.position.setVisibility(View.VISIBLE);
                holder.positionDelta.setImageDrawable(null);
                break;
        }

        switch (song.votingState) {
            case Voted:
                holder.voteButton.setImageDrawable(iconFav);
                holder.voteButton.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                holder.voteButton.setClickable(false);
                break;
            case InProgress:
                holder.voteButton.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                break;
            case NotVoted:
                holder.voteButton.setClickable(true);
                holder.voteButton.setImageDrawable(iconNotFav);
                holder.voteButton.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
                break;
        }

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        int pos = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.charts_title_container:
                final String title = songs.get(pos).title;
                final String artist = songs.get(pos).artist;

                //TODO use callback to parent fragment
                Intent intent = new Intent(context, SongDetailActivity.class);
                intent.putExtra(SongDetailFragment.ARG_SONG_TITLE, title);
                intent.putExtra(SongDetailFragment.ARG_SONG_ARTIST, artist);
                context.startActivity(intent);

                break;

            case R.id.charts_vote_button:
                ChartItem song = songs.get(pos);

                song.votingState = ChartItem.State.InProgress;
                notifyDataSetChanged();
                new ChartsVoteRequester(song, this, pos);
                break;
        }
    }

    @Override
    public void onSuccessfulVote(int mTag) {
        songs.get(mTag).votingState = ChartItem.State.Voted;
        notifyDataSetChanged();
    }

    @Override
    public void onNetworkError(int mTag) {
        songs.get(mTag).votingState = ChartItem.State.NotVoted;
        Toast.makeText(context, context.getString(R.string.chart_vote_connection_error), Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    @Override
    public void onUnknownError(int mTag) {
        songs.get(mTag).votingState = ChartItem.State.NotVoted;
        Toast.makeText(context, context.getString(R.string.chart_vote_unknown_response), Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView position;
        public ImageButton voteButton;
        public ProgressBar progressBar;
        public ImageView positionDelta;
        public LinearLayout chartsContainer;
    }
}
