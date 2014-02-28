package com.nilsbo.egofm.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.nilsbo.egofm.util.ChartItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nilsbo.egofm.util.FragmentUtils.logTiming;

/**
 * Created by Nils on 21.01.14.
 */
public class ChartsRequest extends Request<ArrayList<ChartItem>> {
    private static final String TAG = "com.nilsbo.egofm.volley.PlaylistRequest";

    private final Listener<ArrayList<ChartItem>> mListener;
    private Pattern songRegex;
    private Date startDate;

    public ChartsRequest(int method, String url, Listener<ArrayList<ChartItem>> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        songRegex = Pattern.compile("(.+) - (.+)");
        startDate = new Date();
    }

    public ChartsRequest(String url, Listener<ArrayList<ChartItem>> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(ArrayList<ChartItem> response) {
        mListener.onResponse(response);
    }


    @Override
    protected Response<ArrayList<ChartItem>> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        ArrayList<ChartItem> charts = new ArrayList<ChartItem>();
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        logTiming("egoFM Request", "42 Request", startDate);

        try {
            String cookies = getCookies(response);

            final Document doc = Jsoup.parse(parsed);
            Elements chartElements = doc.select("div#ego42-list > div.rows > div.row");

            for (Element e : chartElements) {
                ChartItem p = parseChartRow(e);
                p.cookie = cookies;

                charts.add(p);
            }

        } catch (Exception e) {
            return null;
        }

        return Response.success(charts, HttpHeaderParser.parseCacheHeaders(response));
    }

    private String getCookies(NetworkResponse response) {
        String cookies = null;
        if (response.headers.containsKey("Set-Cookie")) {
            List<HttpCookie> cookieList = HttpCookie.parse(response.headers.get("Set-Cookie"));

            StringBuilder cookieString = new StringBuilder();
            for (int i = 0, cookiesSize = cookieList.size(); i < cookiesSize; i++) {
                cookieString.append(cookieList.get(i).getName());
                cookieString.append("=");
                cookieString.append(cookieList.get(i).getValue());
                if (i < cookiesSize - 1) {
                    cookieString.append("; ");
                }
            }
            cookies = cookieString.toString();
        }
        return cookies;
    }

    private ChartItem parseChartRow(Element e) {
        ChartItem p = new ChartItem();

        String song = e.select("div.artist").text();
        final Matcher matcher = songRegex.matcher(song);
        if (matcher.matches()) {
            p.artist = matcher.group(1);
            p.title = matcher.group(2);
        }
        Element position = e.select("div.num").first();
        p.position = position.text();
        if (position.hasClass("up")) {
            p.positionDelta = ChartItem.PositionDelta.Up;
        } else if (position.hasClass("down")) {
            p.positionDelta = ChartItem.PositionDelta.Down;
        } else if (position.hasClass("same")) {
            p.positionDelta = ChartItem.PositionDelta.Same;
        } else if (position.hasClass("new")) {
            p.positionDelta = ChartItem.PositionDelta.New;
        } else {
            p.positionDelta = ChartItem.PositionDelta.Unknown;
        }
        if (p.position.charAt(0) == 160) {
            p.positionDelta = ChartItem.PositionDelta.NewNoPosition;
        }

        p.votingState = ChartItem.State.NotVoted;
        p.id = e.select("input[name=id]").attr("value");
        p.key = e.select("input[value=1]").last().attr("name");
        return p;
    }
}
