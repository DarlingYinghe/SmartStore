package com.sicong.smartstore.stock_out.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.adapter.ScanInfoAdapter;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_in.data.model.CheckMessage;
import com.sicong.smartstore.stock_out.adapter.DetailStockOutAdapter;
import com.sicong.smartstore.stock_out.model.CargoListSendMessage;
import com.sicong.smartstore.util.fn.u6.operation.IUSeries;
import com.sicong.smartstore.util.fn.u6.operation.U6Series;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    //基本变量
    private final static String TAG = "DetailActivity";
    private static final int ERROR = -1;
    private static final int FAIL = 0;

    private String model = "U6";
    //数据
    private CargoListSendMessage cargoListSendMessage;
    private List<Map<String,Object>> detailStockOutList;
    private CheckMessage checkMessage;
    public IUSeries mUSeries;//扫描工具
    private List<String> InventoryTaps = new ArrayList<String>();//已扫描RFID集合：已扫描过的rfid码，避免重复
    private List<Cargo> cargos;//货物对象集合：扫描的所有物品的集合
    
    //视图
    private RecyclerView detailStockOutView;
    private RecyclerView scanInfoView;
    private AppCompatButton btnStart;
    private AppCompatButton btnStop;
    private AppCompatButton btnRest;
    private AppCompatButton btnSubmit;
    
    //适配器
    private DetailStockOutAdapter detailStockOutAdapter;
    private ScanInfoAdapter scanInfoAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initView();//初始化控件
        initObject();//初始化一部分对象
        initDetailStockOutView();//初始化待出库物品列表
        initscanInfoView();//初始化扫描列表
    }

    /**
     * 初始化一部分对象
     */
    private void initObject() {
        U6Series.setContext(this);
        mUSeries = U6Series.getInstance();
        mUSeries.openSerialPort(model);
    }

    /**
     * 初始化扫描列表
     */
    private void initscanInfoView() {
        cargos  = new ArrayList<Cargo>();
        scanInfoAdapter = new ScanInfoAdapter(this, cargos);
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
        btnRest = findViewById(R.id.detail_btn_reset);
        btnSubmit = findViewById(R.id.detail_btn_submit);
    }
}
