package com.sicong.smartstore.stock_out.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.data.model.Cargo;
import com.sicong.smartstore.stock_out.model.CargoSendListMessage;
import com.sicong.smartstore.stock_out.view.DetailActivity;

import java.util.List;
import java.util.Map;

public class StockOutListAdapter extends RecyclerView.Adapter {

    private static final String TAG = "StockOutListAdapter";
    private Context mContext;
    private List<Map<String, String>> mList;

    public StockOutListAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_out, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ViewHolder view = (ViewHolder) holder;
        final String id = mList.get(position).get("id");
        String date = mList.get(position).get("date");
        view.id.setText(id);
        view.date.setText(date);
        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("id", id);
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

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.item_stock_out_id);
            date = (TextView) itemView.findViewById(R.id.item_stock_out_date);
        }
    }


}