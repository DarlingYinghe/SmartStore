package com.sicong.smartstore.stock_in.view;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_in.adapter.ScanInfoAdapter;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_in.data.model.CargoInSendMessage;
import com.sicong.smartstore.stock_in.data.model.CheckMessage;
import com.sicong.smartstore.stock_in.data.model.Statistic;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanActivity extends AppCompatActivity {

    //基本数据
    private static final String TAG = "ScanFragment";
    private static final String URL_RECEVICE_TYPE = "";
    private static final int ERROR = -1;
    private static final int FAIL = 0;

    private String model = "U6";
    private String URL_POST_SCAN_RESULTS = "";

    //视图
    private AppCompatButton startScan;//开始扫描按钮
    private AppCompatButton stopScan;//停止扫描按钮
    private AppCompatButton resetScan;//重置按钮
    private AppCompatButton submitScan;//提交按钮

    private View typeViewFirst;//一级类型选择器的布局视图
    private View typeViewSecond;//二级类型选择器的布局视图
    private TextView textTypeFirst;//一级选择器的标题
    private TextView textTypeSecond;//二级选择器的标题
    private Spinner spinnerTypeFirst;//一级类型选择器
    private Spinner spinnerTypeSecond;//二级类型选择器

    private RecyclerView scanInfoView;//扫描信息的列表

    private Handler handler;

    //适配器
    private ScanInfoAdapter scanInfoAdapter;//扫描信息的列标的适配器
    private ArrayAdapter<String> typeFirstAdapter;//一级类型适配器
    private ArrayAdapter<String> typeSecondAdapter;//二级类型适配器

    //数据
    public IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps = new ArrayList<String>();//已扫描RFID集合：已扫描过的rfid码，避免重复
    private List<Cargo> cargos = new ArrayList<Cargo>();//货物对象集合：扫描的所有物品的集合

    private String typeFirst;
    private String typeSecond;
    private List<String> typeFirstList;
    private List<String> typeSecondList;

    private String check;
    private CargoInSendMessage cargoInSendMessage;
    private List<Map<String,Object>> typeList;//包含两级类型的集合

    public ScanActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();//初始化控件
        initReceive();//初始化接收数据
        initHandler();//初始化Handler
        initUSeries();//初始化所需对象

        initStartScan();//初始化开始扫描
        initStopScan();//初始化停止扫描
        initResetScan();//初始化重置扫描
        initSubmitScan();//初始化提交扫描

        initTextType();//初始化选择器的标题
        initChooseType();//初始化类型选择器
        initScanInfo();//初始化扫描信息视图
    }


    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR:
                        Toast.makeText(ScanActivity.this,"获取产品类型异常，请检查网络环境", Toast.LENGTH_SHORT).show();
                        break;
                    case FAIL:
                        Toast.makeText(ScanActivity.this,"获取产品数据失败",Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    private void initReceive() {
        Intent intent = getIntent();
        check = intent.getStringExtra("check");
        receiveType();
        Log.e(TAG, "initReceive: check", null);
    }


    /**
     * 初始化控件
     */
    private void initView() {
        startScan = (AppCompatButton) findViewById(R.id.scan_btn_start);
        stopScan = (AppCompatButton) findViewById(R.id.scan_btn_stop);
        resetScan = (AppCompatButton) findViewById(R.id.scan_btn_reset);
        submitScan = (AppCompatButton) findViewById(R.id.scan_btn_submit);

        typeViewFirst = (View) findViewById(R.id.scan_type_first);
        typeViewSecond = (View) findViewById(R.id.scan_type_second);

        spinnerTypeFirst = (Spinner) typeViewFirst.findViewById(R.id.item_choose_type);
        spinnerTypeSecond = (Spinner) typeViewSecond.findViewById(R.id.item_choose_type);

        textTypeFirst = (TextView)typeViewFirst.findViewById(R.id.item_choose_tv);
        textTypeSecond = (TextView)typeViewSecond.findViewById(R.id.item_choose_tv);

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
     * 初始化选择器的标题
     */
    private void initTextType() {
        textTypeFirst.setText(R.string.type_first);
        textTypeSecond.setText(R.string.type_second);
    }

    /**
     * 初始化类型选择器
     */
    private void initChooseType() {
        //初始化第一、第二级类型的数组
        typeFirstList = new ArrayList<String>();
        typeSecondList = new ArrayList<String>();

        setTypeFirstList();
        //在确保typeFirstList可用的情况下对typeSecondList进行初始化
        if(typeFirstList!=null&&typeFirstList.size()>0) {
            try {
                setTypeSecondList(typeFirstList.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "initChooseType: JSON格式解析错误", null);
            }
        }


        //初始化第一级类型选择器
        typeFirstAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeFirstList);
        typeFirstAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeFirst.setAdapter(typeFirstAdapter);
        spinnerTypeFirst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    setTypeSecondList(typeFirstList.get(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "initChooseType: JSON格式解析错误", null);
                }
                typeSecondAdapter.notifyDataSetChanged();
                typeFirst = typeFirstList.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //初始化第二级类型选择器
        typeSecondAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeSecondList);
        typeSecondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeSecond.setAdapter(typeSecondAdapter);
        spinnerTypeSecond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeSecond = typeSecondList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /**
     * 初始化扫描工具
     */
    private void initUSeries() {
        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();

        /*Message openSerialPortMessage = */
        mUSeries.openSerialPort(model);
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



                //对扫描结果进行筛选
                for (int i = 0; i < InventoryOnceResult.size(); i++) {
                    Tag map = InventoryOnceResult.get(i);
                    Log.e(TAG, "onSuccess");

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

        List<Statistic> statisticList = new ArrayList<>();//存放统计数据的列表
        List<String> types = new ArrayList<>();//储存已经统计过的类型，已经统计过的类型以字符串的形式存放到数组中，用于比对

        for (int i = 0; i < cargos.size(); i++) {
            Cargo cargo1 = cargos.get(i);
            String typeFirstTmp = cargo1.getTypeFirst();//存放当前统计的一级类型
            String typeSecondTmp = cargo1.getTypeSecond();//存放当前统计的二级类型
            List<String> rfidList = new ArrayList<String>();

            String tmp = typeFirstTmp+" "+typeSecondTmp;
            int num=0;//当前物品的统计值
            if(!types.contains(tmp)) {
                types.add(tmp);
                for (int j = 0; j < cargos.size(); j++) {//若当前扫描的物品与比对的物品相同，则归为同一类，该类物品的数量加一并从列表中移除。
                    Cargo cargo2=cargos.get(j);
                    if(cargo2.getTypeFirst().equals(typeFirstTmp)&&cargo2.getTypeSecond().equals(typeSecondTmp)){
                        num++;
                        rfidList.add(cargo2.getRfid());
                        cargos.remove(j);
                        j--;
                    }
                }
            }
            //将该类物品的统计结果加入统计集合中
            Statistic statistic = new Statistic();
            statistic.setNum(num);
            statistic.setTypeFirst(typeFirstTmp);
            statistic.setTypeSecond(typeSecondTmp);
            statistic.setRfid(rfidList);
            statisticList.add(statistic);
        }
        //测试代码,测试发送是否成功
        List<String>  fType = new ArrayList<String>();
        fType.add("p1");
        fType.add("p2");
        fType.add("p3");

        List<String>  sType = new ArrayList<String>();
        sType.add("p11");
        sType.add("p21");
        sType.add("p31");

        for (int i = 0; i < 3; i++) {
            Statistic statistic = new Statistic();
            statistic.setNum(5);
            statistic.setTypeFirst(fType.get(i));
            statistic.setTypeSecond(sType.get(i));
            List<String> rfidList = new ArrayList<>();
            rfidList.add("1ADJAFIO");
            rfidList.add("1DJIFOA");
            rfidList.add("1ASJDFIO");
            statistic.setRfid(rfidList);
            statisticList.add(statistic);
        }

        for (int i = 0; i < statisticList.size(); i++) {
            Log.e(TAG, "submit: "+statisticList.get(i).getTypeFirst()+" "+statisticList.get(i).getTypeSecond(), null);
        }



        /*//测试代码
        for (int i = 0; i < statisticList.size(); i++) {
            System.out.println(statisticList.get(i).getTypeFirst()+" "+statisticList.get(i).getTypeSecond());
            for (int j = 0; j < statisticList.get(i).getRfid().size(); j++) {
                System.out.println(statisticList.get(i).getRfid().get(j));
            }
        }*/

        if (statisticList != null && statisticList.size() > 0) {
            toMainActivity(statisticList);
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


    public void receiveType(){
        Thread receiveTypeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    CheckMessage checkMessage = new CheckMessage(check);

                    cargoInSendMessage = restTemplate.postForObject(URL_RECEVICE_TYPE, checkMessage, cargoInSendMessage.getClass());
                    if(cargoInSendMessage!=null) {
                        typeList = cargoInSendMessage.getType();
                    } else {
                        handler.sendEmptyMessage(FAIL);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(ERROR);
                }
            }
        });
        receiveTypeThread.start();
    }
    /**
     * 获取一级物品类型
     */
    public void setTypeFirstList() {

        if(typeFirstList.size()!=0) {
            typeFirstList.clear();
        }
        if(typeList!=null&&typeList.size()>0) {
            for (int i = 0; i < typeList.size(); i++) {
                typeFirstList.add((String) typeList.get(i).get("typeFirst"));
            }
        }

        /*//测试代码
        String[] strs = {"p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
        typeFirstList.clear();
        for (int i = 0; i < strs.length; i++) {
            typeFirstList.add(strs[i]);
        }*/
    }

    /**
     * 获取二级物品类型
     */
    public void setTypeSecondList(String typeFirst) throws JSONException {
        if(typeSecondList.size()!=0) {
            typeSecondList.clear();
        }

        if(typeList!=null) {
            for (int i = 0; i < typeList.size(); i++) {
                if (typeList.get(i).get("typeFirst").equals(typeFirst)) {
                    JSONArray jsonArray = new JSONArray((String) typeList.get(i).get("typeSecond"));
                    for (int j = 0; j < jsonArray.length(); j++) {
                        typeSecondList.add(jsonArray.getString(j));
                    }
                    return;
                }
            }
        }
        /*//测试代码
        switch (typeFirst) {
            case "p1":
                String[] strs1 = {"p11", "p12", "p13", "p14", "p15"};
                typeSecondList.clear();
                for (int i = 0; i < strs1.length; i++) {
                    typeSecondList.add(strs1[i]);
                }
                typeSecond=typeSecondList.get(0);
                break;
            case "p2":
                String[] strs2 = {"p21", "p22", "p23", "p24", "p25"};
                typeSecondList.clear();
                for (int i = 0; i < strs2.length; i++) {
                    typeSecondList.add(strs2[i]);
                }
                typeSecond=typeSecondList.get(0);
                break;
                default:
                    typeSecondList.clear();
                    break;


        }*/
    }

    /**
     * 跳转至MainActivity
     * 将扫描结果发送消息给MainActivity
     *
     * @param statisticList 统计数据集合
     */
    public void toMainActivity(List<Statistic> statisticList) {
        if (statisticList == null || statisticList.size() > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("statisticList", (Serializable) statisticList);
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


