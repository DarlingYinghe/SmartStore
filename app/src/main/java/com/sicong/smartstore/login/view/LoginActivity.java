package com.sicong.smartstore.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.login.data.model.Account;

import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText inputUsername;//用户名的输入框
    private TextInputEditText inputPassword;//密码的输入框
    private AppCompatButton btnLogin;//登录按钮
    private CoordinatorLayout snackbarContainer;//Snackbar的容器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();//初始化控件
        initEnter();//登录操作
    }



    /**
     * 初始化控件
     */
    private void initView() {
        inputUsername = (TextInputEditText) findViewById(R.id.login_username);
        inputPassword = (TextInputEditText) findViewById(R.id.login_password);
        btnLogin = (AppCompatButton) findViewById(R.id.login_enter);
        snackbarContainer = (CoordinatorLayout) findViewById(R.id.login_container);
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
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("operatorId", "1");
                startActivity(new Intent(this, MainActivity.class));
                /*new Thread(new EnterRunable()).start();*/
        }
    }


    /**
     * 点击登录按钮启动的Runable
     */
    private class EnterRunable implements Runnable {

        @Override
        public void run() {
            Account account = checkInput();//检查输入
            if (account != null) {//若用户的登录信息形式正确则发送post请求到服务器
                postAccount(account);
            }
        }
    }

    /**
     * 发送登录信息并处理
     * @param account 登录信息
     */
    private void postAccount(Account account) {
        String url = "";
        //实例化RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        //通过Post请求发送Json数据并接收返回的数据
        try {
            String response = restTemplate.postForObject(url, account, String.class);
            JSONObject resultJson = new JSONObject(response);
            int result = resultJson.getInt("result");
            switch (result) {
                case 0://信息错误
                    Snackbar.make(snackbarContainer, "您输入的信息有误，请重新输入", Snackbar.LENGTH_SHORT).show();
                    break;
                case 1://信息正确
                    startActivity(new Intent(this, MainActivity.class));
                    break;
                case -1://信息异常
                    Snackbar.make(snackbarContainer, "服务器响应异常", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {//处理请求过程中可能出现的异常
            e.printStackTrace();
            Snackbar.make(snackbarContainer, "服务器请求异常，请检查网络", Snackbar.LENGTH_SHORT).show();
        }
    }


    /**
     * 检查用户的输入并返回登录信息
     * @return 登录信息
     */
    private Account checkInput() {
        //获取用户名与密码
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();

        //若帐户名与密码不为空则实例化account，否则抛出“不可为空”的提示并返回空信息
        if ((username != null && username.length() > 0) && (password != null && password.length() > 0)) {
            Account account = new Account();
            account.setUsername(username);
            account.setPassword(password);
            return account;
        } else {
            Snackbar.make(snackbarContainer, "用户名与密码不可为空", Snackbar.LENGTH_SHORT).show();
            return null;
        }
    }
}
