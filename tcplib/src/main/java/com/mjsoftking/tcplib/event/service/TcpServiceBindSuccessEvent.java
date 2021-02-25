package com.mjsoftking.tcplib.event.service;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：服务端绑定启动触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceBindSuccessEvent extends TcpBaseEvent {

    public TcpServiceBindSuccessEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
