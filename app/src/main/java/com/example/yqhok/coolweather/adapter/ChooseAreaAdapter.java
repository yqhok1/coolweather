package com.example.yqhok.coolweather.adapter;

import android.content.Context;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yqhok.coolweather.R;
import com.example.yqhok.coolweather.base.adapter.BaseRecyclerViewAdapter;
import com.example.yqhok.coolweather.base.adapter.BaseRecyclerViewHolder;
import com.example.yqhok.coolweather.databinding.ItemChooseAreaBinding;

/**
 * Created by yqhok on 2017/5/31.
 */

public class ChooseAreaAdapter extends BaseRecyclerViewAdapter<String> {

    private Context context;

    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        return new ViewHolder(parent, R.layout.item_choose_area);
    }

    private class ViewHolder extends BaseRecyclerViewHolder<String, ItemChooseAreaBinding> {

        ViewHolder(ViewGroup parent, int layout) {
            super(parent, layout);
        }

        @Override
        public void onBindViewHolder(final String s, final int position) {
            if (s != null) {
                binding.setData(s);
                TextView areaName = binding.areaName;
                TextPaint paint = areaName.getPaint();
                paint.setFakeBoldText(true);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onClick(s, position);
                        }
                    }
                });
            }
        }
    }
}
