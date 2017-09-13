package com.example.jackypeng.simulate_pulltorefresh;

import android.os.Build;
import android.view.View;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class PtrHelper {
    public static boolean canScrollUp(View view) {
        if (Build.VERSION.SDK_INT < 14) {
            return false;
        } else {
            return view.canScrollVertically(1);
        }
    }

    public static boolean canScrollDown(View view) {
        if (Build.VERSION.SDK_INT < 14) {
            return false;
        } else {
            return view.canScrollVertically(-1);
        }
    }
}
