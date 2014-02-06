package com.nilsbo.egofm.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nilsbo.egofm.R;

public class CustomListPreference extends ListPreference implements AdapterView.OnItemClickListener {

    public static final String TAG = "ThemedListPreference";

    private int mClickedDialogEntryIndex;

    private CharSequence mDialogTitle;

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPreference(Context context) {
        super(context);
    }

//    @Override
//    protected void onBindView(View view) {
//        super.onBindView(view);
//        TextView titleView = (TextView) view.findViewById(android.R.id.summary);
//        titleView.setTextColor(Color.RED);
//    }
//

    @Override
    protected View onCreateDialogView() {
        // inflate custom layout with custom date & listview
        View view = View.inflate(getContext(), R.layout.qustom_dialog_layout, null);

        mDialogTitle = getDialogTitle();
        if(mDialogTitle == null) mDialogTitle = getTitle();
        ((TextView) view.findViewById(R.id.dialog_title)).setText(mDialogTitle);

        ListView list = (ListView) view.findViewById(android.R.id.list);
        // note the layout we're providing for the ListView entries
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(), R.layout.select_dialog_singlechoice_holo,
                getEntries());

        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setItemChecked(findIndexOfValue(getValue()), true);
        list.setOnItemClickListener(this);

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // adapted from ListPreference
        if (getEntries() == null || getEntryValues() == null) {
            // throws exception
            super.onPrepareDialogBuilder(builder);
            return;
        }

        mClickedDialogEntryIndex = findIndexOfValue(getValue());

        // .setTitle(null) to prevent default (blue)
        // date+divider from showing up
        builder.setTitle(null);

        builder.setPositiveButton(null, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mClickedDialogEntryIndex = position;
        CustomListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // adapted from ListPreference
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0
                && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex]
                    .toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
}
