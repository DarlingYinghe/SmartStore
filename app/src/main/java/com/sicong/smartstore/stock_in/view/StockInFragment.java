package com.sicong.smartstore.stock_in.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_in.data.model.InventoryResult;
import com.sicong.smartstore.stock_in.data.model.Statistic;

import java.util.ArrayList;
import java.util.List;

public class StockInFragment extends Fragment {

    private static final String TAG = "StockInFragment";
    //data
    private List<Cargo> cargos = null;//入库货物数组
    private List<Cargo> cargosClone = null;//复制一个用于统计
    private String operatorId = null;//操作员
    private String describe = null;//描述内容
    private String response = null;//K服务器回应
    private List<Statistic> statistics = new ArrayList<Statistic>();//统计总数列表

    private InventoryResult inventoryResult = null;//发送的数据包

    private final static String  URL_POST_INVENTORY= "";

    //view
    private EditText describeView;//描述视图
    private AppCompatButton submit;//提交的按钮
    private AppCompatButton toScan;//前往扫描的Activity的按钮

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_in, container, false);
        initView(view);//初始化控件
        initObject();//初始化一些对象
        initSubmit();//初始化提交按钮
        initToScan();//初始化前往Activity的按钮

        return view;
    }

    private void initObject() {
        cargos = new ArrayList<Cargo>();
    }

    @Override
    public void onResume() {
        super.onResume();


        receiveScanResult();
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
                startActivity(new Intent(getActivity(), ScanActivity.class));
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
    private void receiveScanResult() {
        Intent intent = getActivity().getIntent();
        if(intent.hasExtra("cargos")) {
            cargos = (List<Cargo>) intent.getSerializableExtra("cargos");
            //统计
            cargosClone.addAll(cargos);
            int count=0;
            for (int i = 0; i < cargosClone.size(); i++) {
                count=0;
                for (int j = i+1; j < cargosClone.size(); j++) {
                    if(cargosClone.get(i) == cargosClone.get(j)){
                        count++;
                        cargosClone.remove(j);
                        j--;
                    }
                }

                Statistic statistic = new Statistic();
                statistic.setTypeSecond(cargosClone.get(i).getTypeSecond());
                statistic.setNum(count);
                statistics.add(statistic);
                System.out.println(cargosClone.get(i)+">>>"+count);
            }
            for (int i = 0; i < cargos.size(); i++) {
                Log.e(TAG, "receiveScanResult: " + cargos.get(i).getRfid(), null);
            }
        }
    }

    /**
     * 提交最终结果
     */
    private void submit() {
        //获取描述内容
        describe = describeView.getText().toString();

        //创建数据包
        inventoryResult = new InventoryResult();
        inventoryResult.setCargos(cargos);
        inventoryResult.setOperatorId(operatorId);
        inventoryResult.setDescribe(describe);

        //测试代码
        for (int i = 0;i<inventoryResult.getCargos().size();i++) {
            Log.e(TAG, "submit: "+inventoryResult.getCargos().get(i).getRfid(), null);
        }

        /******************发送数据******************/
        /*RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(URL_POST_INVENTORY, inventoryResult, String.class);*/
        /*******************************************/
    }


}
