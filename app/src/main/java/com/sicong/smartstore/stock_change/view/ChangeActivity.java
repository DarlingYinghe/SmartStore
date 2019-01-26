package com.sicong.smartstore.stock_change.view;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_change.adapter.ChangeDetailAdapter;
import com.sicong.smartstore.stock_change.adapter.ChangeScanAdapter;
import com.sicong.smartstore.stock_out.view.OutActivity;
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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class ChangeActivity extends AppCompatActivity {

    //常量
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "ChangeActivity";
    private String model = "U6";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int DETAIL_SUCCESS = 1;
    private static final int DETAIL_FAIL = 2;
    private static final int DETAIL_ERROR = 3;

    private static final int SUBMIT_SUCCESS = 4;
    private static final int SUBMIT_FAIL = 5;
    private static final int SUBMIT_ERROR = 6;

    private static final int WAREHOUSE_SUCCESS = 7;
    private static final int WAREHOUSE_FAIL = 8;
    private static final int WAREHOUSE_ERROR = 9;

    private static final int LOCATION_SUCCESS = 10;
    private static final int LAYER_SUCCESS = 11;

    //视图
    private RecyclerView detailStockChangeView;
    private RecyclerView scanInfoView;
    private AppCompatButton btnStart;
    private AppCompatButton btnStop;
    private AppCompatButton btnReset;
    private AppCompatButton btnSubmit;
    private TextView changeFrom;
    private TextView changeTo;
    private TextView areaText;
    private TextView shelfText;

    private View areaView;
    private View shelfView;
    private View layerView;
    private TextView areaName;
    private TextView shelfName;
    private TextView layerName;
    private Spinner spinnerArea;
    private Spinner spinnerShelf;
    private Spinner spinnerLayer;


    private CoordinatorLayout snackbarContainer;//Snackbar的容器


    private Handler handler;

    //数据
    private List<Map<String, Object>> detailMaps;
    private List<Map<String, String>> scanMaps;
    private List<Map<String, String>> scanDatas;

    private String check;
    private String company;
    private String username;

    private int curItem;

    private String idFromIntent;

    private IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps;//已扫描RFID集合：已扫描过的rfid码，避免重复

    private String toWareHouse;
    private String toWareHouseNum;
    private String fromWareHouse;

    private String area;
    private String shelf;
    private String layer;
    private List<String> shelfList;
    private List<String> shelfNumList;
    private List<String> areaList;
    private List<String> areaNumList;
    private List<String> layerList;
    private List<String> layerNumList;

    //适配器
    private ChangeDetailAdapter changeDetailAdapter;
    private ChangeScanAdapter scanInfoAdapter;

    private ArrayAdapter<String> areaAdapter;//货区名称适配器
    private ArrayAdapter<String> shelfAdapter;//货架名称适配器
    private ArrayAdapter<String> layerAdapter;//货架名称适配器

    //线程
    private Thread detailThread;
    private Thread submitThread;
    private Thread wareHouseThread;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        initView();//初始化控件
        initObject();//初始化一部分对象
        initHandler();//初始化Handler

        initDetailStockChangeView();//初始化待出库物品列表
        initScanInfoView();//初始化扫描列表

        initBtnStart();//初始化开始扫描按钮
        initBtnStop();//初始化结束扫描按钮
        initBtnReset();//初始化重置扫描按钮
        initBtnSubmit();//初始化提交按钮

        initBtnStatus();//初始化按钮状态

        initSpinner();
        initText();

    }

    private void initText() {
        areaName.setText(R.string.area);
        layerName.setText(R.string.layer);
        shelfName.setText(R.string.shelf);
    }

    private void initSpinner() {
        shelfList = new ArrayList<>();
        areaList = new ArrayList<>();
        layerList = new ArrayList<>();

        //初始化货区名称选择器
        areaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, areaList);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(areaAdapter);
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                area = areaList.get(position);
                Log.e(TAG, "onItemSelected: area is " + area, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //初始化货架名称选择器
        shelfAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shelfList);
        shelfAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShelf.setAdapter(shelfAdapter);
        spinnerShelf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shelf = shelfList.get(position);
                Log.e(TAG, "onItemSelected: shelf is " + shelf, null);
                getLarerListThread();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //初始化货架内层名称选择器
        layerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, layerList);
        layerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLayer.setAdapter(layerAdapter);
        spinnerLayer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layer = layerList.get(position);
                Log.e(TAG, "onItemSelected: layer is " + layerNumList.get(position), null);
                getLarerListThread();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case NETWORK_UNAVAILABLE:
                        Snackbar.make(snackbarContainer, "请连接网络", Snackbar.LENGTH_SHORT).show();
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
                    case DETAIL_SUCCESS:
                        changeDetailAdapter.notifyDataSetChanged();
                        break;
                    case DETAIL_FAIL:
                        Snackbar.make(snackbarContainer, "获取出库信息失败", Snackbar.LENGTH_SHORT).show();
                        break;
                    case DETAIL_ERROR:
                        Snackbar.make(snackbarContainer, "获取出库信息异常", Snackbar.LENGTH_SHORT).show();
                        break;
                    case WAREHOUSE_SUCCESS:
                        changeFrom.setText(fromWareHouse);
                        changeTo.setText(toWareHouse);
                        getAreaAndShelfListThread();
                        break;
                    case LOCATION_SUCCESS:
                        areaAdapter.notifyDataSetChanged();
                        spinnerArea.setSelection(0, true);
                        shelfAdapter.notifyDataSetChanged();
                        spinnerShelf.setSelection(0, true);
                        break;
                    case LAYER_SUCCESS:
                        layerAdapter.notifyDataSetChanged();
                        spinnerLayer.setSelection(0, true);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initView() {
        detailStockChangeView = findViewById(R.id.change_detail_rv);
        scanInfoView = findViewById(R.id.change_scan);

        btnStart = findViewById(R.id.change_btn_start);
        btnStop = findViewById(R.id.change_btn_stop);
        btnReset = findViewById(R.id.change_btn_reset);
        btnSubmit = findViewById(R.id.change_btn_submit);

        changeFrom = findViewById(R.id.change_from);
        changeTo = findViewById(R.id.change_to);

        snackbarContainer = findViewById(R.id.change_scan_snackbar_container);


        areaView = findViewById(R.id.change_spinner_area);
        shelfView = findViewById(R.id.change_spinner_shelf);
        layerView = findViewById(R.id.change_spinner_layer);

        shelfName = shelfView.findViewById(R.id.item_choose_tv);
        areaName = areaView.findViewById(R.id.item_choose_tv);
        layerName = layerView.findViewById(R.id.item_choose_tv);
        spinnerArea = areaView.findViewById(R.id.item_choose_type);
        spinnerShelf = shelfView.findViewById(R.id.item_choose_type);
        spinnerLayer = layerView.findViewById(R.id.item_choose_type);
    }

    /**
     * 初始化一部分对象
     */
    private void initObject() {
        curItem = -1;

        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();
        mUSeries.openSerialPort(model);
        scanDatas = new ArrayList<>();

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
        if (intent.hasExtra("id")) {
            idFromIntent = intent.getStringExtra("id");
        }
    }

    /**
     * 开始扫描按钮
     */
    private void initBtnStart() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: start", null);
                curItem = changeDetailAdapter.getCurItem();
                Log.e(TAG, "onClick: curItem is " + curItem, null);

                if (curItem != -1) {
                    setBtnStatus(false, true, true, true);
                    getRfidCode();
                    startScanThread();
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
        if (InventoryTaps != null)
            InventoryTaps.clear();
    }

    /**
     * 初始化提交扫描按钮
     */
    private void initBtnSubmit() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(ChangeActivity.this)) {
                    if (checkResult()) {
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
            int num = Integer.valueOf(detailMaps.get(i).get("num").toString());
            int count = (Integer) detailMaps.get(i).get("count");
            if (num == count) {
                n++;
            }
        }
        if (n == detailMaps.size()) {
            return true;
        }
        return false;
    }


    /**
     * 初始化按钮状态
     */
    private void initBtnStatus() {
        setBtnStatus(true, false, false, false);
    }

    /**
     * 初始化扫描列表
     */
    private void initScanInfoView() {
        scanMaps = new ArrayList<Map<String, String>>();
        scanInfoAdapter = new ChangeScanAdapter(this, scanMaps);
        scanInfoView.setAdapter(scanInfoAdapter);
        scanInfoView.setLayoutManager(new LinearLayoutManager(this));
        scanInfoView.setHasFixedSize(true);
        scanInfoView.setItemAnimator(new DefaultItemAnimator());
        scanInfoView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化待出库物品列表
     */
    private void initDetailStockChangeView() {
        detailMaps = new ArrayList<Map<String, Object>>();
        changeDetailAdapter = new ChangeDetailAdapter(ChangeActivity.this, detailMaps);
        detailStockChangeView.setAdapter(changeDetailAdapter);
        detailStockChangeView.setLayoutManager(new LinearLayoutManager(this));
        detailStockChangeView.setHasFixedSize(true);
        detailStockChangeView.setItemAnimator(new DefaultItemAnimator());
        detailStockChangeView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        startDetailThread();
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
                if (!((boolean) changeDetailAdapter.getmList().get(curItem).get("over"))) {

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
                            Log.e("Change is:", detailMap.toString(), null);

                            if (scanDatas.get(curItem).containsKey("rfids")) {
                                List<Map> scanRfids = parseArray(scanDatas.get(curItem).get("rfids"), Map.class);
                                Map temp = new HashMap();
                                temp.put("rfid", map.epc);
                                scanRfids.add(temp);
                                scanDatas.get(curItem).put("rfids", toJSONString(scanRfids));
                                Log.e(TAG, scanDatas.toString(), null);
                            } else {
                                List<Map<String, String>> list = new ArrayList<>();
                                Map<String, String> temp = new HashMap<>();
                                temp.put("rfid", map.epc);
                                list.add(temp);
                                scanDatas.get(curItem).put("rfids", toJSONString(list));
                                Log.e(TAG, scanDatas.toString(), null);
                            }

                            Log.e(TAG, scanDatas.toString(), null);

                            Integer count = (Integer) detailMap.get("count");
                            count++;
                            detailMap.put("count", count);
                            detailMaps.set(curItem, detailMap);

                            //更新视图
                            scanInfoAdapter.insert(scanMap);
                            changeDetailAdapter.changeCurItemCount(curItem);

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
     * 启动提交线程
     */
    private void startSubmitThread() {
        submitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: submit", null);
                try {
                    //发送的信息
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("companyId", company);
                    msg.put("username", username);
                    msg.put("id", idFromIntent);
                    msg.put("items", toJSONString(scanDatas));
                    Log.e(TAG, msg.toString(), null);

                    List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_CHECK_SUBMIT), msg, maps.getClass());
                    Log.e(TAG, maps.toString(), null);

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
     * 开始扫描线程
     */
    private void startScanThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();

                    Map<String, String> msg = new HashMap<>();
                    msg.put("check", check);
                    msg.put("username", username);
                    msg.put("companyId", company);


                    RequestBody requestBody = RequestBody.create(JSON, toJSONString(msg));

                    Request request = new Request.Builder()
                            .post(requestBody)
                            .url(getResources().getString(R.string.URL_STOCK_CHANGE_START))
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();

                    Log.e(TAG, "this is start:" + result, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 启动详细信息线程
     */
    private void startDetailThread() {
        scanDatas.clear();
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
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_CHANGE_DETAIL), msg, maps.getClass());
                    Log.e(TAG, maps.toString(), null);

                    //处理请求的数据
                    if (maps != null && maps.size() > 0) {
                        for (int i = 0; i < maps.size(); i++) {
                            Map<String, Object> mapTmp = maps.get(i);
                            mapTmp.put("count", 0);
                            mapTmp.put("over", false);
                            maps.set(i, mapTmp);

                            Map<String, String> map = new HashMap<>();
                            map.put("item_id", mapTmp.get("item_id").toString());
                            scanDatas.add(map);
                        }
                        detailMaps.clear();
                        detailMaps.addAll(maps);
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
     * 启动仓库详情线程
     */
    private void startWareHouseDetailThread() {
        wareHouseThread = new Thread(new Runnable() {
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
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_CHANGE_WAREHOUSE_DETAIL), msg, maps.getClass());
                    Log.e(TAG, maps.toString(), null);

                    fromWareHouse = maps.get(0).get("from_warehouse_name").toString();
                    toWareHouseNum = maps.get(0).get("to_warehouse_id").toString();
                    toWareHouse = maps.get(0).get("to_warehouse_name").toString();

                    handler.sendEmptyMessage(WAREHOUSE_SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        wareHouseThread.start();
    }

    /**
     * 获取仓库内货区列表、货区列表
     */
    private void getAreaAndShelfListThread() {
        shelfNumList = new ArrayList<>();
        areaNumList = new ArrayList<>();
        areaList.clear();
        shelfList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();

                    Map<String, String> msg = new HashMap<>();
                    msg.put("check", check);
                    msg.put("username", username);
                    msg.put("companyId", company);
                    msg.put("warehouseId", toWareHouseNum);

                    RequestBody requestBody = RequestBody.create(JSON, toJSONString(msg));

                    Request request = new Request.Builder()
                            .post(requestBody)
                            .url(getResources().getString(R.string.URL_AREA_SHELF_LIST))
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();

                    Log.e(TAG, "this is start:" + result, null);
                    List<Map> areas = parseArray(parseObject(result).get("areas").toString(), Map.class);
                    List<Map> shelfs = parseArray(parseObject(result).get("shelfs").toString(), Map.class);
                    if (shelfs.size() < 1 && areas.size() < 1) {

                    } else {
                        for (int i = 0; i < areas.size(); i++) {
                            Map map = areas.get(i);
                            if (map.get("is_locked").toString().equals("false")) {
                                areaList.add(map.get("no").toString());
                                areaNumList.add(map.get("id").toString());
                            }
                        }
                        for (int i = 0; i < shelfs.size(); i++) {
                            Map map = shelfs.get(i);
                            if (map.get("is_locked").toString().equals("false")) {
                                shelfList.add(map.get("no").toString());
                                shelfNumList.add(map.get("id").toString());
                            }
                        }

                        handler.sendEmptyMessage(LOCATION_SUCCESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 货架内层列表
     */
    private void getLarerListThread() {
        layerNumList = new ArrayList<>();
        layerList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();

                    Map<String, String> msg = new HashMap<>();
                    msg.put("check", check);
                    msg.put("username", username);
                    msg.put("companyId", company);
                    msg.put("warehouseId", toWareHouseNum);
                    msg.put("shelfId", shelfNumList.get(spinnerShelf.getSelectedItemPosition()));

                    RequestBody requestBody = RequestBody.create(JSON, toJSONString(msg));

                    Request request = new Request.Builder()
                            .post(requestBody)
                            .url(getResources().getString(R.string.URL_LARER))
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    List<Map> list = parseArray(result, Map.class);


                    if (list.size() < 1) {

                    } else {
                        for (int i = 0; i < list.size(); i++) {
                            Map map = list.get(i);
                            layerList.add(map.get("num").toString());
                            layerNumList.add(map.get("id").toString());
                        }
                        handler.sendEmptyMessage(LAYER_SUCCESS);
                    }

                    Log.e(TAG, "this is getLarerListThread:" + result, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                    if (status) {
                        startDetailThread();
                        startWareHouseDetailThread();
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
    private void setBtnStatus(boolean start, boolean stop, boolean reset, boolean submit) {
        btnStart.setEnabled(start);
        btnStop.setEnabled(stop);
        btnReset.setEnabled(reset);
        btnSubmit.setEnabled(submit);
    }
}
