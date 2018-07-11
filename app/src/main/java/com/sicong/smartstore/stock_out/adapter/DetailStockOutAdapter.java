package com.sicong.smartstore.stock_out.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_out.view.DetailActivity;

import java.util.List;
import java.util.Map;

public class DetailStockOutAdapter extends RecyclerView.Adapter {

    private static final String TAG = "StockOutListAdapter";
    private Context mContext;
    private List<Map<String, String>> mList;

    public DetailStockOutAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_out_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        ViewHolder view = (ViewHolder) holder;

        String name = (String)mList.get(position).get("name");
        String pos = (String)mList.get(position).get("position");
        String num = (String)mList.get(position).get("num");

        view.name.setText(name);
        view.position.setText(pos);
        view.num.setText(num);

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
        TextView position;//位置
        TextView num;//总数

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_stock_out_detail_name);
            position = (TextView)itemView.findViewById(R.id.item_stock_out_detail_position);
            num = (TextView)itemView.findViewById(R.id.item_stock_out_detail_num);
        }
    }


}
