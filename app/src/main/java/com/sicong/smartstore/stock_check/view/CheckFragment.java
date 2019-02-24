package com.sicong.smartstore.stock_check.view;


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
import com.sicong.smartstore.stock_change.adapter.ChangeListAdapter;
import com.sicong.smartstore.stock_check.adapter.CheckListAdapter;

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
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckFragment extends Fragment {

    //常量
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "CheckFragment";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int SUCCESS = 1;
    private static final int FAIL = 2;
    private static final int ERROR = 3;

    //数据
    private String check;
    private String company;
    private String username;
    private List<Map> stockChangeList;

    //视图
    private View view;
    private RecyclerView stockCheckListView;

    private Handler handler;

    //适配器
    CheckListAdapter stockCheckListAdapter;

    //线程
    private Thread requestDataThread;

    public CheckFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stock_check, container, false);
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initstockChangeListView();//初始化列表

        requestData();
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
        stockCheckListView = (RecyclerView)view.findViewById(R.id.stock_check_rv);
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
                        stockCheckListAdapter.notifyDataSetChanged();
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
    private void initstockChangeListView() {
        stockChangeList = new ArrayList<>();

        stockCheckListAdapter = new CheckListAdapter(getContext(), stockChangeList, check, company, username);
        stockCheckListView.setAdapter(stockCheckListAdapter);
        stockCheckListView.setLayoutManager(new LinearLayoutManager(getContext()));
        stockCheckListView.setHasFixedSize(true);
        stockCheckListView.setItemAnimator(new DefaultItemAnimator());
        stockCheckListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 请求数据
     */
    private void requestData(){
        if(isNetworkAvailable(getContext())) {
            startRequestDataThread();
        } else {
            handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
    }

    /**
     * 启动请求列表数据的线程
     */
    public void startRequestDataThread() {
        requestDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: requestData", null);
                try {
                    //发送的信息
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("check", check);
                    msg.put("companyId", company);
                    msg.put("username", username);

                    //用于接收的对象
                    List<Map<String, String>> maps = new ArrayList<>();

                    //发送的请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.postForObject(getContext().getResources().getString(R.string.URL_REQUEST_DATA_FOR_STOCK_CHECK_LIST), msg, maps.getClass());
                    Log.e(TAG, maps.toString(), null);

                    //处理请求的数据
                    if (maps == null) {
                        handler.sendEmptyMessage(FAIL);
                    } else {
                        stockChangeList.clear();
                        stockChangeList.addAll(maps);
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
