package com.sicong.smartstore.stock_out.view;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_out.adapter.OutListAdapter;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * 出库的Fragment
 */
public class OutFragment extends Fragment {

    //常量
    private final static String TAG = "OutFragment";

    private static final int NETWORK_UNAVAILABLE = 0;

    private final static int SUCCESS = 1;
    private final static int FAIL = 2;
    private final static int ERROR = 3;

    //数据
    private String check;
    private String company;
    private String username;

    private List<Map<String,String>> stockOutList;


    //视图
    private View view;
    private RecyclerView stockOutListView;

    private Handler handler;
    //适配器
    private OutListAdapter outListAdapter;

    //线程
    private Thread requestDataThread;



    public OutFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stock_out, container, false);
        initView(view);//初始化控件
        initHandler();
        initStockOutListView();



        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        check = ((MainActivity)context).getCheck();
        company = ((MainActivity)context).getCompany();
        username = ((MainActivity)context).getUsername();
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
                        outListAdapter.notifyDataSetChanged();
                        break;
                    case FAIL:
                        /**UI优化时用背景图片代替**/
                        Toast.makeText(getContext(), "暂时没有单号", Toast.LENGTH_SHORT).show();
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

        outListAdapter = new OutListAdapter(getContext(), stockOutList, check, company, username);
        stockOutListView.setAdapter(outListAdapter);
        stockOutListView.setLayoutManager(new LinearLayoutManager(getContext()));
        stockOutListView.setHasFixedSize(true);
        stockOutListView.setItemAnimator(new DefaultItemAnimator());
        stockOutListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    public void startRequestDataThread(){
        requestDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: requestData", null);
                try {
                    //发送的信息
                    Map<String,String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("company", company);
                    msg.put("username", username);

                    //用于接收的对象
                    List<Map<String,String>> maps = new ArrayList<Map<String, String>>();

                    //发出请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.postForObject(getResources().getString(R.string.URL_REQUEST_DATA_FOR_STOCK_OUT_LIST), msg, maps.getClass());
                    Log.e(TAG, "run: "+maps.get(0), null);
                    //处理请求的数据
                    stockOutList.clear();
                    stockOutList.addAll(maps);

                    if (maps == null) {
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
    }



}
