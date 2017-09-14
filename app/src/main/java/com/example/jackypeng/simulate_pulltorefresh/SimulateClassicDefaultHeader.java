package com.example.jackypeng.simulate_pulltorefresh;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class SimulateClassicDefaultHeader extends FrameLayout implements SimulatePtrHeaderUIHandler {

    private static final String TAG = "SimulateClassicDefaultHeader";
    private TextView title;
    private TextView last_update_time;
    private ImageView rotate_view;
    private ProgressBar progressBar;
    private RotateAnimation downAnimation;
    private int mRotateTime = 150;
    private RotateAnimation upAnimation;
    private boolean mShouldShowLastUpdate;
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static String KEY_SharedPreferences = "simulate_ptr_last_update";
    private LastUpdateTimeUpdater mTimeUpdater = new LastUpdateTimeUpdater();

    public SimulateClassicDefaultHeader(@NonNull Context context) {
        this(context, null);
    }

    public SimulateClassicDefaultHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimulateClassicDefaultHeader(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        View header = LayoutInflater.from(context).inflate(R.layout.ptr_classic_default_header, this);
        title = (TextView) header.findViewById(R.id.header_title);
        last_update_time = (TextView) header.findViewById(R.id.header_last_update);
        rotate_view = (ImageView) header.findViewById(R.id.header_rotate_view);
        progressBar = (ProgressBar) header.findViewById(R.id.header_pb);
        buildAnimation();
    }

    private void buildAnimation() {
        downAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setInterpolator(new LinearInterpolator());
        downAnimation.setDuration(mRotateTime);
        downAnimation.setFillAfter(true);

        upAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setInterpolator(new LinearInterpolator());
        upAnimation.setDuration(mRotateTime);
        upAnimation.setFillAfter(true);
    }


    @Override
    public void onUIRefreshBegin(SimulatePtrLayout frame) {
        mShouldShowLastUpdate = false;
        title.setText("正在刷新");
        hideRotateView();
        progressBar.setVisibility(VISIBLE);
        title.setVisibility(VISIBLE);
        tryUpdateLastUpdateTime();
        mTimeUpdater.stop();
    }

    private void hideRotateView() {
        rotate_view.clearAnimation();
        rotate_view.setVisibility(INVISIBLE);
    }

    @Override
    public void onUIRefreshPrepare(SimulatePtrLayout frame) {
        mShouldShowLastUpdate = true;
        tryUpdateLastUpdateTime();
        mTimeUpdater.start();
        progressBar.setVisibility(INVISIBLE);
        rotate_view.setVisibility(VISIBLE);
        title.setVisibility(VISIBLE);
        title.setText("下拉刷新");
    }

    @Override
    public void onUIRefreshComplete(SimulatePtrLayout frame) {
        hideRotateView();
        progressBar.setVisibility(INVISIBLE);
        title.setVisibility(VISIBLE);
        title.setText("刷新完成");

        //update last update time
        SharedPreferences sp = getContext().getSharedPreferences(KEY_SharedPreferences, 0);
        if (!TextUtils.isEmpty(mLastUpdateTimeKey)) {
            mLastUpdateTime = new Date().getTime();
            Log.i(TAG, "update time");
            sp.edit().putLong(mLastUpdateTimeKey, mLastUpdateTime).commit();
        }
    }

    @Override
    public void onUIReset(SimulatePtrLayout frame) {
        resetView();
        Log.i(TAG, "onUIReset");
        mTimeUpdater.stop();
    }

    private void resetView() {
        hideRotateView();
        progressBar.setVisibility(INVISIBLE);
    }

    @Override
    public void onUIPositionChange(SimulatePtrLayout frame, boolean isUnderTouch, byte status, PtrIndicator indicator) {
        int offsetToRefresh = indicator.getOffsetToRefresh();
        int curPositionY = indicator.getCurPositionY();
        int lastPosY = indicator.getLastPosY();

        if (curPositionY < offsetToRefresh && lastPosY >= offsetToRefresh) {
            if (isUnderTouch && status == SimulatePtrLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromBottomUnderTouch();
            }
        } else if (curPositionY > offsetToRefresh && lastPosY <= offsetToRefresh) {
            if (isUnderTouch && status == SimulatePtrLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromTopUnderTouch();
            }
        }
    }

    private void crossRotateLineFromTopUnderTouch() {
        title.setText("释放刷新");
        title.setVisibility(VISIBLE);
        if (rotate_view != null) {
            rotate_view.clearAnimation();
            rotate_view.startAnimation(downAnimation);
        }
    }

    private void crossRotateLineFromBottomUnderTouch() {
        title.setText("下拉刷新");
        title.setVisibility(VISIBLE);
        if (rotate_view != null) {
            rotate_view.clearAnimation();
            rotate_view.startAnimation(upAnimation);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTimeUpdater != null) {
            mTimeUpdater.stop();
        }
    }

    private String mLastUpdateTimeKey;

    private class LastUpdateTimeUpdater implements Runnable {

        private boolean mRunning = false;

        private void start() {
            if (TextUtils.isEmpty(mLastUpdateTimeKey)) {
                return;
            }
            mRunning = true;
            run();
        }

        private void stop() {
            mRunning = false;
            removeCallbacks(this);
        }

        @Override
        public void run() {  //该方法是在主线程中执行的
            tryUpdateLastUpdateTime();
            postDelayed(this, 1000);   //1s更新一次
        }
    }

    private void tryUpdateLastUpdateTime() {
        if (TextUtils.isEmpty(mLastUpdateTimeKey) || !mShouldShowLastUpdate) {
            last_update_time.setVisibility(GONE);
        } else {
            String lastUpdateTime = getLastUpdateTime();
            if (TextUtils.isEmpty(lastUpdateTime)) {
                last_update_time.setVisibility(GONE);
            } else {
                last_update_time.setVisibility(VISIBLE);
                last_update_time.setText(lastUpdateTime);
            }
        }
    }

    private long mLastUpdateTime = -1;

    public void setLastUpdateTimeRelateObject(Object object) {
        if (object != null) {
            setLastUpdateTimeKey(object.getClass().getName());
        }
    }

    private void setLastUpdateTimeKey(String canonicalName) {
        mLastUpdateTimeKey = canonicalName;
    }

    private String getLastUpdateTime() {

//        Log.i(TAG, "mLastUpdateTimeKey:" + mLastUpdateTimeKey);
        if (mLastUpdateTime == -1 && !TextUtils.isEmpty(mLastUpdateTimeKey)) {
            mLastUpdateTime = getContext().getSharedPreferences(KEY_SharedPreferences, 0).getLong(mLastUpdateTimeKey, -1);
        }
        if (mLastUpdateTime == -1) {
            return null;
        }
//        Log.i(TAG, "mLastUpdateTime:" + mLastUpdateTime);
        long diffTime = new Date().getTime() - mLastUpdateTime;
        int seconds = (int) (diffTime / 1000);
        if (diffTime < 0) {
            return null;
        }
        if (seconds <= 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("last update:");
        if (seconds < 60) {
            builder.append(seconds + " seconds ago");
        } else {
            int minutes = seconds / 60;
            if (minutes > 60) {
                int hours = minutes / 60;
                if (hours > 24) {
                    builder.append(sDataFormat.format(new Date(mLastUpdateTime)));
                } else {
                    builder.append(hours + " hours ago");
                }
            } else {
                builder.append(minutes + " minutes ago");
            }
        }
        return builder.toString();
    }
}
