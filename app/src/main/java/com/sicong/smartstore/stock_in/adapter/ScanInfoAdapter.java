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

    private static final String TAG = "ScanInfoAdapter";
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
        view.type.setText(mList.get(position).getTypeSecond());
        view.epc.setText(mList.get(position).getRfid());
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView id;//扫描的顺序编号的视图
        TextView type;//物品类型的视图
        TextView epc;//rfid码的视图

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.rifd_id);
            type = (TextView) itemView.findViewById(R.id.rfid_type);
            epc = (TextView) itemView.findViewById(R.id.rfid_epc);
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
