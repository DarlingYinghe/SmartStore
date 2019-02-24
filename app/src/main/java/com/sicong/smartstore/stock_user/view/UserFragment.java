package com.sicong.smartstore.stock_user.view;


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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_user.adapter.UserListAdapter;
import com.sicong.smartstore.util.network.Network;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    //常量
    private static final String TAG = "UserFragment";
    private final static int[] icons = {
            R.drawable.ic_user_info,
            R.drawable.ic_over,
            R.drawable.ic_unover
    };

    private final static int[] texts = {
            R.string.manager_user_info,
            R.string.mananger_over,
            R.string.manager_unover
    };
    
    private static final int NETWORK_UNAVAILABLE = 0;
    private static final int QUIT_SUCCESS = 1;
    private static final int QUIT_FAIL = 2;
    private static final int QUIT_ERROR = 3;
    

    //视图
    private TextView usernameView;
    private RecyclerView userList;
    private Button btnQuit;
    
    private Handler handler;

    //变量
    private String username;
    private String company;
    private String check;

    //适配器
    private UserListAdapter userListAdapter;
    
    //线程
    private Thread quitThread;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        username = ((MainActivity) context).getUsername();
        company = ((MainActivity) context).getCompany();
        check = ((MainActivity) context).getCheck();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initUsernameView();//初始化用户名文本框
        initUserList();//初始化功能列表
        initBtnQuit();//初始化退出按钮
        return view;
    }




    /**
     * 初始化控件
     *
     * @param view 当前视图
     */
    private void initView(View view) {
        usernameView = (TextView) view.findViewById(R.id.user_username);
        userList = (RecyclerView) view.findViewById(R.id.user_rv);
        btnQuit = (Button)view.findViewById(R.id.user_quit);
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case NETWORK_UNAVAILABLE:
                        Toast.makeText(getContext(), "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                        break;
                    case QUIT_SUCCESS:
                        break;
                    case QUIT_FAIL:
                        Toast.makeText(getContext(), "注销失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case QUIT_ERROR:
                        Toast.makeText(getContext(), "注销异常，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }
    /**
     * 初始化功能列表
     */
    private void initUserList() {
        List<Map<String, Object>> listMaps = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("text", texts[i]);
            map.put("icon", icons[i]);
            listMaps.add(map);
        }
        userListAdapter = new UserListAdapter(listMaps, getContext(), username, company, check);
        userList.setAdapter(userListAdapter);
        userList.setLayoutManager(new LinearLayoutManager(getContext()));
        userList.setHasFixedSize(true);
        userList.setItemAnimator(new DefaultItemAnimator());
        userList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化用户名文本框
     */
    private void initUsernameView() {
        usernameView.setText("ID："+username);
    }

    /**
     * 初始化退出按钮
     */
    private void initBtnQuit() {
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Network.isNetworkAvailable(getContext())) {
                    startQuitThread();
                } else {
                    handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
                }
            }
        });
    }

    /**
     * 启动注销线程
     */
    private void startQuitThread() {
        quitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> msg = new HashMap<String, String>();
                    msg.put("username", username);
                    msg.put("companyId", company);
                    msg.put("check", check);

                    Map<String,String> map = new HashMap<String,String>();
                    RestTemplate restTemplate = new RestTemplate();
                    map = restTemplate.postForObject(getResources().getString(R.string.URL_USER_QUIT), msg, map.getClass());
                    Log.e(TAG, map.toString(), null);

                    if(map.get("msg").equals("成功退出")) {
                        getActivity().finish();
                    } else {
                        handler.sendEmptyMessage(QUIT_FAIL);
                    }
                } catch(Exception e) {
                    handler.sendEmptyMessage(QUIT_ERROR);
                }
            }
        });
        quitThread.start();
    }


}
