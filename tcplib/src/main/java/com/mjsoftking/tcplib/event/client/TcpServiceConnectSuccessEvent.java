package com.mjsoftking.tcplib.event.client;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：客户端连接服务端成功触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceConnectSuccessEvent extends TcpBaseEvent {

    /**
     * @param servicePort 服务端口
     * @param address     服务器地址，serverIp:(serverPort)
     */
    public TcpServiceConnectSuccessEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
