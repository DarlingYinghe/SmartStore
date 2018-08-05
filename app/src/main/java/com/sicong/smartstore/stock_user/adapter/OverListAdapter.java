package com.sicong.smartstore.stock_user.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;

import java.util.List;
import java.util.Map;

public class OverListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "OverListAdapter";

    private List<Map<String, String>> mList;
    private Context mContext;
    private String check;
    private String company;
    private String username;

    public OverListAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList, String check, String company, String username) {
        this.mContext = mContext;
        this.mList = mList;
        this.check = check;
        this.company = company;
        this.username = username;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_user_over, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.id.setText(mList.get(position).get("id"));
        viewHolder.date.setText(mList.get(position).get("date"));
        viewHolder.title.setText(mList.get(position).get("title"));

        //点击事件
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("check", check);
                intent.putExtra("company", company);
                intent.putExtra("username", username);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView id;//编号
        TextView date;//日期
        TextView title;//标题

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.item_user_over_id);
            date = itemView.findViewById(R.id.item_user_over_date);
            title = itemView.findViewById(R.id.item_user_over_title);
        }

    }

}
