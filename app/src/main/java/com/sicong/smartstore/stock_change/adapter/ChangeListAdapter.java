package com.sicong.smartstore.stock_change.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_change.view.ChangeActivity;
import com.sicong.smartstore.util.CheckStatue;

import java.util.List;
import java.util.Map;

public class ChangeListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "OutListAdapter";

    private Context mContext;
    private List<Map> mList;
    private String check;
    private String company;
    private String username;

    public ChangeListAdapter(@NonNull Context mContext, @NonNull List<Map> mList, String check, String company, String username) {
        this.mContext = mContext;
        this.mList = mList;
        this.check = check;
        this.company = company;
        this.username = username;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_change, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ViewHolder view = (ViewHolder) holder;
        final String id = (String)mList.get(position).get("id");
        String date = (String)mList.get(position).get("date");
        String title = (String)mList.get(position).get("title");
        view.id.setText(id);
        view.date.setText(date);
        view.title.setText(title);
         view.status.setText(CheckStatue.checkStatus(mList.get(position).get("status").toString()));

            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ChangeActivity.class);
                    intent.putExtra("check", check);
                    intent.putExtra("id", id);
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

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView id;//单号
        TextView date;//日期
        TextView title;//标题
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.item_stock_change_id);
            date = (TextView) itemView.findViewById(R.id.item_stock_change_date);
            title = (TextView)itemView.findViewById(R.id.item_stock_change_title);
            status = itemView.findViewById(R.id.item_stock_change_status);
        }
    }


}
