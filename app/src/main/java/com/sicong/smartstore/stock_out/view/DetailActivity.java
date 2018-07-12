package com.sicong.smartstore.stock_out.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.data.model.CheckMessage;
import com.sicong.smartstore.stock_in.view.ScanActivity;
import com.sicong.smartstore.stock_out.adapter.DetailScanAdapter;
import com.sicong.smartstore.stock_out.adapter.DetailStockOutAdapter;
import com.sicong.smartstore.stock_out.model.CargoListSendMessage;
import com.sicong.smartstore.stock_out.model.ClientInfo;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class DetailActivity extends AppCompatActivity {

    //常量
    private final static String TAG = "DetailActivity";
    private String model = "U6";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int CLIENT_INFO_SUCCESS = 1;
    private static final int CLIENT_INFO_FAIL = 2;
    private static final int CLIENT_INFO_ERROR = 3;

    private static final int DETAIL_SUCCESS = 4;
    private static final int DETAIL_FAIL = 5;
    private static final int DETAIL_ERROR = 6;

    private static final int SUBMIT_SUCCESS = 7;
    private static final int SUBMIT_FAIL = 8;
    private static final int SUBMIT_ERROR = 9;

    //数据
    private List<Map<String, String>> detailStockOutList;

    private String check;
    private String company;
    private String username;

    private String id;
    private String name;
    private String phoneNumber;
    private String address;
    private String title;
    private String description;
    private String idFromIntent;

    private IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps;//已扫描RFID集合：已扫描过的rfid码，避免重复
    private List<String> rfidList;//货物对象集合：扫描的所有物品的集合

    //视图
    private RecyclerView detailStockOutView;
    private RecyclerView scanInfoView;
    private AppCompatButton btnStart;
    private AppCompatButton btnStop;
    private AppCompatButton btnReset;
    private AppCompatButton btnSubmit;

    private TextView idView;
    private TextView nameView;
    private TextView phoneNumberView;
    private TextView addressView;
    private TextView titleView;
    private TextView descriptionView;

    private Handler handler;

    //适配器
    private DetailStockOutAdapter detailStockOutAdapter;
    private DetailScanAdapter scanInfoAdapter;

    //线程
    private Thread detailThread;//出库细节信息线程
    private Thread clientThread;//客户信息线程
    private Thread submitThread;//提交线程

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();//初始化控件
        initObject();//初始化一部分对象
        initHandler();//初始化Handler

        initDetailStockOutView();//初始化待出库物品列表
        initScanInfoView();//初始化扫描列表

        initBtnStart();//初始化开始扫描按钮
        initBtnEnd();//初始化结束扫描按钮
        initBtnReset();//初始化重置扫描按钮
        initBtnSubmit();//初始化提交按钮

    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();//注册广播
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);
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
                        Toast.makeText(DetailActivity.this, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_SUCCESS:
                        Toast.makeText(DetailActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_FAIL:
                        Toast.makeText(DetailActivity.this, "提交失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case SUBMIT_ERROR:
                        Toast.makeText(DetailActivity.this, "提交异常，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case CLIENT_INFO_SUCCESS:
                        idView.setText(id);
                        nameView.setText(name);
                        phoneNumberView.setText(phoneNumber);
                        addressView.setText(address);
                        titleView.setText(title);
                        descriptionView.setText(description);
                        break;
                    case CLIENT_INFO_FAIL:
                        Toast.makeText(DetailActivity.this, "获取用户信息失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case CLIENT_INFO_ERROR:
                        Toast.makeText(DetailActivity.this, "获取用户信息异常，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case DETAIL_SUCCESS:
                        detailStockOutAdapter.notifyDataSetChanged();
                        break;
                    case DETAIL_FAIL:
                        Toast.makeText(DetailActivity.this, "获取出库信息失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case DETAIL_ERROR:
                        Toast.makeText(DetailActivity.this, "获取出库信息异常，请稍后再试", Toast.LENGTH_SHORT).show();
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
        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();
        mUSeries.openSerialPort(model);

        Intent intent = getIntent();
        if (intent.hasExtra("check")) {
            check = intent.getStringExtra("check");
        } else if (intent.hasExtra("company")) {
            company = intent.getStringExtra("company");
        } else if (intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        } else if (intent.hasExtra("id")){
            idFromIntent = intent.getStringExtra("id");
        }
    }

    /**
     * 初始化扫描列表
     */
    private void initScanInfoView() {
        rfidList = new ArrayList<String>();
        scanInfoAdapter = new DetailScanAdapter(this, rfidList);
        scanInfoView.setAdapter(scanInfoAdapter);
        scanInfoView.setLayoutManager(new LinearLayoutManager(this));
        scanInfoView.setHasFixedSize(true);
        scanInfoView.setItemAnimator(new DefaultItemAnimator());
        scanInfoView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化待出库物品列表
     */
    private void initDetailStockOutView() {
        detailStockOutList = new ArrayList<Map<String, String>>();
        detailStockOutAdapter = new DetailStockOutAdapter(DetailActivity.this, detailStockOutList);
        detailStockOutView.setAdapter(detailStockOutAdapter);
        detailStockOutView.setLayoutManager(new LinearLayoutManager(this));
        detailStockOutView.setHasFixedSize(true);
        detailStockOutView.setItemAnimator(new DefaultItemAnimator());
        detailStockOutView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化控件
     */
    private void initView() {
        detailStockOutView = findViewById(R.id.detail_stock_out);
        scanInfoView = findViewById(R.id.detail_scan);

        btnStart = findViewById(R.id.detail_btn_start);
        btnStop = findViewById(R.id.detail_btn_stop);
        btnReset = findViewById(R.id.detail_btn_reset);
        btnSubmit = findViewById(R.id.detail_btn_submit);

        idView = findViewById(R.id.detail_id);
        nameView = findViewById(R.id.detail_name);
        phoneNumberView = findViewById(R.id.detail_phone_number);
        addressView = findViewById(R.id.detail_address);
        titleView = findViewById(R.id.detail_title);
        descriptionView = findViewById(R.id.detail_description);
    }

    /**
     * 开始扫描按钮
     */
    private void initBtnStart() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRfidCode();
            }
        });
    }

    /**
     * 初始化结束扫描按钮
     */
    private void initBtnEnd() {
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUSeries.stopInventory();
            }
        });
    }

    /**
     * 初始化开始扫描按钮
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
                List<Tag> InventoryOnceResult = (List<Tag>) data;//一次扫描到的数据，因为不排除扫描到周围其他物体的可能性，故用数组接收结果，但是数组内部已做好对其他数组的过滤

                //对扫描结果进行筛选
                for (int i = 0; i < InventoryOnceResult.size(); i++) {
                    Tag map = InventoryOnceResult.get(i);

                    if (!InventoryTaps.contains(map.epc)) {//避免RFID码重复扫入
                        //若RFID不重复，则将扫描到的RFID码放入“已扫描RFID集合”
                        InventoryTaps.add(map.epc);

                        //更新视图
                        scanInfoAdapter.insert(map.epc);

                    } else {

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
     * 初始化重置扫描按钮
     */
    private void initBtnReset() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUSeries.stopInventory();
                scanInfoAdapter.clear();
                InventoryTaps.clear();
            }
        });
    }

    /**
     * 初始化提交扫描按钮
     */
    private void initBtnSubmit() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(DetailActivity.this)) {
                    startSubmitThread();
                } else {
                    handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
                }
            }
        });
    }




    /**
     * 启动客户信息线程
     */
    private void startClientThread() {

        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.w(TAG, "run: clientInfo", null);
                    //发送的信息
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("company", company);
                    msg.put("username", username);
                    msg.put("id", idFromIntent);

                    //用于接收的对象
                    Map<String, String> map = new HashMap<String, String>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    map = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_OUT_CLIENT_INFO), msg, map.getClass());

                    //处理请求的数据
                    id = map.get("id");
                    name = map.get("name");
                    phoneNumber = map.get("phoneNumber");
                    address = map.get("address");
                    title = map.get("title");
                    description = map.get("description");

                    if (map != null) {
                        handler.sendEmptyMessage(CLIENT_INFO_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(CLIENT_INFO_FAIL);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(CLIENT_INFO_ERROR);
                }
            }
        });
        clientThread.start();
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
                    msg.put("company", company);

                    //用于接收的对象
                    List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_OUT_DETAIL), msg, maps.getClass());

                    //处理请求的数据
                    if (maps != null && maps.size()>0) {
                        detailStockOutList.clear();
                        detailStockOutList.addAll(maps);
                        handler.sendEmptyMessage(DETAIL_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(DETAIL_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(DETAIL_ERROR);
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
                        startClientThread();
                    } else {
                        Toast.makeText(DetailActivity.this, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, filter);
    }
}
