package com.sicong.smartstore.stock_user.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_user.adapter.OverCatalogAdapter;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class OverCatalogActivity extends AppCompatActivity {

    //常量
    private final static  String TAG = "OverCatalogActivity";
    private OverCatalogActivity self = OverCatalogActivity.this;

    final int icons[] = {R.drawable.ic_stock_in,R.drawable.ic_stock_out,R.drawable.ic_stock_change,R.drawable.ic_stock_check};

    //控件
    private RecyclerView userOverListView;

    //适配器
    private OverCatalogAdapter overCatalogAdapter;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //线程
    private Thread dataThread;

    //数据
    private List<String> userOverList;
    private String username;
    private String check;
    private String company;

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
     * 初始化RecycleView
     */
    private void initList() {
        userOverList = new ArrayList<>();
        userOverList.add(getResources().getString(R.string.title_stock_in));
        userOverList.add(getResources().getString(R.string.title_stock_out));
        userOverList.add(getResources().getString(R.string.title_stock_change));
        userOverList.add(getResources().getString(R.string.title_stock_check));

        overCatalogAdapter = new OverCatalogAdapter(getBaseContext(), userOverList, check, company, username, icons);
        userOverListView.setAdapter(overCatalogAdapter);
        userOverListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        userOverListView.setHasFixedSize(true);
        userOverListView.setItemAnimator(new DefaultItemAnimator());
        userOverListView.addItemDecoration(new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));
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
     * 初始化控件
     */
    private void initView() {
        userOverListView = (RecyclerView) findViewById(R.id.user_over_rv);
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
