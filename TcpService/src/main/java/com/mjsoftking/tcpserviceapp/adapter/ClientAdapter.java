package com.mjsoftking.tcpserviceapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mjsoftking.tcpserviceapp.R;
import com.mjsoftking.tcpserviceapp.bean.Client;
import com.mjsoftking.tcpserviceapp.databinding.AdapterClientBinding;

import java.util.Collection;


/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/03
 */
public class ClientAdapter extends BaseBindingArrayAdapter<Client> {

    private String currentClient;

    public ClientAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    public int layoutResID() {
        return R.layout.adapter_client;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AdapterClientBinding binding = super.getView(convertView, parent);
        binding.setClient(getItem(position));
        return binding.getRoot();
    }

    public boolean refreshSelect(Client c) {
        boolean select = false;
        for (Client client : list) {
            if (client.equals(c)) {
                client.setSelect(true);
                select = true;
                currentClient = c.getAddress();
            } else {
                client.setSelect(false);
            }
        }
        if (!select) {
            currentClient = null;
        }
        notifyDataSetChanged();
        return select;
    }

    public boolean refreshSelect(String address) {
        boolean select = false;
        for (Client client : list) {
            if (client.getAddress().equals(address)) {
                client.setSelect(true);
                select = true;
                currentClient = address;
            } else {
                client.setSelect(false);
            }
        }
        if (!select) {
            currentClient = null;
        }
        notifyDataSetChanged();
        return select;
    }

    public String getCurrentClient() {
        return currentClient;
    }

    /**
     * 添加一组并刷新
     */
    public void addAndRefresh(@NonNull Collection<String> collection) {
        for (String s : collection) {
            add(new Client(s, false));
        }
        notifyDataSetChanged();
    }
}
