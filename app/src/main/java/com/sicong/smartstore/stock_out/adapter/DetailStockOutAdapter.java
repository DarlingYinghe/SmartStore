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
    private List<Map<String, Object>> mList;

    public DetailStockOutAdapter(@NonNull Context mContext, @NonNull List<Map<String, Object>> mList) {
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

        String typeFirst = (String)mList.get(position).get("typeFirst");
        String typeSecond = (String)mList.get(position).get("typeSecond");
        String pos = (String)mList.get(position).get("position");
        Integer num = (Integer)mList.get(position).get("num");

        view.typeFirst.setText(typeFirst);
        view.typeSecond.setText(typeSecond);
        view.position.setText(pos);
        view.num.setText(String.valueOf(num));

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

        TextView typeFirst;//类型1
        TextView typeSecond;//类型2
        TextView position;//位置
        TextView num;//总数

        public ViewHolder(View itemView) {
            super(itemView);
            typeFirst = (TextView) itemView.findViewById(R.id.item_stock_out_detail_type_first);
            typeSecond = (TextView) itemView.findViewById(R.id.item_stock_out_detail_type_second);
            position = (TextView)itemView.findViewById(R.id.item_stock_out_detail_position);
            num = (TextView)itemView.findViewById(R.id.item_stock_out_detail_num);
        }
    }


}
