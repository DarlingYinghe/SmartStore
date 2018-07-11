package com.sicong.smartstore.stock_change.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;

import java.util.List;
import java.util.Map;

public class DetailStockChangeAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Map<String, String>> mList;

    public DetailStockChangeAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_change_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        ViewHolder view = (ViewHolder) holder;

        String name = (String)mList.get(position).get("name");
        String outPosition = (String)mList.get(position).get("outPosition");
        String inPosition = (String)mList.get(position).get("inPosition");
        String num = (String)mList.get(position).get("num");

        view.name.setText(name);
        view.num.setText(num);
        view.outPosition.setText(outPosition);
        view.inPosition.setText(inPosition);


        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;//货物名称
        TextView outPosition;//原始位置
        TextView inPosition;//目标位置
        TextView num;//总数

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_stock_change_detail_name);
            num = (TextView)itemView.findViewById(R.id.item_stock_change_detail_num);
            outPosition = (TextView)itemView.findViewById(R.id.item_stock_change_detail_out_position);
            inPosition = (TextView)itemView.findViewById(R.id.item_stock_change_detail_in_position);
        }
    }


}
