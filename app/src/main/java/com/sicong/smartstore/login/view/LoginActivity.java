package com.sicong.smartstore.login.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.login.data.model.Login;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sicong.smartstore.util.network.Network.isNetworkAvailable;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //常量
    private static final String TAG = "LoginActivity";

    private static final int NETWORK_UNAVAILABLE = 0;

    private static final int LOGIN_SUCCESS = 1;
    private static final int LOGIN_ERROR = 2;
    private static final int LOGIN_FAIL = 3;

    private static final int COMPANY_SUCCESS = 4;
    private static final int COMPANY_FAIL =5;
    private static final int COMPANY_ERROR = 6;

    //视图
    private TextInputEditText inputUsername;//用户名的输入框
    private TextInputEditText inputPassword;//密码的输入框
    private Spinner spinnerCompany;//公司选择器
    private AppCompatButton btnLogin;//登录按钮
    private CoordinatorLayout snackbarContainer;//Snackbar的容器

    private Handler handler;
    
    //适配器
    private ArrayAdapter<String> companyAdapter;
    
    //数据
    private List<String> companyList;
    private String check;
    private String companyName;
    private String company;
    private String username;
    private String password;

    //线程
    private Thread companyThread;
    private Thread loginThread;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();//初始化控件
        initHandler();//初始化handler
        initSpinnerCompany();//初始化公司选择器
        initEnter();//登录操作
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();//初始化广播

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);
    }

    /**
     * 初始化公司选择器
     */
    private void initSpinnerCompany() {
        companyList = new ArrayList<String>();
        companyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, companyList);
        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompany.setAdapter(companyAdapter);

        spinnerCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected: "+position, null);
                companyName = companyList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", inputUsername.getText().toString());
                        intent.putExtra("company", company);
                        intent.putExtra("check", check);
                        startActivity(intent);
                        break;
                    case LOGIN_FAIL:
                        Snackbar.make(snackbarContainer, "登录失败，请检查用户名与密码是否正确", Snackbar.LENGTH_SHORT).show();
                        break;
                    case LOGIN_ERROR:
                        Snackbar.make(snackbarContainer, "服务器请求异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case NETWORK_UNAVAILABLE:
                        Snackbar.make(snackbarContainer, "无可用的网络，请连接网络", Snackbar.LENGTH_SHORT).show();
                        break;
                    case COMPANY_SUCCESS:
                        companyAdapter.notifyDataSetChanged();
                        break;
                    case COMPANY_FAIL:
                        Snackbar.make(snackbarContainer, "请求公司数据失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                        break;
                    case COMPANY_ERROR:
                        Snackbar.make(snackbarContainer, "请求公司数据异常，请稍后再试", Snackbar.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }


    /**
     * 初始化控件
     */
    private void initView() {
        inputUsername = (TextInputEditText) findViewById(R.id.login_username);
        inputPassword = (TextInputEditText) findViewById(R.id.login_password);
        btnLogin = (AppCompatButton) findViewById(R.id.login_enter);
        snackbarContainer = (CoordinatorLayout) findViewById(R.id.login_container);
        spinnerCompany = (Spinner)findViewById(R.id.login_company);
    }

    /**
     * 设置登录按钮
     */
    private void initEnter() {
        //为按钮添加点击事件
        btnLogin.setOnClickListener(this);
    }

    /**
     * 点击事件
     * @param v 传入的视图
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_enter:
                postAccount();
        }
    }


    /**
     * 发送登录信息并处理
     */
    private void postAccount() {
        Login login = checkInput();//检查输入
        if (login != null) {//若用户的登录信息形式正确则发送post请求到服务器
            //通过Post请求发送Json数据并接收返回的数据
            if (isNetworkAvailable(LoginActivity.this)) {
                startLoginThread(login);
            } else{
                handler.sendEmptyMessage(NETWORK_UNAVAILABLE);
            }

        }
    }


    /**
     * 检查用户的输入并返回登录信息
     * @return 登录信息
     */
    private Login checkInput() {
        //获取用户名与密码
        username = inputUsername.getText().toString();
        password = inputPassword.getText().toString();

        //若帐户名与密码不为空则实例化login，否则抛出“不可为空”的提示并返回空信息
        if ((username != null && username.length() > 0) && (password != null && password.length() > 0)&&companyName!=null) {
            Login login = new Login();
            login.setUsername(username);
            login.setPassword(password);
            login.setCompanyName(companyName);
            return login;
        } else {
            Snackbar.make(snackbarContainer, "公司、用户名与密码不可为空", Snackbar.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 启动公司信息线程
     */
    private void startCompanyThread() {
        companyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.w(TAG, "run: company", null);
                    //用于接收的对象
                    List<String> maps = new ArrayList<String>();

                    //发送请求
                    RestTemplate restTemplate = new RestTemplate();
                    maps = restTemplate.getForObject(getResources().getString(R.string.URL_LOGIN_COMPANY), maps.getClass());

                    //处理请求的数据
                    if(maps != null && maps.size()>0) {
                        companyList.clear();
                        companyList.addAll(maps);
                        handler.sendEmptyMessage(COMPANY_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(COMPANY_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(COMPANY_ERROR);
                }
            }
        });
        companyThread.start();
    }

    /**
     * 启动登录信息线程
     */
    private void startLoginThread(final Login login) {
        loginThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.w(TAG, "run: login", null);
                try {

                    //用于接收的对象
                    Map<String, String> map = new HashMap<String, String>();

                    //发送请求
                    RestTemplate restTemplate = new RestTemplate();
                    map = restTemplate.postForObject(getResources().getString(R.string.URL_LOGIN), login, map.getClass());

                    //处理请求的数据
                    check = map.get("check");
                    company = map.get("company");

                    Log.e(TAG, "run: check is "+check+", company is "+company, null);

                    if (check != null && company != null) {
                        handler.sendEmptyMessage(LOGIN_SUCCESS);
                    } else {
                        handler.sendEmptyMessage(LOGIN_FAIL);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(LOGIN_ERROR);

                }
            }

        });
        loginThread.start();
    }

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
                        startCompanyThread();
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

}
