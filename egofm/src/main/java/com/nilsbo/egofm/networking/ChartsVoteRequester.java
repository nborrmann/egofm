package com.nilsbo.egofm.networking;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.nilsbo.egofm.Interfaces.ChartVoteListener;
import com.nilsbo.egofm.util.ChartItem;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nilsbo.egofm.util.FragmentUtils.logTiming;

/**
 * Created by Nils on 09.02.14.
 */
public class ChartsVoteRequester {
    private static final String TAG = "com.nilsbo.egofm.networking.ChartsVoteRequester";
    private final ChartVoteListener mCallback;
    private final int mTag;
    private final String voteUrl = "http://www.egofm.de/musik/egofm-42?hitcount=0";
    private Date startDate;

    public ChartsVoteRequester(ChartItem song, ChartVoteListener mCallback, int tag) {
        this.mCallback = mCallback;
        mTag = tag;
        startDate = new Date();
        new VoteRequest().execute(song);
    }

    private class VoteRequest extends AsyncTask<ChartItem, Void, Integer> {

        protected Integer doInBackground(ChartItem... songs) {
            URL url;
            ChartItem song = songs[0];

            try {
                url = new URL(voteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Host", "www.egofm.de");
                conn.setRequestProperty("Origin", "http://www.egofm.de");
                conn.setRequestProperty("Referer", url.toString());

                if (!TextUtils.isEmpty(song.cookie)) {
                    conn.setRequestProperty("Cookie", song.cookie);
                }
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(generateParameters(song));
                out.close();

                try {
                    int responseCode = conn.getResponseCode();
                    String redirectUrl = String.valueOf(conn.getURL());

                    Log.i(TAG, "finished voting with status " + responseCode + "; url = " + redirectUrl);

                    if (responseCode == 303 && redirectUrl.equals(voteUrl)) {
                        return 0;
                    }
                } catch (IOException e) {
                    Log.w(TAG, String.format("Error downloading %s", voteUrl), e);
                    return 1;
                } finally {
                    if (conn != null) conn.disconnect();
                }
            } catch (IOException e) {
                Log.w(TAG, String.format("Error downloading %s", voteUrl), e);
                return 1;
            }
            return 2;
        }

        protected void onPostExecute(Integer status) {
            if (status == 0) {
                logTiming("egoFM Request", "42 Vote", startDate);
            }

            if (status == 0 && mCallback != null) mCallback.onSuccessfulVote(mTag);
            if (status == 1 && mCallback != null) mCallback.onNetworkError(mTag);
            if (status == 2 && mCallback != null) mCallback.onUnknownError(mTag);
        }
    }

    private byte[] generateParameters(ChartItem song) {
        HashMap<String, String> mParams = new HashMap<String, String>();
        mParams.put("user_rating", "1");
        mParams.put("option", "com_content");
        mParams.put("task", "article.vote");
        mParams.put("view", "article");
        mParams.put("id", song.id);
        mParams.put("hitcount", "0");
        mParams.put("url", voteUrl);
        mParams.put(song.key, "1");
        return encodeParameters(mParams, "UTF-8");
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

}
