package com.mjsoftking.tcplib.event.service;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：服务器向客户端发送消息事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/06/04
 */
public class TcpServiceSendMessageEvent extends TcpBaseEvent {

    /**
     * 发送的原始数据
     */
    private Object content;

    /**
     * @param servicePort 服务端口
     * @param address     客户端地址，ip:port
     */
    public TcpServiceSendMessageEvent(int servicePort, String address) {
        super(servicePort, address);
    }

    public TcpServiceSendMessageEvent(int servicePort, String address, Object content) {
        super(servicePort, address);
        this.content = content;
    }

    /**
     * 发送的原始数据
     */
    public Object getContent() {
        return content;
    }

}
