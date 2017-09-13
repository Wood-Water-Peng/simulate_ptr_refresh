package com.example.jackypeng.simulate_pulltorefresh;

/**
 * Created by jackypeng on 2017/9/11.
 */

public interface SimulatePtrUIHandler {
    void onUIRefreshBegin(SimulatePtrLayout frame);

    void onUIRefreshPrepare(SimulatePtrLayout frame);

    void onUIRefreshComplete(SimulatePtrLayout frame);

    void onUIReset(SimulatePtrLayout frame);

    void onUIPositionChange(SimulatePtrLayout frame, boolean isUnderTouch,byte status,PtrIndicator indicator);
}
