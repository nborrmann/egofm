package com.nilsbo.egofm.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;

import java.util.ArrayList;

/**
 * Created by Nils on 23.01.14.
 */
public class PlaylistItem implements Parcelable{
    public String artist;
    public String title;
    public String date;
    public String time;

    public PlaylistItem() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(time);
    }

    public static final Parcelable.Creator<PlaylistItem> CREATOR = new Parcelable.Creator<PlaylistItem>() {
        public PlaylistItem createFromParcel(Parcel in) {
            return new PlaylistItem(in);
        }

        public PlaylistItem[] newArray(int size) {
            return new PlaylistItem[size];
        }
    };

    private PlaylistItem(Parcel in) {
        artist = in.readString();
        title = in.readString();
        date = in.readString();
        time = in.readString();
    }
}