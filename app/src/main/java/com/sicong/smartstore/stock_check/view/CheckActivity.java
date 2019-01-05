package com.sicong.smartstore.stock_check.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_check.adapter.CheckDetailAdapter;
import com.sicong.smartstore.stock_check.adapter.CheckScanAdapter;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class CheckActivity extends AppCompatActivity {
    //常量
    private static final String TAG = "CheckActivity";
    private String model = "U6";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int check_SUCCESS = 1;
    private static final int check_FAIL = 2;
    private static final int check_ERROR = 3;

    private static final int SUBMIT_SUCCESS = 4;
    private static final int SUBMIT_FAIL = 5;
    private static final int SUBMIT_ERROR = 6;

    //视图
    private RecyclerView detailStockCheckView;
    private RecyclerView scanInfoView;
    private AppCompatButton btnStart;
    private AppCompatButton btnStop;
    private AppCompatButton btnReset;
    private AppCompatButton btnSubmit;

    private CoordinatorLayout snackbarContainer;//Snackbar的容器

    private Handler handler;

    //数据
    private List<Map<String, Object>> detailMaps;
    private List<Map<String, String>> scanMaps;

    private String check;
    private String company;
    private String username;

    private int curItem;

    private String idFromIntent;

    private IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps;//已扫描RFID集合：已扫描过的rfid码，避免重复

    //适配器
    private CheckDetailAdapter checkDetailAdapter;
    private CheckScanAdapter scanInfoAdapter;

    //线程
    private Thread detailThread;
    private Thread submitThread;


    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        initView();//初始化控件
        initObject();//初始化一部分对象
        initHandler();//初始化Handler

        initDetailStockCheckView();//初始化待出库物品列表
        initScanInfoView();//初始化扫描列表

        initBtnStart();//初始化开始扫描按钮
        initBtnStop();//初始化停止扫描按钮
        initBtnReset();//初始化重置扫描按钮
        initBtnSubmit();//初始化提交按钮

        initBtnStatus();//初始化按钮状态
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUSeries.modulePowerOn(model);
        initNetBoardcastReceiver();//注册广播
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);
        setBtnStatus(true, false, false, false);
        reset();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        detailStockCheckView = findViewById(R.id.check_detail_rv);
        scanInfoView = findViewById(R.id.check_scan);

        btnStart = findViewById(R.id.check_btn_start);
        btnStop = findViewById(R.id.check_btn_stop);
        btnReset = findViewById(R.id.check_btn_reset);
        btnSubmit = findViewById(R.id.check_btn_submit);

        snackbarContainer = findViewById(R.id.check_scan_snackbar_container);

    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case NETWORK_UNAVAILABLE:
                        Snackbar.make(snackbarContainer, "无可用的网络，请连接网络", Snackbar.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_SUCCESS:
                        Snackbar.make(snackbarContainer, "提交成功", Snackbar.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_FAIL:
                        Snackbar.make(snackbarContainer, "提交失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_ERROR:
                        Snackbar.make(snackbarContainer, "提交异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case check_SUCCESS:
                        checkDetailAdapter.notifyDataSetChanged();
                        break;
                    case check_FAIL:
                        Snackbar.make(snackbarContainer, "获取出库信息失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case check_ERROR:
                        Snackbar.make(snackbarContainer, "获取出库信息异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;

                }
                return false;
            }
        });
    }

    /**
     * 初始化一部分对象
     */
    private void initObject() {
        curItem = -1;

        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();
        mUSeries.openSerialPort(model);

        Intent intent = getIntent();
        if (intent.hasExtra("check")) {
            check = intent.getStringExtra("check");
        }
        if (intent.hasExtra("company")) {
            company = intent.getStringExtra("company");
        }
        if (intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        }
        if (intent.hasExtra("id")){
            idFromIntent = intent.getStringExtra("id");
        }
    }

    /**
     * 初始化开始扫描按钮
     */
    private void initBtnStart() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: start", null);
                curItem = checkDetailAdapter.getCurItem();
                Log.e(TAG, "onClick: curItem is "+curItem, null);

                if(curItem!=-1) {
                    setBtnStatus(false, true, true, true);
                    getRfidCode();
                } else {
                    Snackbar.make(snackbarContainer, "请选择需要扫描的条目", Snackbar.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * 初始化停止扫描按钮
     */
    private void initBtnStop() {
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: stop", null);
                setBtnStatus(true, false, true, true);
                mUSeries.stopInventory();
            }
        });
    }



    /**
     * 初始化重置扫描按钮
     */
    private void initBtnReset() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: reset", null);
                setBtnStatus(true, false, false, false);
                reset();
            }
        });
    }

    private void reset() {
        mUSeries.stopInventory();
        scanInfoAdapter.clear();
        InventoryTaps.clear();
    }

    /**
     * 初始化提交扫描按钮
     */
    private void initBtnSubmit() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(CheckActivity.this)) {
                    if(checkResult()) {
                        startSubmitThread();
                    } else {
                        Snackbar.make(snackbarContainer, "存在未扫描完成的条目，请检查", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
                }
            }
        });
    }

    private boolean checkResult() {
        int n = 0;
        for (int i = 0; i < detailMaps.size(); i++) {
            int num = Integer.valueOf((String)detailMaps.get(i).get("num"));
            int count = (Integer)detailMaps.get(i).get("count");
            if(num==count) {
                n++;
            }
        }
        if(n == detailMaps.size()) {
            return true;
        }
        return false;
    }
    /**
     * 开始扫描
     */
    public void getRfidCode() {
        InventoryTaps = new ArrayList<String>();
        mUSeries.startInventory(new ResponseHandler() {
            /**
             * 扫描成功一次，就触发一次该方法
             * 方法内通过ScanInfo对象实现“物品类型”与“RFID码”的绑定
             * @param msg
             * @param data 扫描到的数据
             * @param parameters
             */
            @Override
            public void onSuccess(String msg, Object data, byte[] parameters) {
                Log.e(TAG, "onSuccess: 启动了", null);

                if(!((boolean)checkDetailAdapter.getmList().get(curItem).get("over"))) {
                    List<Tag> InventoryOnceResult = (List<Tag>) data;//一次扫描到的数据，因为不排除扫描到周围其他物体的可能性，故用数组接收结果，但是数组内部已做好对其他数组的过滤

                    //对扫描结果进行筛选
                    for (int i = 0; i < InventoryOnceResult.size(); i++) {
                        Tag map = InventoryOnceResult.get(i);

                        if (!InventoryTaps.contains(map.epc)) {//避免RFID码重复扫入
                            //若RFID不重复，则将扫描到的RFID码放入“已扫描RFID集合”
                            InventoryTaps.add(map.epc);

                            Map<String, String> scanMap = new HashMap<String, String>();
                            scanMap.put("rfid", map.epc);

                            Map<String, Object> detailMap = detailMaps.get(curItem);
                            Integer count = (Integer) detailMap.get("count");
                            count++;
                            detailMap.put("count", count);
                            detailMaps.set(curItem, detailMap);

                            //更新视图
                            scanInfoAdapter.insert(scanMap);
                            checkDetailAdapter.changeCurItemCount(curItem);

                        } else {

                        }
                    }
                }
            }

            @Override
            public void onFailure(String msg) {
                super.onFailure(msg);
                Log.e(TAG, "onFailure", null);
            }
        });
    }

    /**
     * 初始化扫描列表
     */
    private void initScanInfoView() {
        scanMaps = new ArrayList<Map<String, String>>();
        scanInfoAdapter = new CheckScanAdapter(this, scanMaps);
        scanInfoView.setAdapter(scanInfoAdapter);
        scanInfoView.setLayoutManager(new LinearLayoutManager(this));
        scanInfoView.setHasFixedSize(true);
        scanInfoView.setItemAnimator(new DefaultItemAnimator());
        scanInfoView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化待出库物品列表
     */
    private void initDetailStockCheckView() {
        detailMaps = new ArrayList<Map<String, Object>>();
        checkDetailAdapter = new CheckDetailAdapter(CheckActivity.this, detailMaps);
        detailStockCheckView.setAdapter(checkDetailAdapter);
        detailStockCheckView.setLayoutManager(new LinearLayoutManager(this));
        detailStockCheckView.setHasFixedSize(true);
        detailStockCheckView.setItemAnimator(new DefaultItemAnimator());
        detailStockCheckView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }


    /**
     * 初始化按钮状态
     */
    private void initBtnStatus() {
        setBtnStatus(true, false, false, false);
    }


    /**
     * 启动提交线程
     */
    private void startSubmitThread() {
        submitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: submit", null);
                try {
                    //发送的信息
                    Map<String,String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("company", company);
                    msg.put("username", username);

                    //用于接收的对象
                    List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    //restTemplate.postForObject(URL_SUBMIT,check,);

                    //处理请求的数据

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(SUBMIT_ERROR);
                }
            }
        });
        submitThread.start();
    }

    /**
     * 启动详细信息线程
     */
    private void startDetailThread() {
        detailThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: detail", null);
                try {
                    //发送的信息
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("username", username);
                    msg.put("companyId", company);
                    msg.put("id", idFromIntent);

                    //用于接收的对象
                    List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_CHECK_DETAIL), msg, maps.getClass());


                    //处理请求的数据
                    if (maps != null && maps.size()>0) {
                        for (int i = 0; i < maps.size(); i++) {
                            Map<String,Object> mapTmp = maps.get(i);
                            mapTmp.put("count", 0);
                            mapTmp.put("over", false);
                            maps.set(i, mapTmp);
                        }
                        detailMaps.clear();
                        detailMaps.addAll(maps);
                        handler.sendEmptyMessage(check_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(check_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(check_ERROR);
                }
            }
        });
        detailThread.start();
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
                        startDetailThread();
                    } else {
                        Snackbar.make(snackbarContainer, "无可用的网络，请连接网络", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, filter);
    }

    /**
     * 设置四个按钮的可用性
     */
    private void setBtnStatus(boolean start, boolean stop, boolean reset, boolean submit){
        btnStart.setEnabled(start);
        btnStop.setEnabled(stop);
        btnReset.setEnabled(reset);
        btnSubmit.setEnabled(submit);
    }
}
