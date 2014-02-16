package com.nilsbo.egofm.util;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nilsbo.egofm.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IntentView extends LinearLayout implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "com.nilsbo.egofm.util.IntentView";

    private static final int NUMBER_OF_BUTTONS = 3;

    private final Context context;
    private final PackageManager mPackageManager;
    private final ArrayList<ResolveInfo> mResolveInfos;
    public final ArrayList<DisplayResolveInfo> mDisplayResolveInfos;

    private ListPopupWindow intentOverflowList;
    private final LayoutInflater layoutInflater;
    private final ImageButton[] intentButtons;
    private String query = "";
    private PopupWindow popupWindow;
    private PopupTouchInterceptor mPopupTouchInterceptor = new PopupTouchInterceptor();
    private boolean justDismissed;

    public IntentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

        mPackageManager = context.getPackageManager();
        mResolveInfos = new ArrayList<ResolveInfo>();
        mDisplayResolveInfos = new ArrayList<DisplayResolveInfo>();
        loadIntentList();

        LayoutInflater.from(context).inflate(R.layout.fragment_song_details_intent_view, this, true);

        intentButtons = new ImageButton[NUMBER_OF_BUTTONS + 1];
        intentButtons[0] = (ImageButton) findViewById(R.id.song_details_intent_button1);
        intentButtons[1] = (ImageButton) findViewById(R.id.song_details_intent_button2);
        intentButtons[2] = (ImageButton) findViewById(R.id.song_details_intent_button3);
        intentButtons[3] = (ImageButton) findViewById(R.id.song_details_intent_overflow);

        // TODO test for values < 4
        int i = 0;
        for (; i < mDisplayResolveInfos.size() && i < NUMBER_OF_BUTTONS; i++) {
            intentButtons[i].setVisibility(View.VISIBLE);
            intentButtons[i].setImageDrawable(mDisplayResolveInfos.get(i).displayIcon);
            intentButtons[i].setOnClickListener(this);
        }
        for (; i < NUMBER_OF_BUTTONS; i++) {
            intentButtons[i].setVisibility(View.GONE);
        }

        if (mDisplayResolveInfos.size() < NUMBER_OF_BUTTONS) {
            intentButtons[3].setVisibility(View.GONE);
        } else {
            intentOverflowList = new ListPopupWindow(context);
            intentOverflowList.setAnchorView(intentButtons[3]);
            intentOverflowList.setWidth((int) context.getResources().getDimension(R.dimen.song_details_popup_width));
            SongIntentPopupAdapter adapter = new SongIntentPopupAdapter(mDisplayResolveInfos);
            intentOverflowList.setAdapter(adapter);
            intentOverflowList.setOnItemClickListener(this);

            try {
                final Field popupWindowField = ListPopupWindow.class.getDeclaredField("mPopup");
                popupWindowField.setAccessible(true);
                popupWindow = (PopupWindow) popupWindowField.get(intentOverflowList);
            } catch (Exception e) {
                Log.d(TAG, "Error instantiating ListPopupWindow", e);
            }

            intentButtons[3].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentOverflowList.show();
                    if (popupWindow != null) {
                        popupWindow.setTouchInterceptor(mPopupTouchInterceptor);
                    }
                }
            });

            intentButtons[3].setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Don't show the touch feedback if the overflow was just closed. Returning true will also
                    // prevent onClick from firing.
                    if (event.getAction() == MotionEvent.ACTION_DOWN && justDismissed) {
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        justDismissed = false;
                    }
                    return false;
                }
            });
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void onClick(View v) {
        onIntentClick(indexOfChild(v));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onIntentClick(position + NUMBER_OF_BUTTONS);
    }

    private void onIntentClick(int position) {
        mDisplayResolveInfos.get(position).startActivity();
    }

    public class SongIntentPopupAdapter extends BaseAdapter {
        private static final String TAG = "com.nilsbo.egofm.adapters.SongIntentPopupAdapter";

        private final List<DisplayResolveInfo> mDisplayResolveInfos;

        public SongIntentPopupAdapter(ArrayList<DisplayResolveInfo> mDisplayResolveInfos) {
            this.mDisplayResolveInfos = mDisplayResolveInfos;
        }

        @Override
        public int getCount() {
            //TODO
            return mDisplayResolveInfos.size() - NUMBER_OF_BUTTONS;
        }

        @Override
        public DisplayResolveInfo getItem(int position) {
            return mDisplayResolveInfos.get(position + NUMBER_OF_BUTTONS);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView " + position + " : " + getItem(position).extendedInfo);
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.popup_menu_intent, null, false);

                holder.icon = (ImageView) convertView.findViewById(R.id.popup_menu_icon);
                holder.title = (TextView) convertView.findViewById(R.id.popup_menu_title);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(getItem(position).extendedInfo);
            holder.icon.setImageDrawable(getItem(position).displayIcon);

            convertView.setTag(holder);
            return convertView;
        }

        private class ViewHolder {
            public TextView title;
            public ImageView icon;
        }
    }

    /**
     * This listener is added to intentOverflowList.mPopup (private field accessed through reflection).
     * It is intended to keep track of whether the popup was dismissed by a click on the OverflowButton.
     * If true, the button's onClick will not show the Popup the next time.
     */
    private class PopupTouchInterceptor implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            int[] l = new int[2];
            v.getLocationOnScreen(l);

            Rect r = new Rect();
            intentButtons[3].getGlobalVisibleRect(r);

            if (r.contains(x + l[0], y + l[1]) && intentOverflowList.isShowing()) {
                justDismissed = true;
            }
            return false;
        }
    }

    private void loadIntentList() {
        Intent mediaSearchAndPlayIntent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        Intent mediaSearchIntent = new Intent(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
        Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);

        // if MATCH_DEFAULT_ONLY is set, Google Music will not be matched, due to a bug(?) in GMusic.
        mResolveInfos.addAll(mPackageManager.queryIntentActivities(mediaSearchAndPlayIntent, PackageManager.GET_RESOLVED_FILTER));
        mResolveInfos.addAll(mPackageManager.queryIntentActivities(mediaSearchIntent, PackageManager.GET_RESOLVED_FILTER));
        mResolveInfos.addAll(mPackageManager.queryIntentActivities(searchIntent, PackageManager.GET_RESOLVED_FILTER));

        String tempPackageName;
        for (ResolveInfo resolveInfo : mResolveInfos) {
            try {
                tempPackageName = resolveInfo.activityInfo.applicationInfo.packageName;
                if (isFilteredApplication(tempPackageName) || isApplicationAlreadyAdded(tempPackageName))
                    continue;

                DisplayResolveInfo displayResolveInfo = new DisplayResolveInfo();
                displayResolveInfo.packageName = tempPackageName;
                displayResolveInfo.displayLabel = resolveInfo.activityInfo.loadLabel(mPackageManager);
                displayResolveInfo.extendedInfo = resolveInfo.activityInfo.applicationInfo.loadLabel(mPackageManager);
                displayResolveInfo.activityName = resolveInfo.activityInfo.name;
                displayResolveInfo.action = resolveInfo.filter.getAction(0);
                displayResolveInfo.displayIcon = resolveInfo.loadIcon(mPackageManager);
                mDisplayResolveInfos.add(displayResolveInfo);
            } catch (Exception e) {
                Log.w(TAG, "Exception while resolving Intent Activity. Skipping.", e);
            }
        }
    }

    private boolean isFilteredApplication(String packageName) {
        return "com.android.chrome".equals(packageName) || "com.google.android.youtube".equals(packageName);
    }

    private boolean isApplicationAlreadyAdded(String packageName) {
        for (int i = 0; i < mDisplayResolveInfos.size(); i++) {
            if (mDisplayResolveInfos.get(i).packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public final class DisplayResolveInfo {
        public CharSequence displayLabel;
        public CharSequence extendedInfo;
        public Drawable displayIcon;
        String packageName;
        String activityName;
        String action;

        DisplayResolveInfo() {
        }

        public void startActivity() {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityName));
            intent.setAction(action);
            intent.putExtra(SearchManager.QUERY, query);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
