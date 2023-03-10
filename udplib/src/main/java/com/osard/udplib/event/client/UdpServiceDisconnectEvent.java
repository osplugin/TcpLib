package com.osard.udplib.event.client;


import com.osard.udplib.event.UdpBaseEvent;

/**
 * 用途：客户端连接服务端断开触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpServiceDisconnectEvent extends UdpBaseEvent {

    /**
     * @param servicePort 服务端口
     * @param address     服务器地址，serverIp:(serverPort)
     */
    public UdpServiceDisconnectEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
