package com.sicong.smartstore.stock_user.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_user.adapter.OverListAdapter;
import com.sicong.smartstore.stock_user.adapter.UnoverListAdapter;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OverActivity extends AppCompatActivity {

    //常量
    private final static  String TAG = "OverActivity";
    private OverActivity self = OverActivity.this;
    private static final int OVER_SUCCESS = 1;
    private static final int OVER_FAIL = 2;
    private static final int OVER_ERROR = 3;

    //控件
    private RecyclerView userOverListView;
    private Handler handler;

    //适配器
    private OverListAdapter overListAdapter;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //线程
    private Thread dataThread;

    //数据
    private List<Map<String, String>> userOverList;
    private String username;
    private String check;
    private String company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over);
        initNetBoardcastReceiver();//初始化网络广播
        initView();//初始化控件
        initHandler();//初始化Handler
        initReceive();//接受上个Activity的传值
        initList();//初始化RecycleView
    }


    /**
     * 初始化RecycleView
     */
    private void initList() {
        userOverList = new ArrayList<Map<String,String>>();

        overListAdapter = new OverListAdapter(getBaseContext(), userOverList, check, company, username);
        userOverListView.setAdapter(overListAdapter);
        userOverListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        userOverListView.setHasFixedSize(true);
        userOverListView.setItemAnimator(new DefaultItemAnimator());
        userOverListView.addItemDecoration(new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));
    }


    /**
     * 启动线程，获取数据
     */
    public void startDataRequestThread(){
        dataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //第一步：首先从数据库中获取数据/看是否有后台；


                    List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();



                    userOverList = new ArrayList<Map<String, String>>();
                    userOverList.clear();
                    userOverList.addAll(mapList);

                    //第二步：判断数量的多少
                    if(userOverList.size() < 1 ){
                        handler.sendEmptyMessage(OVER_FAIL);
                    }else{
                        handler.sendEmptyMessage(OVER_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(OVER_ERROR);
                    Log.e(TAG, "数据获取失败", null);
                }
            }
        });
        dataThread.start();
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
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case OVER_ERROR:
                        Toast.makeText(self, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                        break;
                    case OVER_FAIL:
                        Toast.makeText(self, "不存在未完成的单号", Toast.LENGTH_SHORT).show();
                        break;
                    case OVER_SUCCESS:
                        overListAdapter.notifyDataSetChanged();
                        Toast.makeText(self, "未完成单号列表获取成功", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
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
                        startDataRequestThread();
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
        initNetBoardcastReceiver();
    }
}
