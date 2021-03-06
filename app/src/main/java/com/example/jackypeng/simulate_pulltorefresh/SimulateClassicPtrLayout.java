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
        mClassicHeader.setLastUpdateTimeRelateObject(this);
        setHeaderView(mClassicHeader);
        setFooterView(mClassicFooter);
        addPtrHeaderUIHandler(mClassicHeader);
        addPtrFooterUIHandler(mClassicFooter);
    }


}
