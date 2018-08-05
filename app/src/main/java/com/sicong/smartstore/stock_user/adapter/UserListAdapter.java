package com.sicong.smartstore.stock_user.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.main.MainActivity;
import com.sicong.smartstore.stock_out.adapter.OutDetailAdapter;
import com.sicong.smartstore.stock_user.view.OverActivity;
import com.sicong.smartstore.stock_user.view.UnoverActivity;
import com.sicong.smartstore.stock_user.view.UserInfoActivity;

import java.util.List;
import java.util.Map;

public class  UserListAdapter extends RecyclerView.Adapter{

    private static final String TAG = "UserListAdapter";
    private List<Map<String, Object>> mList;
    private Context mContext;
    private String username;
    private String company;
    private String check;

    public UserListAdapter(List<Map<String, Object>> mList, Context mContext, String username, String company, String check) {
        this.mList = mList;
        this.mContext = mContext;
        this.username = username;
        this.company = company;
        this.check = check;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,final int position) {
        ViewHolder view = (ViewHolder) holder;
        
        Map<String,Object> map = mList.get(position);
        view.text.setText((Integer)map.get("text"));
        view.icon.setImageResource((Integer)map.get("icon"));

        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (position) {
                    case 0:
                        Intent intent0 = new Intent(mContext, UserInfoActivity.class);
                        intent0.putExtra("username", username);
                        intent0.putExtra("company", company);
                        intent0.putExtra("check", check);
                        mContext.startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1 = new Intent(mContext, OverActivity.class);
                        intent1.putExtra("username", username);
                        intent1.putExtra("company", company);
                        intent1.putExtra("check", check);
                        mContext.startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent(mContext, UnoverActivity.class);
                        intent2.putExtra("username", username);
                        intent2.putExtra("company", company);
                        intent2.putExtra("check", check);
                        mContext.startActivity(intent2);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;//条目名称
        ImageView icon;//条目图标

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_user_tv);
            icon = (ImageView) itemView.findViewById(R.id.item_user_ic);
        }
    }
}
