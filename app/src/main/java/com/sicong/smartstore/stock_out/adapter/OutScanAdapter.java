package com.sicong.smartstore.stock_out.adapter;

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

public class OutScanAdapter extends RecyclerView.Adapter {

    private static final String TAG = "OutScanAdapter";

    private Context mContext;
    private List<Map<String, String>> mList;

    public OutScanAdapter(@NonNull Context mContext, @NonNull List<Map<String, String>> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_detail_scan, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ViewHolder view = (ViewHolder) holder;
        view.id.setText(String.valueOf(position+1));
        view.rfid.setText(mList.get(position).get("rfid"));
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView id;//扫描的顺序编号的视图
        TextView rfid;//rfid码的视图

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.item_detail_scan_id);
            rfid = (TextView) itemView.findViewById(R.id.item_detail_scan_rfid);
        }
    }

    public void insert(Map<String, String> str) {
        mList.add(str);
        notifyItemInserted(mList.size() - 1);
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }


}
