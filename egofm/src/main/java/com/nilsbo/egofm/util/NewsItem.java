package com.nilsbo.egofm.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nils on 25.01.14.
 */
public class NewsItem implements Parcelable {
    public String date;
    public String title;
    public String subtitle;
    public String imgUrl;
    public String link;

    public NewsItem() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(imgUrl);
        dest.writeString(link);
    }

    public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
        public NewsItem createFromParcel(Parcel in) {
            return new NewsItem(in);
        }

        public NewsItem[] newArray(int size) {
            return new NewsItem[size];
        }
    };

    public NewsItem(Parcel in) {
        date = in.readString();
        title = in.readString();
        subtitle = in.readString();
        imgUrl = in.readString();
        link = in.readString();
    }

}
