package com.sicong.smartstore.stock_user.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicong.smartstore.R;
import com.sicong.smartstore.stock_out.view.OutActivity;

import java.util.List;

public class UnOverCatalogAdapter extends RecyclerView.Adapter {

    private static final String TAG = "UnOverCatalogAdapter";

    private List<String> mList;
    private Context mContext;
    private String check;
    private String company;
    private String username;
    private int[] icons;

    public UnOverCatalogAdapter(@NonNull Context mContext, @NonNull List<String> mList, String check, String company, String username, int[] icons) {
        this.mContext = mContext;
        this.mList = mList;
        this.check = check;
        this.company = company;
        this.username = username;
        this.icons = icons;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UnOverCatalogAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.name.setText(mList.get(position));
        viewHolder.imageView.setBackgroundResource(icons[position]);

        //点击事件
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(mContext, OutActivity.class);
                    intent.putExtra("check", check);
                    intent.putExtra("company", company);
                    intent.putExtra("username", username);
                    intent.putExtra("type", position);
                    mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;//名称
        ImageView imageView;//日期

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_user_tv);
            imageView = itemView.findViewById(R.id.item_user_ic);
        }

    }
}
