package com.example.jackypeng.simulate_pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by jackypeng on 2017/9/11.
 */

public class SimulateClassicPtrLayout extends SimulatePtrLayout {

    private SimulateClassicDefaultHeader mClassicHeader;
    private SimulateClassicDefaultFooter mClassicFooter;

    public SimulateClassicPtrLayout(Context context) {
        this(context, null);
    }

    public SimulateClassicPtrLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimulateClassicPtrLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        mClassicHeader = new SimulateClassicDefaultHeader(context);
        mClassicFooter = new SimulateClassicDefaultFooter(context);
        /**
         * addView不会导致onFinishInflate方法的调用
         */
        setHeaderView(mClassicHeader);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * 这么写暂时为了准备的判断出子孩子的类型
                 */
                setFooterView(mClassicFooter);
            }
        }, 1000);
        addPtrHeaderUIHandler(mClassicHeader);
        addPtrFooterUIHandler(mClassicFooter);
    }




}
