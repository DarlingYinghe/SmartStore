package com.sicong.smartstore.stock_in.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_in.view.InActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InListAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private List<Map<String, String>> mList;

    public InListAdapter(Context mContext, List<Map<String, String>> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_stock_in, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Map<String, String> map = mList.get(position);
        viewHolder.id.setText(map.get("id"));
        viewHolder.date.setText(map.get("date"));
        viewHolder.title.setText(map.get("title"));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, InActivity.class);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView id;
        private TextView date;
        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.item_stock_in_id);
            date = itemView.findViewById(R.id.item_stock_in_date);
            title = itemView.findViewById(R.id.item_stock_in_title);
        }
    }
}
