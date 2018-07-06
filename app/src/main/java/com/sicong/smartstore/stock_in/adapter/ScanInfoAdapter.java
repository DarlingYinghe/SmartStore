package com.sicong.smartstore.stock_in.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.data.model.Cargo;

import java.util.List;

public class ScanInfoAdapter extends RecyclerView.Adapter {

    private static final String TAG = "StockOutListAdapter";
    private Context mContext;
    private List<Cargo> mList;

    public ScanInfoAdapter(@NonNull Context mContext, @NonNull List<Cargo> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rfid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder view = (ViewHolder) holder;

        view.id.setText(String.valueOf(position+1));
        view.type_first.setText(mList.get(position).getTypeFirst());
        view.type_second.setText(mList.get(position).getTypeSecond());
        view.rfid.setText(mList.get(position).getRfid());
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView id;//扫描的顺序编号的视图
        TextView type_first;//一级物品类型的视图
        TextView type_second;//二级物品类型的视图
        TextView rfid;//rfid码的视图

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.rfid_id);
            type_first = (TextView) itemView.findViewById(R.id.rfid_type_first);
            type_second=(TextView) itemView.findViewById(R.id.rfid_type_second);
            rfid = (TextView) itemView.findViewById(R.id.rfid_rfid);
        }
    }

    public void insert(Cargo cargo) {
        mList.add(cargo);
        notifyItemInserted(mList.size() - 1);
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }
}
