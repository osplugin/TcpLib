package com.mjsoftking.tcpserviceapp.bean;

import java.util.Objects;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/03
 */
public class Client {

    private String address;

    private boolean select;

    public Client(String address, boolean select) {
        this.address = address;
        this.select = select;
    }

    public Client() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return address.equals(client.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
