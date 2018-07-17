package com.sicong.smartstore.stock_in.view;


import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_in.adapter.InScanAdapter;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_in.data.model.Statistic;
import com.sicong.smartstore.util.fn.u6.model.ResponseHandler;
import com.sicong.smartstore.util.fn.u6.model.Tag;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

/**
 * A simple {@link Fragment} subclass.
 */
public class InActivity extends AppCompatActivity {

    //常量
    private static final String TAG = "ScanFragment";
    private String model = "U6";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int TYPE_SUCCESS = 1;
    private static final int TYPE_FAIL = 2;
    private static final int TYPE_ERROR = 3;

    private static final int NAME_SUCCESS = 4;
    private static final int NAME_FAIL = 5;
    private static final int NAME_ERROR = 6;

    //视图
    private AppCompatButton btnStart;//开始扫描按钮
    private AppCompatButton btnStop;//停止扫描按钮
    private AppCompatButton btnReset;//重置按钮
    private AppCompatButton btnSubmit;//提交按钮

    private View typeViewFirst;//一级类型选择器的布局视图
    private View typeViewSecond;//二级类型选择器的布局视图
    private View nameView;//货物名称的布局视图
    private TextView textTypeFirst;//一级选择器的标题
    private TextView textTypeSecond;//二级选择器的标题
    private TextView textName;//名称的标题
    private Spinner spinnerTypeFirst;//一级类型选择器
    private Spinner spinnerTypeSecond;//二级类型选择器
    private Spinner spinnerName;//货物名称选择器

    private CoordinatorLayout snackbarContainer;//Snackbar的容器

    private RecyclerView scanInfoView;//扫描信息的列表

    private Handler handler;

    //适配器
    private InScanAdapter inScanAdapter;//扫描信息的列标的适配器
    private ArrayAdapter<String> typeFirstAdapter;//一级类型适配器
    private ArrayAdapter<String> typeSecondAdapter;//二级类型适配器
    private ArrayAdapter<String> nameAdapter;//货物名称适配器

    //数据
    public IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps = new ArrayList<String>();//已扫描RFID集合：已扫描过的rfid码，避免重复
    private List<Cargo> cargos = new ArrayList<Cargo>();//货物对象集合：扫描的所有物品的集合

    private String typeFirst;
    private String typeSecond;
    private String name;
    private List<String> typeFirstList;
    private List<String> typeSecondList;
    private List<String > nameList;

    private String check;
    private String username;
    private String company;
    private List<Map<String,Object>> typeList;//包含两级类型的集合

    //线程
    private Thread nameThread;
    private Thread receiveTypeThread;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;


