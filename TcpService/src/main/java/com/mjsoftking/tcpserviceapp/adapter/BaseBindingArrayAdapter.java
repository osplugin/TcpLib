package com.mjsoftking.tcpserviceapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseBindingArrayAdapter<D> extends ArrayAdapter<D> {

    @NonNull
    protected final List<D> list;

    /**
     * 布局ID
     */
    @LayoutRes
    public abstract int layoutResID();

    public BaseBindingArrayAdapter(@NonNull Context context) {
        super(context, 0);
        this.list = new ArrayList<>();
    }

    @Override
    public void add(D object) {
        this.list.add(object);
    }

    @Override
    public void addAll(@NonNull Collection<? extends D> collection) {
        this.list.addAll(collection);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public D getItem(int ix) {
        return list.get(ix);
    }

    @Override
    public int getPosition(D item) {
        return list.indexOf(item);
    }

    /**
     * 清除列表并更新显示
     */
    public void clearAndRefresh() {
        clear();
        notifyDataSetChanged();
    }

    /**
     * 添加一个并刷新
     */
    public void addAndRefresh(@Nullable D object) {
        add(object);
        notifyDataSetChanged();
    }

    /**
     * 添加一组并刷新
     */
    public void addAllAndRefresh(@NonNull Collection<? extends D> collection) {
        addAll(collection);
        notifyDataSetChanged();
    }

    /**
     * 获取绑定视图，如果是视图复用那就从视图中直接读取
     */
    protected <T extends ViewDataBinding> T getView(View convertView, ViewGroup parent) {
        T binding = null;
        if (convertView != null) {
            binding = DataBindingUtil.getBinding(convertView);
        }
        if (null == binding) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), layoutResID(), parent, false);
        }
        return binding;
    }

}
