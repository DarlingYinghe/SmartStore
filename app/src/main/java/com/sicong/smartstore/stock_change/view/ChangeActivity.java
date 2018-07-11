package com.sicong.smartstore.stock_change.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sicong.smartstore.R;

public class ChangeActivity extends AppCompatActivity {

    //常量
    private static final String TAG = "ChangeActivity";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int DETAIL_SUCCESS = 1;
    private static final int DETAL_FAIL = 2;
    private static final int DETAL_ERROR = 3;

    private static final int SUBMIT_SUCCESS = 4;
    private static final int SUBMIT_FAIL = 5;
    private static final int SUBMIT_ERROR = 6;

    //视图

    //数据

    //适配器

    //线程
    private Thread detailThread;
    private Thread submitThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
    }
}
