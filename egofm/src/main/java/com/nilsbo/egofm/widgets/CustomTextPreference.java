package com.nilsbo.egofm.widgets;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Nils on 25.01.14.
 */
public class CustomTextPreference extends Preference {

    public CustomTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setVisibility(View.GONE);
        TextView titleView = (TextView) view.findViewById(android.R.id.summary);
        titleView.setTextColor(Color.parseColor("#666666"));
    }

}
