package com.example.jackypeng.simulate_pulltorefresh;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class SimulateClassicDefaultFooter extends FrameLayout implements SimulatePtrUIHandler {

    private static final String TAG = "SimulateClassicDefaultFooter";
    private TextView title;

    public SimulateClassicDefaultFooter(@NonNull Context context) {
        this(context, null);
    }

    public SimulateClassicDefaultFooter(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimulateClassicDefaultFooter(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {

        View header = LayoutInflater.from(context).inflate(R.layout.ptr_classic_default_footer, this);
        title = (TextView) header.findViewById(R.id.footer_title);

    }

    @Override
    public void onUIRefreshBegin(SimulatePtrLayout frame) {
        title.setText("正在加载");
    }

    @Override
    public void onUIRefreshPrepare(SimulatePtrLayout frame) {
        //跟新界面
    }

    @Override
    public void onUIRefreshComplete(SimulatePtrLayout frame) {
        title.setText("加载完成");
    }

    @Override
    public void onUIReset(SimulatePtrLayout frame) {
        title.setText("初始状态");
    }

    @Override
    public void onUIPositionChange(SimulatePtrLayout frame, boolean isUnderTouch, byte status, PtrIndicator indicator) {
        int offsetToRefresh = indicator.getOffsetToRefresh();
        int curPositionY = indicator.getCurPositionY();
        int lastPosY = indicator.getLastPosY();

        Log.i(TAG, "offsetToRefresh:" + offsetToRefresh);
        Log.i(TAG, "curPositionY:" + curPositionY);
        Log.i(TAG, "lastPosY:" + lastPosY);

        if (curPositionY < offsetToRefresh && lastPosY >= offsetToRefresh) {
            if (isUnderTouch && status == SimulatePtrLayout.PTR_STATUS_PREPARE) {
                title.setText("上拉加载");
            }
        } else if (curPositionY > offsetToRefresh && lastPosY <= offsetToRefresh) {
            if (isUnderTouch && status == SimulatePtrLayout.PTR_STATUS_PREPARE) {
                title.setText("释放加载");
            }
        }
    }
}
