package com.example.yqhok.coolweather.base.adapter;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yqhok on 2017-04-12.
 */

public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder> {

    private List<T> dataList = new ArrayList<>();
    protected OnItemClickListener<T> listener;

    public interface OnItemClickListener<T> {
        void onClick(T t, int position);
    }

    public void addAll(List<T> data) {
        this.dataList.addAll(data);
    }

    public void removeAll() {
        this.dataList.clear();
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        holder.onBaseBindViewHolder(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.listener = listener;
    }

}
