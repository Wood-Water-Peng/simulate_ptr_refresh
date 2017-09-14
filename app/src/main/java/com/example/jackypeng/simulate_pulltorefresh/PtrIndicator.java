package com.example.jackypeng.simulate_pulltorefresh;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class PtrIndicator {
    private PointF mLastMove = new PointF();
    private float mOffsetX, mOffsetY;
    private float mResistanceHeader = 1.3f;
    public static final int START_POS = 0;
    private int mCurPosition;
    private int mLastPos;
    private int mOffsetToRefresh;
    private float mRatioOfHeaderHeightToRefresh = 1.4f;
    private float mHeaderHeight;
    private int mOffsetToLoadMore;
    private float mFooterHeight;
    private float mHeaderheight;
    private boolean isHeader = false;
    private boolean mIsUnderTouch = false;
    private int mPressPos;

    public void onPressDown(float x, float y) {
        mIsUnderTouch = true;
        mPressPos = mCurPosition;
        mLastMove.set(x, y);
    }

    public void onMove(float x, float y) {
        float offsetX = x - mLastMove.x;
        float offsetY = y - mLastMove.y;
        processOnMove(offsetX, offsetY);
        mLastMove.set(x, y);
    }

    public void onRelease() {
        mIsUnderTouch = false;
    }

    public void setRatioOfHeaderHeightToRefresh(float ratio) {
        mRatioOfHeaderHeightToRefresh = ratio;
        mOffsetToRefresh = (int) (mHeaderHeight * ratio);
        mOffsetToLoadMore = (int) (mFooterHeight * ratio);
    }

    public void setOffsetToRefresh(int offset) {
        mRatioOfHeaderHeightToRefresh = mHeaderheight * 1.0f / offset;
        mOffsetToRefresh = offset;
        mOffsetToLoadMore = offset;
    }

    private void processOnMove(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        float cur_resist;
        if (isOverOffsetToRefresh()) {
            //可采用某种数学公式---根据距离动态的改变阻力
//            cur_resist = (float) (mResistanceHeader * Math.log(getCurPositionY() - getOffsetToRefresh()));
        } else {
        }
        cur_resist = mResistanceHeader;
        mOffsetY = offsetY / cur_resist;
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean hasLeftStartPosition() {
        return mCurPosition > START_POS;
    }

    //记录了滑动的距离，刷新-->大于0   加载-->小与0
    public int getCurPositionY() {
        return mCurPosition;
    }

    public void setCurPosition(int current) {
        mLastPos = mCurPosition;
        mCurPosition = current;
    }

    public boolean isOverOffsetToRefresh() {
        return mCurPosition > getOffsetToRefresh();
    }

    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }

    public void setHeaderHeight(int headerHeight) {
        this.mHeaderHeight = headerHeight;
        mOffsetToRefresh = (int) (mRatioOfHeaderHeightToRefresh * mHeaderHeight);
//        mOffsetToLoadMore = (int) (mRatioOfHeaderHeightToRefresh * mFooterHeight);
    }

    public void setFooterHeight(int footerHeight) {
        this.mFooterHeight = footerHeight;
//        mOffsetToRefresh = (int) (mRatioOfHeaderHeightToRefresh * mHeaderHeight);
        mOffsetToLoadMore = (int) (mRatioOfHeaderHeightToRefresh * mFooterHeight);
    }

    public int getOffsetToKeepHeaderWhileLoading() {
        if (isHeader()) {
            return (int) mHeaderHeight;
        } else {
            return (int) mFooterHeight;
        }
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public boolean hasJustLeftStartPosition() {
        return mLastPos == START_POS && hasLeftStartPosition();
    }

    public boolean isInStartPosition() {
        return getCurPositionY() == START_POS;
    }

    public int getLastPosY() {
        return mLastPos;
    }

    public boolean isUnderTouch() {
        return mIsUnderTouch;
    }

    public boolean hasBackToStartPosition() {
        return mLastPos != START_POS && isInStartPosition();
    }

    public boolean hasMovedAfterPressedDown() {
        return mCurPosition != mPressPos;
    }

    public boolean willOverTop(int to) {
        return to < START_POS;
    }
}
