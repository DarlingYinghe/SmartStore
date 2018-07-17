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
import com.sicong.smartstore.stock_in.adapter.InStatisticAdapter;
import com.sicong.smartstore.stock_in.data.model.CargoInMessage;
import com.sicong.smartstore.stock_in.data.model.Statistic;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class InFragment extends Fragment {

    //常量
    private static final String TAG = "InFragment";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int SEND_SUCCESS = 1;
    private static final int SEND_FAIL = 2;
    private static final int SEND_ERROR = 3;

    //数据
    private String check ;//校验码
    private String company;//公司id
    private String username;//操作员

    private String describe = null;//描述内容
    private List<Statistic> statisticList;//统计数据集合
    private List<Statistic> statisticListTmp;//从MainActivity获取到的统计数据
    private CargoInMessage cargoInMessage;//发送的数据包



    //视图
    private EditText describeView;//描述视图
    private AppCompatButton submit;//提交的按钮
    private AppCompatButton toScan;//前往扫描的Activity的按钮
    private RecyclerView statisticView;//统计视图

    private Handler handler;

    //线程
    private Thread sendStatisticThread;

    //适配器
    private InStatisticAdapter inStatisticAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_in, container, false);
        initView(view);//初始化控件
        initHandler();//初始化handler
        initSubmit();//初始化提交按钮
        initToScan();//初始化前往Activity的按钮

        initStatistic();//初始化统计视图

        return view;
    }

    /**
     * 初始化统计视图
     */
    private void initStatistic() {
        statisticList = new ArrayList<>();
        inStatisticAdapter = new InStatisticAdapter(getContext(), statisticList);
        statisticView.setAdapter(inStatisticAdapter);
        statisticView.setLayoutManager(new LinearLayoutManager(getContext()));
        statisticView.setHasFixedSize(true);
        statisticView.setItemAnimator(new DefaultItemAnimator());
        statisticView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case SEND_SUCCESS:
                        statisticList.clear();
                        inStatisticAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SEND_FAIL:
                        Toast.makeText(getContext(), "提交失败", Toast.LENGTH_SHORT).show();
                        break;
                    case SEND_ERROR:
                        Toast.makeText(getContext(), "提交异常，请检查网络环境", Toast.LENGTH_SHORT).show();
                        break;
                    case NETWORK_UNAVAILABLE:
                        Toast.makeText(getContext(), "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        check = ((MainActivity) context).getCheck();
        company = ((MainActivity)context).getCompany();
        username = ((MainActivity) context).getUsername();
        statisticListTmp = ((MainActivity) context).getStatisticList();
    }



    @Override
    public void onResume() {
        super.onResume();

        onAttach(getContext());

        if (statisticListTmp != null && statisticListTmp.size() > 0) {
            statisticList.clear();
            statisticList.addAll(statisticListTmp);
            inStatisticAdapter.notifyDataSetChanged();
            packCargoInMessage();
        }

    }

    /**
     * 打包发送的数据
     */
    private void packCargoInMessage() {
        cargoInMessage = new CargoInMessage();
        cargoInMessage.setCheck(check);
        cargoInMessage.setUsername(username);
        cargoInMessage.setCompany(company);
        cargoInMessage.setStatistic(statisticList);
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        describeView = view.findViewById(R.id.stock_in_text);
        submit = view.findViewById(R.id.stock_in_submit);
        toScan = view.findViewById(R.id.stock_in_to_scan);
        statisticView = view.findViewById(R.id.stock_in_statistic);
    }

    private void initToScan() {
        toScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InActivity.class);
                intent.putExtra("check", check);
                intent.putExtra("company", company);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }


    /**
     * 初始化提交按钮
     */
    private void initSubmit() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }


    /**
     * 提交最终结果
     */
    private void submit() {
        if (statisticList != null && statisticList.size() > 0) {
            setDescribe();
            startSendStatisticThread();
        } else {
            Toast.makeText(getContext(), "无可供提交的数据，请检查数据", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDescribe() {
        describe = describeView.getText().toString();
        cargoInMessage.setDescription(describe);
    }

    private void startSendStatisticThread() {
        if (isNetworkAvailable(getContext())) {
            sendStatisticThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        Map<String, String> map = new HashMap<String,String>();

                        //发送请求
                        RestTemplate restTemplate = new RestTemplate();
                        map = restTemplate.postForObject(getResources().getString(R.string.URL_POST_CARGO_IN_MESSAGE), cargoInMessage, map.getClass());
                        if (map.get("msg").equals("success")) {
                            handler.sendEmptyMessage(SEND_SUCCESS);
                        } else {
                            handler.sendEmptyMessage(SEND_FAIL);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(SEND_ERROR);
                    }
                }


            });

            sendStatisticThread.start();
        }else{
            handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
        }
    }




}
