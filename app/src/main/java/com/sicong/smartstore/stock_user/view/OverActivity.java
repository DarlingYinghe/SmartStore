package com.sicong.smartstore.stock_user.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

public class OverActivity extends AppCompatActivity {

    //常量
    private OverActivity self = OverActivity.this;

    //适配器

    //控件
    private RecyclerView recyclerView;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //数据
    private String username;
    private String company;
    private String check;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over);
        initNetBoardcastReceiver();//初始化网络广播
        initView();//初始化控件
        initReceive();//接受上个Activity的传值
        initList();//初始化RecycleView
    }

    /**
     *
     */
    private void initList(){

    }

    /**
     * 控件初始化
     */
    private void initView(){
        recyclerView = findViewById(R.id.user_over_rv);
    }

    /**
     * 接受上个Activity的传值
     */
    private void initReceive() {
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("check")){
            check = intent.getStringExtra("check");
        }
        if(intent != null && intent.hasExtra("company")){
            company = intent.getStringExtra("company");
        }
        if(intent != null && intent.hasExtra("username")){
            username = intent.getStringExtra("username");
        }
    }

    /**
     * 初始化网络广播
     */
    private void initNetBoardcastReceiver() {
        if (netBroadcastReceiver == null) {
            netBroadcastReceiver = new NetBroadcastReceiver();
            netBroadcastReceiver.setNetChangeListern(new NetBroadcastReceiver.NetChangeListener() {
                @Override
                public void onChangeListener(boolean status) {
                    if(status) {
                        //写入要启动的线程
                    } else {
                        Toast.makeText(self, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, filter);
    }


    /**
     * 生命周期中的部分操作
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);//注销广播
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();//注册广播
    }
}
