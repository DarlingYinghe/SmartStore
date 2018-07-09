package com.sicong.smartstore.stock_out.view;

import android.content.Intent;
import android.location.Address;
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
import com.sicong.smartstore.stock_out.adapter.DetailScanAdapter;
import com.sicong.smartstore.stock_out.adapter.DetailStockOutAdapter;
import com.sicong.smartstore.stock_out.model.CargoListSendMessage;
import com.sicong.smartstore.stock_out.model.ClientInfo;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;

import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class DetailActivity extends AppCompatActivity {

    //基本变量
    private final static String TAG = "DetailActivity";
    private static final int SUBMIT_ERROR = -1;
    private static final int SUBMIT_FAIL = 0;
    private static final int SUBMIT_SUCCESS = 1;
    private static final int NETWORK_UNAVAILABLE = -2;
    private static final String URL_SUBMIT = "";
    private static final String URL_CLIENT_INFO = "";
    private static final int CLIENT_INFO_SUCCESS = 4;
    private static final int CLIENT_INFO_FAIL = 3;
    private static final int CLIENT_INFO_ERROR = 2;

    private String model = "U6";
    //数据
    private CargoListSendMessage cargoListSendMessage;
    private List<Map<String,Object>> detailStockOutList;
    private CheckMessage checkMessage;
    public IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps;//已扫描RFID集合：已扫描过的rfid码，避免重复
    private List<String> rfidList;//货物对象集合：扫描的所有物品的集合
    private ClientInfo clientInfo;//客户信息

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

    private Handler handler;
    
    //适配器
    private DetailStockOutAdapter detailStockOutAdapter;
    private DetailScanAdapter scanInfoAdapter;


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
        initClientInfoView();//初始化用户信息视图
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
                        Toast.makeText(DetailActivity.this, "请连接网络", Toast.LENGTH_SHORT).show();
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
                        idView.setText(clientInfo.getId());
                        nameView.setText(clientInfo.getName());
                        phoneNumberView.setText(clientInfo.getPhoneNumber());
                        addressView.setText(clientInfo.getAddress());
                        break;
                    case CLIENT_INFO_FAIL:
                        Toast.makeText(DetailActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                        break;
                    case CLIENT_INFO_ERROR:
                        Toast.makeText(DetailActivity.this, "获取用户信息异常", Toast.LENGTH_SHORT).show();
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
        String check = intent.getStringExtra("check");
        checkMessage = new CheckMessage(check);
    }

    /**
     * 初始化扫描列表
     */
    private void initScanInfoView() {
        rfidList  = new ArrayList<String>();
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
        detailStockOutList = new ArrayList<Map<String,Object>>();
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
    }

    /**
     * 开始扫描按钮
     */
    private void initBtnStart(){
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
               if(isNetworkAvailable(DetailActivity.this)) {
                    try{

                        Thread submitThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                RestTemplate restTemplate = new RestTemplate();
                                //restTemplate.postForObject(URL_SUBMIT,);
                            }
                        });
                        submitThread.start();

                    }catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(SUBMIT_ERROR);
                    }
               } else {
                   handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
               }
           }
       });
    }

    /**
     * 初始化用户信息视图
     */
    private void initClientInfoView() {
        if(isNetworkAvailable(DetailActivity.this)) {
            try{

                Thread submitThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RestTemplate restTemplate = new RestTemplate();
                        clientInfo = restTemplate.postForObject(URL_CLIENT_INFO, checkMessage, ClientInfo.class);
                        if(clientInfo!=null) {
                            handler.sendEmptyMessage(CLIENT_INFO_SUCCESS);
                        } else {
                            handler.sendEmptyMessage(CLIENT_INFO_FAIL);
                        }
                    }
                });
                submitThread.start();

            }catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(CLIENT_INFO_ERROR);
            }
        } else {
            handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
    }
}
