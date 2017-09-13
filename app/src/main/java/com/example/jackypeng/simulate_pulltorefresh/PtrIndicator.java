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

    public void onPressDown(float x, float y) {
        mLastMove.set(x, y);
    }

    public void onMove(float x, float y) {
        float offsetX = x - mLastMove.x;
        float offsetY = y - mLastMove.y;
        processOnMove(offsetX, offsetY);
        mLastMove.set(x, y);
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
        mOffsetY = offsetY / mResistanceHeader;
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
}
