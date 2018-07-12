package com.sicong.smartstore.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_change.view.StockChangeFragment;
import com.sicong.smartstore.stock_check.view.StockCheckFragment;
import com.sicong.smartstore.stock_in.data.model.Statistic;
import com.sicong.smartstore.stock_in.view.StockInFragment;
import com.sicong.smartstore.stock_out.view.StockOutFragment;
import com.sicong.smartstore.util.network.NetBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //基本变量
    private final static String TAG = "MainActivity";


    //数据
    private String username;//操作人员的id
    private String check;//校验码
    private String company;//公司
    private List<Statistic> statisticList;//统计结果


    //视图
    private ViewPager pagers;//分页
    private BottomNavigationView bottomNav;//底部导航栏
    private List<Fragment> fragments;
    private StockInFragment stockInFragment;
    private StockOutFragment stockOutFragment;
    private StockChangeFragment stockChangeFragment;
    private StockCheckFragment stockCheckFragment;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化控件
        initPagers();//初始化分页
        initBottomNav();//初始化底部导航栏

        initNetBoardcastReceiver();//初始化广播
    }

    private void initRecevicerFromScan() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("statisticList")) {
            statisticList = (List<Statistic>) intent.getSerializableExtra("statisticList");
        } else if (intent != null && intent.hasExtra("check")) {
            check = intent.getStringExtra("check");
        } else if (intent != null && intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        } else if (intent != null && intent.hasExtra("company")) {
            company = intent.getStringExtra("company");
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        pagers = (ViewPager) findViewById(R.id.main_pagers);
        bottomNav = (BottomNavigationView) findViewById(R.id.main_bottom_nav);
    }

    /**
     * 初始化分页
     */
    private void initPagers() {
        //设置屏幕外分页数量
        pagers.setOffscreenPageLimit(3);

        stockInFragment = new StockInFragment();
        stockOutFragment = new StockOutFragment();
        stockChangeFragment = new StockChangeFragment();
        stockCheckFragment = new StockCheckFragment();

        fragments = new ArrayList<Fragment>();
        fragments.add(stockInFragment);
        fragments.add(stockOutFragment);
        fragments.add(stockChangeFragment);
        fragments.add(stockCheckFragment);

        //适配器
        pagers.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });

        //页面切换事件
        pagers.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //图标Id
            int[] itemIds = {
                    R.id.stock_in,
                    R.id.stock_out,
                    R.id.stock_change
            };

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNav.setSelectedItemId(itemIds[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化底部导航栏
     */
    private void initBottomNav() {

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //初始化当前位置
                int position = 0;
                //根据item获取当前位置的页码
                switch (item.getItemId()) {
                    case R.id.stock_in:
                        position = 0;
                        break;
                    case R.id.stock_out:
                        position = 1;
                        break;
                    case R.id.stock_change:
                        position = 2;
                        break;
                    case R.id.stock_check:
                        position = 3;
                        break;
                }
                //选择分页的页码
                pagers.setCurrentItem(position);
                //设置当前图标为选中状态
                item.setChecked(true);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecevicerFromScan();
        initNetBoardcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);
    }


    public String getUsername() {
        return username;
    }


    public String getCheck() {
        return check;
    }

    public String getCompany() {
        return company;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public List<Statistic> getStatisticList() {
        return statisticList;
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
                        Log.e(TAG, "onChangeListener: 可行", null);
                        StockOutFragment stockOutFragmentTmp  = (StockOutFragment)fragments.get(1);
                        stockOutFragmentTmp.startRequestDataThread();

                        StockChangeFragment stockChangeFragment = (StockChangeFragment)fragments.get(2);
                        stockChangeFragment.startRequestDataThread();

                        StockCheckFragment stockCheckFragment = (StockCheckFragment)fragments.get(3);
                        stockCheckFragment.startRequestDataThread();

                    } else {
                        Toast.makeText(MainActivity.this, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, filter);
    }
}
