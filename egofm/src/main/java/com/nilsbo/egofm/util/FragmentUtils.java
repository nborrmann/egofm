package com.nilsbo.egofm.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;

public class FragmentUtils {
    /**
     * @param frag              The Fragment whose parent is to be found
     * @param callbackInterface The interface class that the parent should implement
     * @return The parent of frag that implements the callbackInterface or null
     * if no such parent can be found
     */
    @SuppressWarnings("unchecked") // Casts are checked using runtime methods
    public static <T> T getParent(Fragment frag, Class<T> callbackInterface) {
        Fragment parentFragment = frag.getParentFragment();
        if (parentFragment != null
                && callbackInterface.isInstance(parentFragment)) {
            return (T) parentFragment;
        } else {
            FragmentActivity activity = frag.getActivity();
            if (activity != null && callbackInterface.isInstance(activity)) {
                return (T) activity;
            }
        }
        return null;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
                actionBarHeight += context.getResources().getDimensionPixelSize(resourceId);
        }
        return actionBarHeight;
    }

    public static int clamp(int value, int max, int min) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float max, float min) {
        return Math.min(Math.max(value, min), max);
    }

}
