package com.osard.udplib.event.service;


import com.osard.udplib.event.UdpBaseEvent;

/**
 * 用途：服务端接收到客户端连接触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpClientConnectEvent extends UdpBaseEvent {

    /**
     * @param servicePort 服务端口
     * @param address     客户端地址，clientIp:(clientPort)
     */
    public UdpClientConnectEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
