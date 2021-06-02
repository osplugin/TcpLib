package com.mjsoftking.tcplib.event.service;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：服务端接收到客户端连接断开触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpClientDisconnectEvent extends TcpBaseEvent {

    /**
     * @param servicePort 服务端口
     * @param address     客户端地址，clientIp:(clientPort)
     */
    public TcpClientDisconnectEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
