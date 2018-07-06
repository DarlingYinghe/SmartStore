package com.sicong.smartstore.stock_in.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_in.data.model.CargoInMessage;
import com.sicong.smartstore.stock_in.data.model.Statistic;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class StockInFragment extends Fragment {

    private static final String TAG = "StockInFragment";
    private static final int SEND_SUCCESS = 1;
    private static final int SEND_FAIL = 0;
    private static final int SEND_ERROR = -1;
    //data
    private String check = null;//校验码
    private String operatorId = null;//操作员
    private String describe = null;//描述内容
    private List<Statistic> statisticList;//统计数据集合
    private CargoInMessage cargoInMessage;//发送的数据包

    private final static String URL_POST_CARGO_IN_MESSAGE = "";

    //view
    private EditText describeView;//描述视图
    private AppCompatButton submit;//提交的按钮
    private AppCompatButton toScan;//前往扫描的Activity的按钮

    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_in, container, false);
        initView(view);//初始化控件
        initHandler();//初始化handler
        initSubmit();//初始化提交按钮
        initToScan();//初始化前往Activity的按钮

        return view;
    }

    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case SEND_SUCCESS:
                        Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SEND_FAIL:
                        Toast.makeText(getContext(), "提交失败", Toast.LENGTH_SHORT).show();
                        break;
                    case SEND_ERROR:
                        Toast.makeText(getContext(), "提交异常，请检查网络环境", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach: start", null);
        check = ((MainActivity) context).getCheck();
        operatorId = ((MainActivity) context).getOperatorId();
        statisticList = ((MainActivity) context).getStatisticList();

    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();
        onAttach(getContext());
        //测试代码
        /*if (statisticList!=null){
            for (int i = 0; i < statisticList.size(); i++) {
                Log.e(TAG, "onAttach: "+statisticList.get(i).getTypeFirst(), null);
            }
        }
        Log.e(TAG, "onResume: "+check, null);
        Log.e(TAG, "onResume: "+operatorId, null);*/

        if (intent.hasExtra("statisticList")) {
            receiveStatisticList(intent);
            packCargoInMessage();
        }
    }

    /**
     * 打包发送的数据
     */
    private void packCargoInMessage() {
        cargoInMessage = new CargoInMessage();
        cargoInMessage.setCheck(check);
        cargoInMessage.setOperatorId(operatorId);
        cargoInMessage.setStatistic(statisticList);
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        describeView = view.findViewById(R.id.stock_in_text);
        submit = view.findViewById(R.id.stock_in_submit);
        toScan = view.findViewById(R.id.stock_in_to_scan);
    }

    private void initToScan() {
        toScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                intent.putExtra("check", check);
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
     * 用于接收ScantActivity的发送过来的数据
     */
    private void receiveStatisticList(Intent intent) {
        statisticList = new ArrayList<>();
        statisticList = (List<Statistic>) intent.getSerializableExtra("statisticList");
        //测试代码
            /*for (int i = 0; i < statisticList.size(); i++) {
                System.out.println(statisticList.get(i).getTypeFirst()+" "+statisticList.get(i).getTypeSecond());
                for (int j = 0; j < statisticList.get(i).getRfid().size(); j++) {
                    System.out.println(statisticList.get(i).getRfid().get(j));
                }
            }*/
    }

    /**
     * 提交最终结果
     */
    private void submit() {
        setDescribe();
        sendCargoInMessage();
    }

    public void setDescribe() {
        describe = describeView.getText().toString();
        cargoInMessage.setDescribe(describe);
    }

    private void sendCargoInMessage() {
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    String response = restTemplate.postForObject(URL_POST_CARGO_IN_MESSAGE, cargoInMessage, String.class);
                    if (response.equals("success")) {
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
        sendThread.start();
    }


}
