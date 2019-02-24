package com.sicong.smartstore.stock_in.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_in.adapter.InListAdapter;
import com.sicong.smartstore.stock_in.adapter.InStatisticAdapter;
import com.sicong.smartstore.stock_in.data.model.CargoInMessage;
import com.sicong.smartstore.stock_in.data.model.Statistic;

import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class InFragment extends Fragment {
    //常量
    private final static String TAG = "InFragment";
    private final static int IN_LIST_SUCESS = 1;
    private final static int IN_LIST_FAIL = 2;
    private final static int IN_LIST_ERROR = 3;

    //控件
    private RecyclerView inList;
    private Handler handler;

    //适配器
    private InListAdapter inListAdapter;

    //数据
    private List<Map<String, String>> stockinList;
    private String username;
    private String check;
    private String company;

    //线程
    private Thread requestDataThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_in, container, false);
        initView(view);//控件初始化
        initHandler();//初始化Handler
        initInList();//初始化表单列表


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        username = ((MainActivity)context).getUsername();
        check = ((MainActivity)context).getCheck();
        company = ((MainActivity)context).getCompany();
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case IN_LIST_ERROR:
                        Toast.makeText(getContext(),"获取入库表单列表异常",Toast.LENGTH_SHORT).show();
                        break;
                    case IN_LIST_FAIL:
                        Toast.makeText(getContext(),"入库表单获取失败",Toast.LENGTH_SHORT).show();
                        break;
                    case IN_LIST_SUCESS:
                        inListAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 表单列表初始化
     */
    private void initInList() {
        //设置样式
        inList.setLayoutManager(new LinearLayoutManager(getContext()));
        inList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        //适配器
        stockinList = new ArrayList<Map<String, String>>();
        inListAdapter = new InListAdapter(getContext(), stockinList, check, company, username);
        inList.setAdapter(inListAdapter);
    }

    /**
     * 控件初始化
     * @param view
     */
    private void initView(View view) {
        inList = (RecyclerView) view.findViewById(R.id.in_list);
    }

    /**
     * 启动数据请求的进程
     */
    public void startRequestDataThread() {
        requestDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("username", username);
                    msg.put("check", check);
                    msg.put("companyId", company);

                    //api接口地址
                    List<Map<String, String>> mapsTmp = new ArrayList<Map<String, String>>();
                    String Url = getResources().getString(R.string.URL_REQUEST_DATA_FOR_STOCK_IN_LIST);

                    //发送请求
                    RestTemplate restTemplate = new RestTemplate();
                    mapsTmp = restTemplate.postForObject(Url, msg, mapsTmp.getClass());
                    Log.e(TAG, "run"+mapsTmp.toString(), null);

                    if (mapsTmp == null) {
                        handler.sendEmptyMessage(IN_LIST_FAIL);
                    } else {
                        stockinList.clear();
                        stockinList.addAll(mapsTmp);
                        handler.sendEmptyMessage(IN_LIST_SUCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(IN_LIST_ERROR);
                }

            }
        });
        requestDataThread.start();
    }


}
