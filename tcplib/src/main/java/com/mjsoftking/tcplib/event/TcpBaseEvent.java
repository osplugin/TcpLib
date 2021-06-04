package com.mjsoftking.tcplib.event;

import java.io.Serializable;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public abstract class TcpBaseEvent implements Serializable {

    protected int servicePort;
    protected String address;

    /**
     * @param servicePort 服务端口
     * @param address     地址，ip:port
     */
    public TcpBaseEvent(int servicePort, String address) {
        this.servicePort = servicePort;
        this.address = address;
    }

    /**
     * @param address 地址，ip:port
     */
    public TcpBaseEvent(String address) {
        this.servicePort = Integer.parseInt(address.split(":")[1]);
        this.address = address;
    }

    public TcpBaseEvent() {
    }

    public String getAddress() {
        return address;
    }

    public int getServicePort() {
        return servicePort;
    }
}
