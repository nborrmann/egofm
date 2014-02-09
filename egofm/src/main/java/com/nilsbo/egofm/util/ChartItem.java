package com.nilsbo.egofm.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nils on 08.02.14.
 */
public class ChartItem implements Parcelable {
    public String artist;
    public String title;
    public String position;
    public String id;
    public String key;
    public String cookie;
    public State votingState;
    public PositionDelta positionDelta;


    public enum State {
        Voted,
        InProgress,
        NotVoted
    }

    public enum PositionDelta {
        Up,
        Down,
        Same,
        New,
        NewNoPosition,
        Unknown
    }

    public ChartItem() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(position);
        dest.writeString(id);
        dest.writeString(key);
        dest.writeSerializable(votingState);
        dest.writeSerializable(positionDelta);
    }

    public static final Parcelable.Creator<ChartItem> CREATOR = new Parcelable.Creator<ChartItem>() {
        public ChartItem createFromParcel(Parcel in) {
            return new ChartItem(in);
        }

        public ChartItem[] newArray(int size) {
            return new ChartItem[size];
        }
    };

    public ChartItem(Parcel in) {
        artist = in.readString();
        title = in.readString();
        position = in.readString();
        id = in.readString();
        key = in.readString();
        votingState = (State) in.readSerializable();
        positionDelta = (PositionDelta) in.readSerializable();
    }
}
