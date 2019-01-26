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

public class ChangeDetailAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Map<String, Object>> mList;
    private int curItem = -1;

    public ChangeDetailAdapter(@NonNull Context mContext, @NonNull List<Map<String, Object>> mList) {
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

        Map<String,Object> map = mList.get(position);
        if(map.get("count")==map.get("num")) {
            map.put("over", true);
        }

        String name = (String)map.get("product_name");
        String inPosition = (String)map.get("warehouse_name");
        String num = map.get("count")+"/"+map.get("num");

        view.name.setText(name);
        view.num.setText(num);
        view.inPosition.setText(inPosition);


        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurItem(position);
            }
        });

        if(curItem!=-1) {
            if(curItem == position) {
                holder.itemView.setBackgroundResource(R.color.colorPrimary);
                view.name.setTextColor(mContext.getResources().getColor(R.color.white));
                view.num.setTextColor(mContext.getResources().getColor(R.color.white));
                view.inPosition.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                holder.itemView.setBackgroundResource(R.color.white);
                view.name.setTextColor(mContext.getResources().getColor(R.color.black));
                view.num.setTextColor(mContext.getResources().getColor(R.color.black));
                view.inPosition.setTextColor(mContext.getResources().getColor(R.color.black));
            }
        }


    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;//货物名称
        TextView inPosition;//目标位置
        TextView num;//总数

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_stock_change_detail_name);
            num = (TextView)itemView.findViewById(R.id.item_stock_change_detail_num);
            inPosition = (TextView)itemView.findViewById(R.id.item_stock_change_detail_in_position);
        }
    }

    public void setCurItem(int position) {
        this.curItem = position;
        notifyDataSetChanged();
    }

    public int getCurItem() {
        return curItem;
    }

    public void changeCurItemCount(int position) {
        notifyItemChanged(position);
    }

    public List<Map<String, Object>> getmList() {
        return mList;
    }
}
