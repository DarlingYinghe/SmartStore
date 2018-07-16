package com.sicong.smartstore.stock_check.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_check.view.CheckActivity;

import java.util.List;
import java.util.Map;

public class CheckListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "OutListAdapter";

    private Context mContext;
    private List<Map<String, String>> mList;
    private String check;
    private String company;
    private String username;

    public CheckListAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList, String check, String company, String username) {
        this.mContext = mContext;
        this.mList = mList;
        this.check = check;
        this.company = company;
        this.username = username;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_check, parent, false));
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

            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, CheckActivity.class);
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

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.item_stock_check_id);
            date = (TextView) itemView.findViewById(R.id.item_stock_check_date);
            title = (TextView)itemView.findViewById(R.id.item_stock_check_title);
        }
    }


}
