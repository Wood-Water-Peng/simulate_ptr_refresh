package com.example.jackypeng.simulate_pulltorefresh;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SimulateClassicPtrLayout classicPtrLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        classicPtrLayout = (SimulateClassicPtrLayout) findViewById(R.id.ptr_frame_layout);
        classicPtrLayout.setPtrHandler(new SimulatePtrHandler() {
            @Override
            public void onRefreshBegin(SimulatePtrLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        classicPtrLayout.notifyUIRefreshComplete();
                    }
                }, 3000);
            }
        });
    }
}
