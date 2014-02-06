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
    public String imgUrlBig;

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
        dest.writeString(imgUrlBig);
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
        imgUrlBig = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || NewsItem.class != o.getClass()) return false;

        NewsItem newsItem = (NewsItem) o;

        if (date != null ? !date.equals(newsItem.date) : newsItem.date != null) return false;
        if (imgUrl != null ? !imgUrl.equals(newsItem.imgUrl) : newsItem.imgUrl != null)
            return false;
        if (link != null ? !link.equals(newsItem.link) : newsItem.link != null) return false;
        if (subtitle != null ? !subtitle.equals(newsItem.subtitle) : newsItem.subtitle != null)
            return false;
        if (title != null ? !title.equals(newsItem.title) : newsItem.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subtitle != null ? subtitle.hashCode() : 0);
        result = 31 * result + (imgUrl != null ? imgUrl.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }
}
