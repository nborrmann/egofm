package com.nilsbo.egofm.Interfaces;

import com.nilsbo.egofm.util.NewsItem;

/**
 * Created by Nils on 11.02.14.
 */
public interface NewsListListener {
    public void onItemClicked(NewsItem item);

    void onDefault(NewsItem newsItem);
}
