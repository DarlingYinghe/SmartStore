package com.sicong.smartstore.stock_change.view;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_out.adapter.StockOutListAdapter;
import com.sicong.smartstore.stock_out.model.CargoSendListMessage;
import com.sicong.smartstore.stock_out.model.StockOutCargoReceiveMessage;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockChangeFragment extends Fragment {
    //基本变量
    private static final String TAG = "StockChangeFragment";
    private static final int SUCCESS = 1;
    private static final int FAIL = 0;
    private static final int ERROR = -1;
    private static final int NETWORK_UNAVAILABLE = -2;
    private final String URL_REQUEST_DATA_FOR_STOCK_CHANGE_LIST=getContext().getResources().getString(R.string.URL_REQUEST_DATA_FOR_STOCK_CHANGE_LIST);
    //数据
    private String check;
    private CargoSendListMessage cargoSendListMessage;
    private List<Map<String,String>> stockOutList;
    private String tag;
    //视图
    private View view;
    private RecyclerView stockOutListView;

    private Handler handler;
    //适配器
    StockOutListAdapter stockOutListAdapter;


    public StockChangeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stock_change, container, false);
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initStockOutListView();//初始化列表

        requestData();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        check = ((MainActivity)context).getCheck();
    }

    /**
     * 初始化控件
     * @param view 当前Fragment视图
     */
    private void initView(View view) {
        stockOutListView = (RecyclerView)view.findViewById(R.id.stock_out_rv);
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS:
                        stockOutListAdapter.notifyDataSetChanged();
                        break;
                    case FAIL:
                        Toast.makeText(getContext(), "请求无响应，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        Toast.makeText(getContext(),"获取单号列表失败，请检查网络环境", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化单号列表
     */
    private void initStockOutListView() {
        stockOutList = new ArrayList<Map<String,String>>();
        tag = "change";
        //测试数据
        for (int i = 0; i < 10; i++) {
            Map map = new HashMap<String,String>();
            map.put("id",String.valueOf(i));
            map.put("date","日期"+String.valueOf(i));
            stockOutList.add(map);
        }

        stockOutListAdapter = new StockOutListAdapter(getContext(), stockOutList, check, tag);
        stockOutListView.setAdapter(stockOutListAdapter);
        stockOutListView.setLayoutManager(new LinearLayoutManager(getContext()));
        stockOutListView.setHasFixedSize(true);
        stockOutListView.setItemAnimator(new DefaultItemAnimator());
        stockOutListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 请求数据
     */
    private void requestData(){
        if(isNetworkAvailable(getContext())) {

            Thread requestDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        cargoSendListMessage = restTemplate.postForObject(URL_REQUEST_DATA_FOR_STOCK_CHANGE_LIST, new StockOutCargoReceiveMessage(check), CargoSendListMessage.class);
                        if (cargoSendListMessage == null) {
                            handler.sendEmptyMessage(FAIL);
                        } else {
                            handler.sendEmptyMessage(SUCCESS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(ERROR);
                    }
                }
            });
            requestDataThread.start();
        } else {
            handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
    }




}
