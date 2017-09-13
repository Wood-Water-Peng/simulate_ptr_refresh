package com.example.jackypeng.simulate_pulltorefresh;

import android.content.Context;
import android.support.annotation.BoolRes;
import android.support.annotation.Px;
import android.support.v4.app.NavUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class SimulatePtrLayout extends ViewGroup {
    private static final String TAG = "SimulatePtrLayout";
    private byte mStatus = PTR_STATUS_INIT;
    // status enum
    public final static byte PTR_STATUS_INIT = 1;
    public final static byte PTR_STATUS_PREPARE = 2;
    public final static byte PTR_STATUS_LOADING = 3;
    public final static byte PTR_STATUS_COMPLETE = 4;

    private View mHeaderView;
    private int mHeaderHeight;
    private View mFooterView;
    private int mFooterHeight;
    private View mContent;
    private PtrIndicator ptrIndicator;
    private MotionEvent mLastMoveEvent;
    private int touchSlop;
    private SimulatePtrHandler mPtrHandler;
    private boolean mKeepHeaderWhenRefresh;
    private int mDurationToBackHeader = 2000;
    private SimulatePtrUIHandler mPtrFooterUIHandler;
    private SimulatePtrUIHandler mPtrHeaderUIHandler;

    public SimulatePtrLayout(Context context) {
        this(context, null);
    }

    public SimulatePtrLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimulatePtrLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //必要的初始化操作
        ptrIndicator = new PtrIndicator();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    public void setHeaderView(View headerView) {
        if (mHeaderView != null && headerView != null && mHeaderView != headerView) {
            removeView(mHeaderView);
        }
        LayoutParams lp = headerView.getLayoutParams();
        if (lp == null) {
            lp = new PtrLayoutParams(-1, -2);
            headerView.setLayoutParams(lp);
        }
        mHeaderView = headerView;
        addView(headerView);
    }

    public void setFooterView(View footerView) {
        if (mFooterView != null && footerView != null && mFooterView != footerView) {
            removeView(mFooterView);
        }
        LayoutParams lp = footerView.getLayoutParams();
        if (lp == null) {
            lp = new PtrLayoutParams(-1, -2);
            footerView.setLayoutParams(lp);
        }
        mFooterView = footerView;
        addView(footerView);
    }

    public void setPtrHandler(SimulatePtrHandler ptrHandler) {
        mPtrHandler = ptrHandler;
    }

    public void addPtrHeaderUIHandler(SimulatePtrUIHandler headUIHandler) {
        this.mPtrHeaderUIHandler = headUIHandler;
    }

    public void addPtrFooterUIHandler(SimulatePtrUIHandler footUIHandler) {
        this.mPtrFooterUIHandler = footUIHandler;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new PtrLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new PtrLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new PtrLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = getChildCount();
        Log.i(TAG, "onFinishInflate---childCount:" + getChildCount());
        if (childCount > 3) {
            throw new IllegalStateException("PtrFrameLayout only can host 3 elements");
        } else if (childCount == 2) {  //ignore the footer by default
            //根据类的类型来确定header和content
            if (mContent == null || mHeaderView == null) {
                View child1 = getChildAt(0);
                View child2 = getChildAt(1);
                if (child1 instanceof SimulatePtrUIHandler) {
                    mHeaderView = child1;
                    mContent = child2;
                } else if (child2 instanceof SimulatePtrUIHandler) {
                    mHeaderView = child2;
                    mContent = child1;
                }
            }
        } else if (childCount == 3) {
            Log.i(TAG, "child_01:" + getChildAt(0).getClass().getCanonicalName());
            Log.i(TAG, "child_02:" + getChildAt(1).getClass().getCanonicalName());
            Log.i(TAG, "child_03:" + getChildAt(2).getClass().getCanonicalName());
            if (mContent == null || mHeaderView == null || mFooterView == null) {
                View child1 = getChildAt(0);
                View child2 = getChildAt(1);
                View child3 = getChildAt(2);
                //all are not specified
                if (mContent == null && mHeaderView == null && mFooterView == null) {
                    mHeaderView = child1;
                    mContent = child2;
                    mFooterView = child3;
                }
            }
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * step 1: 测量出各子视图的大小，包括header,content,footer
         */

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            ptrIndicator.setHeaderHeight(mHeaderHeight);
        }
        if (mFooterView != null) {
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            ptrIndicator.setFooterHeight(mFooterHeight);
        }
        if (mContent != null) {
            measureContentView(mContent, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            //support for wrap_content
            if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
                super.setMeasuredDimension(getMeasuredWidth(), mContent.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }
        }

    }

    public static class PtrLayoutParams extends MarginLayoutParams {

        public PtrLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public PtrLayoutParams(int width, int height) {
            super(width, height);
        }

        public PtrLayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public PtrLayoutParams(LayoutParams source) {
            super(source);
        }
    }


    private void measureContentView(View child, int widthMeasureSpec, int heightMeasureSpec) {
//        MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
        child.measure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean flag, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int contentBottom = 0;

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin - mHeaderHeight;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);
        }
        if (mContent != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            int left;
            int top;
            int right;
            int bottom;
            left = paddingLeft + lp.leftMargin;
            top = paddingTop + lp.topMargin;
            right = left + mContent.getMeasuredWidth();
            bottom = top + mContent.getMeasuredHeight();

            mContent.layout(left, top, right, bottom);
            contentBottom = bottom;
        }

        if (mFooterView != null) {
            Log.i(TAG, "layout footerview");
            Log.i(TAG, "contentBottom:" + contentBottom);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + contentBottom;
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + mFooterView.getMeasuredHeight();
            mFooterView.layout(left, top, right, bottom);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mContent == null || mHeaderView == null) {
            return super.dispatchTouchEvent(ev);
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (ptrIndicator.hasLeftStartPosition()) {
                    onRelease();
                } else {
                    //控件没有滑动过,按正常的分发逻辑走下去
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                //用一个专门的类，将我移动的距离记录下来，方便后面的滑动，回滚操作
                ptrIndicator.onPressDown(ev.getX(), ev.getY());
                Log.i(TAG, "ACTION_DOWN");
                //让子孩子拿到down事件，如果该控件不处理后续的move事件，子孩子才能拿到后续的move事件
                super.dispatchTouchEvent(ev);

                //返回true,该控件才能拿到后续的move事件
                return true;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = ev;
                ptrIndicator.onMove(ev.getX(), ev.getY());

                float offsetX = ptrIndicator.getOffsetX();
                float offsetY = ptrIndicator.getOffsetY();
                if (Math.abs(offsetX) > touchSlop && Math.abs(offsetX) > Math.abs(offsetY)) {
                    //不滑动
                }
                //判断用户的滑动方向
                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                //判断当前是header显示还是footer显示
                boolean canMoveUp = ptrIndicator.isHeader() && ptrIndicator.hasLeftStartPosition();

                boolean canMoveDown = mFooterView != null && !ptrIndicator.isHeader() && ptrIndicator.hasLeftStartPosition();

//                Log.i(TAG, "canMoveUp:" + canMoveUp);
//                Log.i(TAG, "canMoveDown:" + canMoveDown);

                /**
                 * 头部能否被下滑，在于content还能不能向下滑动
                 */
                boolean canHeaderMoveDown = (mPtrHandler != null && !PtrHelper.canScrollDown(mContent));
                /**
                 * footer能否被上拉，在于content还能不能向上滑动
                 */
                boolean canFooterMoveUp = (mPtrHandler != null && mFooterView != null && !PtrHelper.canScrollUp(mContent));

                Log.i(TAG, "canHeaderMoveDown:" + canHeaderMoveDown);
                Log.i(TAG, "canFooterMoveUp:" + canFooterMoveUp);

                //header和footer都没有显示出来
                if (!canMoveUp && !canMoveDown) {
                    Log.i(TAG, "header和footer都没有显示出来");
                    //先判断content能否滑动,如ListView,ScrollView
                    if (moveDown && !canHeaderMoveDown) {
                        return super.dispatchTouchEvent(ev);
                    }
                    if (moveUp && !canFooterMoveUp) {
                        return super.dispatchTouchEvent(ev);
                    }
//                    向下滑动，那么应该显示头部
                    if (moveDown) {
                        Log.i(TAG, "---moveDown---");
                        moveHeaderPos(offsetY);
                        return true;
                    }
//                    向上滑动,那么应该拉出footer
                    if (moveUp) {
                        Log.i(TAG, "---moveUp---");
                        moveFooterPos(offsetY);
                        return true;
                    }
                }
                //if header is showing,then no need to move footer
//                Log.i(TAG, "offsetY:" + offsetY);
                if (canMoveUp) {    //头部显示了一部分,且可以向上滑动
                    Log.i(TAG, "header滑动");
                    moveHeaderPos(offsetY);
                    return true;
                }
                if (canMoveDown) {
                    Log.i(TAG, "footer滑动");
                    moveFooterPos(offsetY);
                    return true;
                }

                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    /**
     * 该方法在onDraw()中调用,计算出视图的scroll值
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
//            Log.i(TAG, "curY:" + mScroller.getCurrY());
            if (-mScroller.getCurrY() == getScrollY()) {
                invalidate();
            } else {
                scrollTo(0, -mScroller.getCurrY());
                ptrIndicator.setCurPosition(Math.abs(getScrollY()));
            }
            /**
             * 如何定义一次滑动是否结束
             * 1.确保之前必须滑动过
             * 2.滑动现在已经停止了
             */
            if (ptrIndicator.isInStartPosition()) {
//                Log.i(TAG, "---滑动结束---");
                setEnabled(true);  //在滑动过程中该控件不响应任何的触摸事件
//                Log.i(TAG, "curScrollY:" + getScrollY());
                if (ptrIndicator.isInStartPosition()) {
                    tryToNotifyReset();
                }
            }
        }
    }

    /**
     * 刷新完毕
     */
    public void notifyUIRefreshComplete() {
        tryScrollToBackAfterComplete();
        if (ptrIndicator.isHeader()) {
            if (mPtrHeaderUIHandler != null) {
                mPtrHeaderUIHandler.onUIRefreshComplete(this);
            }
        } else {
            if (mPtrFooterUIHandler != null) {
                mPtrFooterUIHandler.onUIRefreshComplete(this);
            }
        }
//        tryToNotifyReset();
    }

    private void tryToNotifyReset() {
        mStatus = PTR_STATUS_INIT;
//        if (mPtrUIHandler != null) {
//            mPtrUIHandler.onUIReset(this);
//        }
    }

    private void tryScrollToBackAfterComplete() {
        tryScrollBackToTop();
    }

    private void tryScrollBackToTop() {
        tryToScrollTo(PtrIndicator.START_POS, mDurationToBackHeader);
    }

    private Scroller mScroller;

    private void onRelease() {
        /**
         * headView可能的几种状态
         * 1.准备刷新
         * 2.正在刷新
         */
        Log.i(TAG, "onRelease");

        tryToPerformRefresh();

        Log.i(TAG, "status:" + mStatus);

        if (mStatus == PTR_STATUS_LOADING) {

            tryToScrollTo(ptrIndicator.getOffsetToKeepHeaderWhileLoading(), mDurationToBackHeader);

            if (mKeepHeaderWhenRefresh) {
                if (ptrIndicator.isHeader()) {
                    //scrollTo
//                    tryToScrollTo(ptrIndicator.getOffsetToKeepHeaderWhileLoading(), mDurationToBackHeader);
                }
            }
        } else {
            if (mStatus == PTR_STATUS_PREPARE) {
                tryScrollBackToTopAbortRefresh();
            }
        }
    }

    private void tryScrollBackToTopAbortRefresh() {
        tryScrollBackToTop();
    }

    /**
     * @param offsetToKeepHeaderWhileLoading 滑动的目的地
     * @param mDurationToBackHeader          滑动的时间
     */
    private void tryToScrollTo(int offsetToKeepHeaderWhileLoading, int mDurationToBackHeader) {
        setEnabled(false);
        Log.i(TAG, "start:" + ptrIndicator.getCurPositionY());
        Log.i(TAG, "end:" + offsetToKeepHeaderWhileLoading);
        Log.i(TAG, "dis:" + (offsetToKeepHeaderWhileLoading - ptrIndicator.getCurPositionY()));
        if (ptrIndicator.isHeader()) {
            mScroller.startScroll(0, ptrIndicator.getCurPositionY(), 0, offsetToKeepHeaderWhileLoading - ptrIndicator.getCurPositionY(), mDurationToBackHeader);
        } else {
            mScroller.startScroll(0, -ptrIndicator.getCurPositionY(), 0, -offsetToKeepHeaderWhileLoading + ptrIndicator.getCurPositionY(), mDurationToBackHeader);
        }
        invalidate();
    }

    //尝试着执行刷新操作
    private void tryToPerformRefresh() {
        if (mStatus != PTR_STATUS_PREPARE) {
            return;
        }
        if (ptrIndicator.isOverOffsetToRefresh()) {
            mStatus = PTR_STATUS_LOADING;
            performRefresh();
        }
    }

    //更新状态、界面信息、通知监听器
    private void performRefresh() {
        if (mPtrHandler != null) {
            mPtrHandler.onRefreshBegin(this);
        }
        if (ptrIndicator.isHeader()) {
            if (mPtrHeaderUIHandler != null) {
                mPtrHeaderUIHandler.onUIRefreshBegin(this);
            }
        } else {
            if (mPtrFooterUIHandler != null) {
                mPtrFooterUIHandler.onUIRefreshBegin(this);
            }
        }
    }

    private void moveFooterPos(float offsetY) {
        ptrIndicator.setIsHeader(false);
        movePos(offsetY);
    }

    private void movePos(float offsetY) {
        /**
         * 确保ptrIndicator中保存的都是正整数
         */
//        Log.i(TAG, "offsetY:" + offsetY);
        if (ptrIndicator.isHeader()) {
            ptrIndicator.setCurPosition(ptrIndicator.getCurPositionY() + (int) offsetY);
        } else {
            ptrIndicator.setCurPosition(ptrIndicator.getCurPositionY() - (int) offsetY);
        }

        //更新状态   init--->prepare
        if (ptrIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) {
            mStatus = PTR_STATUS_PREPARE;
            //通知界面进行改变
//            mPtrUIHandler.onUIRefreshBegin(this);
        }
        //更新状态   prepare--->loading
        if (mStatus == PTR_STATUS_PREPARE) {
//            tryToPerformRefresh();
        }
        scrollBy(0, -(int) offsetY);
//        if (ptrIndicator.isHeader()) {
//        } else {
//            scrollBy(0, (int) offsetY);
//        }

//        Log.i(TAG, "curY:" + ptrIndicator.getCurPositionY());
//        Log.i(TAG, "lastY:" + ptrIndicator.getLastPosY());
//        Log.i(TAG, "offsetToRefresh:" + ptrIndicator.getOffsetToRefresh());
        if (ptrIndicator.isHeader()) {
            if (mPtrHeaderUIHandler != null) {
                mPtrHeaderUIHandler.onUIPositionChange(this, true, mStatus, ptrIndicator);
            }
        } else {
            if (mPtrFooterUIHandler != null) {
                mPtrFooterUIHandler.onUIPositionChange(this, true, mStatus, ptrIndicator);
            }
        }
    }

    private void moveHeaderPos(float offsetY) {
        /**
         * 还需要经过一系列判断，是否滑到极限了之类
         * 然后再决定是否要执行具体的滑动
         *
         * 在滑动中，根据具体的滑动距离，需要设置ptrIndicator的状态
         */
        ptrIndicator.setIsHeader(true);
        movePos(offsetY);

    }
}
