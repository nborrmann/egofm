package com.nilsbo.egofm.Interfaces;

/**
 * Created by Nils on 09.02.14.
 */
public interface ChartVoteListener {
    public void onSuccessfulVote(int mTag);

    public void onNetworkError(int mTag);

    public void onUnknownError(int mTag);
}
