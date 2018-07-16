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
import com.sicong.smartstore.stock_in.data.model.Statistic;

import java.util.List;

public class InStatisticAdapter extends RecyclerView.Adapter {

    private static final String TAG = "OutListAdapter";
    private Context mContext;
    private List<Statistic> mList;

    public InStatisticAdapter(@NonNull Context mContext, @NonNull List<Statistic> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_statistic, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder view = (ViewHolder) holder;

        view.id.setText(String.valueOf(position+1));
        view.name.setText(mList.get(position).getName());
        view.num.setText(String.valueOf(mList.get(position).getNum()));
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView id;//扫描的顺序编号的视图
        TextView name;//货物名称的视图
        TextView num;//数量

        public ViewHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.statistic_id);
            name = (TextView) itemView.findViewById(R.id.statistic_name);
            num = (TextView) itemView.findViewById(R.id.statistic_num);
        }
    }

    public void insert(Statistic statistic) {
        mList.add(statistic);
        notifyItemInserted(mList.size() - 1);
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }
}
