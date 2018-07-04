package com.sicong.smartstore.stock_in.view;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.adapter.ScanInfoAdapter;
import com.sicong.smartstore.util.fn.u6.model.Message;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_in.data.model.InventoryResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ScanFragment";

    private String model = "U6";
    private String URL_POST_SCAN_RESULTS = "";
    private static final String URL_GET_TYPE_FIST = "";
    private static final String URL_GET_TYPE_SECOND = "";

    private AppCompatButton startScan;//开始扫描按钮
    private AppCompatButton stopScan;//停止扫描按钮
    private AppCompatButton resetScan;//重置按钮
    private AppCompatButton submitScan;//提交按钮

    private View typeViewFirst;//一级类型选择器的布局视图
    private View typeViewSecond;//二级类型选择器的布局视图
    private Spinner chooseTypeFirst;//一级类型选择器
    private Spinner chooseTypeSecond;//二级类型选择器

    private RecyclerView scanInfoView;//扫描信息的列表

    private ScanInfoAdapter scanInfoAdapter;//扫描信息的列标的适配器
    ArrayAdapter<String> typeAdapterFirst;//一级类型适配器
    ArrayAdapter<String> typeAdapterSecond;//二级类型适配器

    public IUSeries mUSeries;//扫描工具
    List<String> InventoryTaps = new ArrayList<String>();//已扫描RFID集合：已扫描过的rfid码，避免重复
    List<Cargo> cargos = new ArrayList<Cargo>();//货物对象集合：扫描的所有物品的集合
    String[] typesFirst = null;//一级类型数组
    String[] typesSecond = null;//二级类型数据


    public ScanActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();//初始化控件
        initUSeries();//初始化所需对象

        initStartScan();//初始化开始扫描
        initStopScan();//初始化停止扫描
        initResetScan();//初始化重置扫描
        initSubmitScan();//初始化提交扫描

        initChooseType();//初始化类型选择器
        initScanInfo();//初始化扫描信息视图
    }

    /**
     * 初始化控件
     *
     */
    private void initView() {
        startScan = (AppCompatButton) findViewById(R.id.scan_btn_start);
        stopScan = (AppCompatButton) findViewById(R.id.scan_btn_stop);
        resetScan = (AppCompatButton) findViewById(R.id.scan_btn_reset);
        submitScan = (AppCompatButton) findViewById(R.id.scan_btn_submit);

        typeViewFirst = (View)findViewById(R.id.scan_type_first);
        typeViewSecond = (View)findViewById(R.id.scan_type_second);

        chooseTypeFirst = (Spinner) typeViewFirst.findViewById(R.id.item_choose_type);
        chooseTypeSecond = (Spinner)typeViewSecond.findViewById(R.id.item_choose_type);

        scanInfoView = (RecyclerView) findViewById(R.id.scan_info_view);
    }

    /**
     * 初始化重置扫描
     */
    private void initResetScan() {
        resetScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }


    /**
     * 初始化提交扫描
     */
    private void initSubmitScan() {
        submitScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    /**
     * 初始化扫描信息视图
     */
    private void initScanInfo() {
        scanInfoAdapter = new ScanInfoAdapter(this, cargos);
        scanInfoView.setAdapter(scanInfoAdapter);
        scanInfoView.setLayoutManager(new LinearLayoutManager(this));
        scanInfoView.setHasFixedSize(true);
        scanInfoView.setItemAnimator(new DefaultItemAnimator());
        scanInfoView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化类型选择器
     */
    private void initChooseType() {
        typesFirst = getTypesFirst();
        typesSecond = getTypesSecond();

        if (typesFirst != null) {
            typeAdapterFirst = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typesFirst);
            typeAdapterFirst.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chooseTypeFirst.setAdapter(typeAdapterFirst);
            chooseTypeFirst.setOnItemSelectedListener(this);
        } else {
            //如果数据为空，如何处理
        }

        if (typesSecond != null) {
            typeAdapterSecond = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typesSecond);
            typeAdapterSecond.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chooseTypeSecond.setAdapter(typeAdapterSecond);
            chooseTypeSecond.setOnItemSelectedListener(this);
        } else {
            //如果数据为空，如何处理
        }
    }

    /**
     * 初始化扫描工具
     */
    private void initUSeries() {
        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();

        /*Message openSerialPortMessage = */mUSeries.openSerialPort(model);
    }



    /**
     * 初始化开始扫描按钮
     */
    private void initStartScan() {
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRfidCode();
            }
        });

    }

    /**
     * 初始化停止扫描按钮
     */
    private void initStopScan() {
        stopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRfidCode();
            }
        });
    }


    /**
     * 开始扫描
     */
    public void getRfidCode() {

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
                String typeFirst = "类型1";//获取一级类型Spinner中的数据
                String typeSecond = "类型2";//获取二级类型Spinner中的数据


                //对扫描结果进行筛选
                for (int i = 0; i < InventoryOnceResult.size(); i++) {
                    Tag map = InventoryOnceResult.get(i);
                    Log.e(TAG, "onSuccess: " + map.epc, null);

                    if (!InventoryTaps.contains(map.epc)) {//避免RFID码重复扫入
                        //若RFID不重复，则将扫描到的RFID码放入“已扫描RFID集合”
                        InventoryTaps.add(map.epc);

                        //创建新的货物对象
                        Cargo cargo = new Cargo();

                        cargo.setTypeFirst(typeFirst);
                        cargo.setTypeSecond(typeSecond);
                        cargo.setRfid(map.epc);

                        //更新视图
                        scanInfoAdapter.insert(cargo);

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
     * 停止扫描
     */
    public void stopRfidCode() {
        Log.e(TAG, "stopRfidCode", null);
        mUSeries.stopInventory();

    }

    /**
     * 提交扫描结果
     */
    public void submit() {
        mUSeries.stopInventory();
        /*if (cargos != null && cargos.size() > 0) {
            toDescribeActivity(cargos, MainActivity.operatorId);
        } else {
            Toast.makeText(this, "无扫描结果，无法提交", Toast.LENGTH_SHORT).show();
        }*/

        if (cargos != null && cargos.size() > 0) {
            toMainActivity(cargos);
        } else {
            Toast.makeText(this, "无扫描结果，无法提交", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重置扫描
     */
    private void reset() {
        stopRfidCode();
        scanInfoAdapter.clear();
        InventoryTaps.clear();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * 获取货物种类
     * @return 货物种类的数组
     */
    public String[] getTypesFirst() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String result = restTemplate.getForObject(URL_GET_TYPE_FIST, String.class);
            JSONObject jsonResult = new JSONObject(result);
            JSONArray jsonTypeArray = jsonResult.getJSONArray("typesFirst");
            String[] types = new String[jsonTypeArray.length()];
            for (int i = 0; i < jsonTypeArray.length(); i++) {
                types[i] = jsonTypeArray.getString(i);
            }
            return types;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "获取产品列表失败", Toast.LENGTH_SHORT);
        }
        return null;
    }

    /**
     * 获取货物种类
     * @return 货物种类的数组
     */
    public String[] getTypesSecond() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String result = restTemplate.getForObject(URL_GET_TYPE_SECOND, String.class);
            JSONObject jsonResult = new JSONObject(result);
            JSONArray jsonTypeArray = jsonResult.getJSONArray("typesSecond");
            String[] types = new String[jsonTypeArray.length()];
            for (int i = 0; i < jsonTypeArray.length(); i++) {
                types[i] = jsonTypeArray.getString(i);
            }
            return types;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "获取产品列表失败", Toast.LENGTH_SHORT);
        }
        return null;
    }

    /**
     * 跳转至MainActivity
     * 将扫描结果发送消息给MainActivity
     * @param cargos 扫描后的货物数组
     */
    public void toMainActivity(List<Cargo> cargos) {
        if(cargos==null || cargos.size()>0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("cargos", (Serializable) cargos);
            startActivity(intent);
        }
        this.overridePendingTransition(R.anim.activity_in_right, R.anim.activity_out_left);
    }

    /*class postScanResults implements Runnable {

        @Override
        public void run() {
            RestTemplate restTemplate = new RestTemplate();
            try {
                restTemplate.postForObject(URL_POST_SCAN_RESULTS, inventoryResults, String.class);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(SUBMIT_FAIL);
            }
        }
    }*/
}


