package com.sicong.smartstore.stock_user.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_out.adapter.OutListAdapter;
import com.sicong.smartstore.stock_user.adapter.UnoverListAdapter;
import com.sicong.smartstore.stock_user.model.UnoverMessage;
import com.sicong.smartstore.stock_user.model.UnoverMessage_;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;
import com.sicong.smartstore.util.objectBox.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

public class UnoverActivity extends AppCompatActivity {

    //常量
    private final static  String TAG = "UnoverActivity";
    private UnoverActivity self = UnoverActivity.this;
    private static final int UNOVER_SUCCESS = 1;
    private static final int UNOVER_FAIL = 2;
    private static final int UNOVER_ERROR = 3;

    //数据
    private List<UnoverMessage> userUnoverList;
    private String username;
    private String check;
    private String company;

    private Box<UnoverMessage> unoverMessageBox;
    private Query<UnoverMessage> unoverMessageQuery;

    //控件
    private Handler handler;
    private RecyclerView userUnoverListView;

    //适配器
    private UnoverListAdapter unoverListAdapter;


    //线程
    private Thread dataThread;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unover);
        initNetBoardcastReceiver();//初始化网络广播
        initView();//初始化控件
        initHandler();//初始化Handler
        initReceive();//接受上个Activity的传值
        initList();//初始化RecyleView
    }


    /**
     * 初始化RecycleView
     */
    private void initList() {
        userUnoverList = new ArrayList<UnoverMessage>();

        unoverListAdapter = new UnoverListAdapter(getBaseContext(), userUnoverList, check, company, username);
        userUnoverListView.setAdapter(unoverListAdapter);
        userUnoverListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        userUnoverListView.setHasFixedSize(true);
        userUnoverListView.setItemAnimator(new DefaultItemAnimator());
        userUnoverListView.addItemDecoration(new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));

    }

    /**
     * 启动线程，获取数据
     */
    public void startDataRequestThread(){
        dataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //第一步：首先从数据库中获取数据
                    BoxStore boxStore = ((App)getApplication()).getBoxStore();
                    unoverMessageBox = boxStore.boxFor(UnoverMessage.class);
                    unoverMessageQuery = unoverMessageBox.query().order(UnoverMessage_.id).build();

                    List<UnoverMessage> list = unoverMessageQuery.find();

                    //第二步：更换变量中原有数据
                    userUnoverList = new ArrayList<UnoverMessage>();
                    userUnoverList.clear();
                    userUnoverList.addAll(list);

                    //第三步：判断数量的多少
                    if(userUnoverList.size() < 1 ){
                        handler.sendEmptyMessage(UNOVER_FAIL);
                    }else{
                        handler.sendEmptyMessage(UNOVER_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(UNOVER_ERROR);
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
        userUnoverListView = (RecyclerView) findViewById(R.id.user_unover_rv);
    }


    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case UNOVER_ERROR:
                        Toast.makeText(self, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                        break;
                    case UNOVER_FAIL:
                        Toast.makeText(self, "不存在未完成的单号", Toast.LENGTH_SHORT).show();
                        break;
                    case UNOVER_SUCCESS:
                        unoverListAdapter.notifyDataSetChanged();
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
