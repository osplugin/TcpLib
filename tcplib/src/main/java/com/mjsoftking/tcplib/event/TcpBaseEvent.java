package com.mjsoftking.tcplib.event;

import java.io.Serializable;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public abstract class TcpBaseEvent implements Serializable {

    protected String address;

    public TcpBaseEvent(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
