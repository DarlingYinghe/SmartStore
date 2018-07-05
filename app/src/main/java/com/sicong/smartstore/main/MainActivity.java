package com.sicong.smartstore.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.data.model.Statistic;
import com.sicong.smartstore.stock_in.view.StockInFragment;
import com.sicong.smartstore.stock_in.view.StockOutFragment;
import com.sicong.smartstore.stock_in.data.model.Cargo;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private String operatorId = "16905121";//操作人员的id
    private String check;//校验码
    private List<Statistic> statistics;//统计结果



    private ViewPager pagers;//分页
    private BottomNavigationView bottomNav;//底部导航栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化控件
        initPagers();//初始化分页
        initBottomNav();//初始化底部导航栏
        initRecevicerFromScan();//
    }

    private void initRecevicerFromScan() {
        Intent intent = getIntent();
        if(intent!=null&&intent.hasExtra("statisticList")) {

        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        pagers = (ViewPager)findViewById(R.id.main_pagers);
        bottomNav = (BottomNavigationView)findViewById(R.id.main_bottom_nav);
    }

    /**
     * 初始化分页
     */
    private void initPagers() {
        //设置屏幕外分页数量
        pagers.setOffscreenPageLimit(1);

        //适配器
        pagers.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            Fragment[] fragments = {
                new StockInFragment(), new StockOutFragment()
            };

            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        });

        //页面切换事件
        pagers.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //图标Id
            int[] itemIds = {
                    R.id.stock_in,
                    R.id.stock_out
            };

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNav.setSelectedItemId(itemIds[position]);
                Log.w(TAG, "onPageSelected: Pager"+position, null);
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
                }
                Log.w(TAG, "onNavigationItemSelected: Item"+position, null);
                //选择分页的页码
                pagers.setCurrentItem(position);
                //设置当前图标为选中状态
                item.setChecked(true);
                return false;
            }
        });
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }


}