    public InActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in);

        initView();//初始化控件
        initReceive();//初始化接收数据
        initHandler();//初始化Handler
        initUSeries();//初始化所需对象

        initBtnStart();//初始化开始扫描
        initBtnStop();//初始化停止扫描
        initBtnReset();//初始化重置扫描
        initBtnSubmit();//初始化提交扫描

        initBtnStatus();//初始化按钮状态

        initTextType();//初始化选择器的标题
        initChooseType();//初始化类型选择器
        initScanInfo();//初始化扫描信息视图

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
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case TYPE_SUCCESS:
                        typeFirstAdapter.notifyDataSetChanged();
                        typeSecondAdapter.notifyDataSetChanged();
                        break;
                    case TYPE_ERROR:
                        Snackbar.make(snackbarContainer,"获取产品类型异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case TYPE_FAIL:
                        Snackbar.make(snackbarContainer,"获取产品类型失败，请稍后再试",Snackbar.LENGTH_SHORT).show();
                        break;
                    case NETWORK_UNAVAILABLE:
                        Snackbar.make(snackbarContainer, "无可用的网络，请连接网络", Snackbar.LENGTH_SHORT).show();
                        break;
                    case NAME_SUCCESS:
                        nameAdapter.notifyDataSetChanged();
                        break;
                    case NAME_FAIL:
                        Snackbar.make(snackbarContainer, "请求货物名称数据失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case NAME_ERROR:
                        Snackbar.make(snackbarContainer, "请求货物名称数据异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;

                }
                return false;
            }
        });
    }

    private void initReceive() {
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
    }


    /**
     * 初始化控件
     */
    private void initView() {
        btnStart = (AppCompatButton) findViewById(R.id.scan_btn_start);
        btnStop = (AppCompatButton) findViewById(R.id.scan_btn_stop);
        btnReset = (AppCompatButton) findViewById(R.id.scan_btn_reset);
        btnSubmit = (AppCompatButton) findViewById(R.id.scan_btn_submit);
        

        typeViewFirst = (View) findViewById(R.id.scan_type_first);
        typeViewSecond = (View) findViewById(R.id.scan_type_second);
        nameView = (View)findViewById(R.id.scan_name);

        spinnerTypeFirst = (Spinner) typeViewFirst.findViewById(R.id.item_choose_type);
        spinnerTypeSecond = (Spinner) typeViewSecond.findViewById(R.id.item_choose_type);
        spinnerName = (Spinner)nameView.findViewById(R.id.item_choose_type);

        textTypeFirst = (TextView)typeViewFirst.findViewById(R.id.item_choose_tv);
        textTypeSecond = (TextView)typeViewSecond.findViewById(R.id.item_choose_tv);
        textName = (TextView)nameView.findViewById(R.id.item_choose_tv);

        scanInfoView = (RecyclerView) findViewById(R.id.scan_info_view);

        snackbarContainer = (CoordinatorLayout) findViewById(R.id.in_scan_snackbar_container);
    }


    /**
     * 初始化扫描信息视图
     */
    private void initScanInfo() {
        inScanAdapter = new InScanAdapter(this, cargos);
        scanInfoView.setAdapter(inScanAdapter);
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
        textName.setText(R.string.name);
    }

    /**
     * 初始化类型选择器
     */
    private void initChooseType() {
        //初始化第一、第二级类型的数组
        typeFirstList = new ArrayList<String>();
        typeSecondList = new ArrayList<String>();
        nameList = new ArrayList<String>();
        setTypeFirstList();
        //在确保typeFirstList可用的情况下对typeSecondList进行初始化
        if(typeFirstList!=null&&typeFirstList.size()>0) {
            try {
                setTypeSecondList(typeFirstList.get(0));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "initChooseType: 数据解析错误", null);
            }
        }


        //初始化第一级类型选择器
        typeFirstAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeFirstList);
        typeFirstAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeFirst.setAdapter(typeFirstAdapter);
        spinnerTypeFirst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeFirst = typeFirstList.get(position);
                Log.e(TAG, "onItemSelected: typeFirst is "+typeFirst,null );

                try {
                    setTypeSecondList(typeFirst);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "initChooseType: 数据解析错误", null);
                }
                typeSecondAdapter.notifyDataSetChanged();
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
                Log.e(TAG, "onItemSelected: typeSecond is "+typeSecond,null );

                try {
                    setNameList(typeSecond);
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(snackbarContainer, "数据解析错误", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //初始化货物名称选择器
        nameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nameList);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerName.setAdapter(nameAdapter);
        spinnerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                name = nameList.get(position);
                Log.e(TAG, "onItemSelected: name is "+name,null );

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
    private void initBtnStart() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //测试代码
                getRfidCode();
                //测试代码结束
                if(typeFirst==null || typeSecond == null || name == null) {
                    Snackbar.make(snackbarContainer, "请先选择类型", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "onClick: start", null);
                setBtnStatus(false, true, true, true);
                getRfidCode();
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
                stopRfidCode();
            }
        });
    }


    /**
     * 初始化重置扫描
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


    /**
     * 初始化提交扫描
     */
    private void initBtnSubmit() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: submit", null);
                submit();
            }
        });
    }

    /**
     * 初始化按钮状态
     */
    private void initBtnStatus() {
        setBtnStatus(true, false, false, false);
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
                if (name != null) {

                    List<Tag> InventoryOnceResult = (List<Tag>) data;//一次扫描到的数据，因为不排除扫描到周围其他物体的可能性，故用数组接收结果，但是数组内部已做好对其他数组的过滤


                    //对扫描结果进行筛选
                    for (int i = 0; i < InventoryOnceResult.size(); i++) {
                        Tag map = InventoryOnceResult.get(i);

                        if (!InventoryTaps.contains(map.epc)) {//避免RFID码重复扫入与空类型扫入
                            //若RFID不重复，则将扫描到的RFID码放入“已扫描RFID集合”
                            InventoryTaps.add(map.epc);

                            //创建新的货物对象
                            Cargo cargo = new Cargo();

                            cargo.setName(name);
                            cargo.setRfid(map.epc);

                            //更新视图
                            inScanAdapter.insert(cargo);

                        } else {

                        }
                    }
                }
            }

                @Override
                public void onFailure (String msg){
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

        //测试代码
        /*for (int i = 0; i < 10; i++) {
            Cargo cargo = new Cargo();
            cargo.setName("锌电池");
            cargo.setRfid("JDIOA"+i);
            cargos.add(cargo);
        }
        for (int i = 0; i < 5; i++) {
            Cargo cargo = new Cargo();
            cargo.setName("锌锰电池");
            cargo.setRfid("XINFAM"+i);
            cargos.add(cargo);
        }*/
        //测试代码完毕

        if(cargos!=null&&cargos.size()>0) {
            for (int i = 0; i < cargos.size(); i++) {
                Cargo cargo1 = cargos.get(i);
                String nameTmp = cargo1.getName();//存放当前统计的名称
                List<String> rfidList = new ArrayList<String>();

                if (nameTmp != null) {
                    int num = 0;//当前物品的统计值
                    if (!types.contains(nameTmp)) {
                        types.add(nameTmp);
                        for (int j = 0; j < cargos.size(); j++) {//若当前扫描的物品与比对的物品相同，则归为同一类，该类物品的数量加一并从列表中移除。
                            Cargo cargo2 = cargos.get(j);
                            if (cargo2.getName().equals(nameTmp)) {
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
                    statistic.setName(nameTmp);
                    statistic.setRfid(rfidList);
                    statisticList.add(statistic);

                }
            }


        } else {
            Snackbar.make(snackbarContainer, "扫描数据为空，请检查数据", Snackbar.LENGTH_SHORT).show();
            return;
        }
        //测试代码,测试发送是否成功
        /*List<String>  fType = new ArrayList<String>();
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
        }*/



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
            Snackbar.make(snackbarContainer, "无扫描结果，无法提交", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * 重置扫描
     */
    private void reset() {
        stopRfidCode();
        inScanAdapter.clear();
        InventoryTaps.clear();
    }


    /**
     * 接收物品类型
     */
    public void startReceiveTypeThread(){
        receiveTypeThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    RestTemplate restTemplate = new RestTemplate();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("check", check);
                    map.put("username",username);
                    map.put("company",company);

                    typeList = new ArrayList<Map<String, Object>>();
                    typeList = restTemplate.postForObject(getResources().getString(R.string.URL_RECEVICE_TYPE), map, typeList.getClass());

                    if (typeList != null) {
                        for (int i = 0; i < typeList.size(); i++) {

                            String  typeSecondsStr = (String)typeList.get(i).get("typeSeconds");

                            String[] typeSecondsTmp = typeSecondsStr.split(",");
                            List<String> typeSecondsListTmp = new ArrayList<String>();
                            for (int j = 0; j < typeSecondsTmp.length; j++) {
                                typeSecondsListTmp.add(j, typeSecondsTmp[j]);
                            }
                            String typeFirstTmp = (String)typeList.get(i).get("typeFirst");

                            Map<String, Object> mapTmp = new HashMap<String, Object>();
                            mapTmp.put("typeFirst", typeFirstTmp);
                            mapTmp.put("typeSeconds", typeSecondsListTmp);

                            typeList.set(i, mapTmp);

                        }

                        setTypeFirstList();
                        typeFirst = (String)typeList.get(0).get("typeFirst");
                        setTypeSecondList(typeFirst);

                        //测试代码
                        /*for (int i = 0; i < typeList.size(); i++) {
                            Log.e(TAG, "run: "+typeList.get(i).get("typeFirst"), null);
                            for (int j = 0; j < ((ArrayList<String>)typeList.get(i).get("typeSeconds")).size(); j++) {
                                List<String> tmp = (ArrayList<String>)typeList.get(i).get("typeSeconds");
                                Log.e(TAG, "run: "+tmp.get(j), null);
                            }
                        }*/
                        handler.sendEmptyMessage(TYPE_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(TYPE_FAIL);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(TYPE_ERROR);
                }
            }

        });
        receiveTypeThread.start();
    }
    /**
     * 设置一级物品类型数组
     */
    public void setTypeFirstList() {

        if(typeFirstList.size()!=0) {
            typeFirstList.clear();
        }

        if(typeList!=null&&typeList.size()>0) {
            for (int i = 0; i < typeList.size(); i++) {
                Map<String,Object> map = typeList.get(i);
                String typeFirstTmp = (String)map.get("typeFirst");
                typeFirstList.add(typeFirstTmp);
            }
        }

        //测试代码
        /*String[] strs = {"p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};
        typeFirstList.clear();
        for (int i = 0; i < strs.length; i++) {
            typeFirstList.add(strs[i]);
        }*/
    }

    /**
     * 设置二级物品类型数组
     */
    public void setTypeSecondList(String typeFirst)  {

        if(typeSecondList.size()!=0) {
            typeSecondList.clear();
        }

        if(typeList!=null&&typeList.size()>0) {
            for (int i = 0; i < typeList.size(); i++) {
                Map<String,Object> map1 = typeList.get(i);
                String typeFirstTmp = (String)map1.get("typeFirst");
                if(typeFirst.equals(typeFirstTmp)){
                    List<String> stringList = (List<String>) map1.get("typeSeconds");
                    typeSecondList.addAll(stringList);
                }
            }
        }

        //测试代码
        /*switch (typeFirst) {
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
     * 设置货物名称数组
     * @param typeSecond 当前二级类型
     */
    public void setNameList(String typeSecond){
        startNameThread(typeSecond);
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
        this.overridePendingTransition(R.anim.activity_out_left, R.anim.activity_in_right);
    }

    /**
     * 请求货物名称列表的线程
     */
    public void startNameThread(final String typeSecond) {
        Log.e(TAG, "startNameThread: start", null);
        if(isNetworkAvailable(InActivity.this)) {
        nameThread = new Thread(new Runnable() {
            @Override
            public void run() {

                    try{
                        Map map = new HashMap<String,String>();

                        map.put("check", check);
                        map.put("typeSecond", typeSecond);
                        map.put("company", company);

                        Log.e(TAG, "run: check is "+check, null);
                        Log.e(TAG, "run: typeSecond is "+typeSecond, null);
                        Log.e(TAG, "run: company is "+company, null);

                        RestTemplate restTemplate = new RestTemplate();
                        List<String> nameListTmp = restTemplate.postForObject(getResources().getString(R.string.URL_STOCK_IN_SCAN_NAME), map, nameList.getClass());
                        if(nameListTmp!=null) {
                            nameList.clear();
                            nameList.addAll(nameListTmp);
                            handler.sendEmptyMessage(NAME_SUCCESS);
                            Log.e(TAG, "run: nameList is "+nameList, null);
                        } else {
                            handler.sendEmptyMessage(NAME_FAIL);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(NAME_ERROR);
                    }

            }
        });
        } else {

        }
        nameThread.start();

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
                        startReceiveTypeThread();
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


