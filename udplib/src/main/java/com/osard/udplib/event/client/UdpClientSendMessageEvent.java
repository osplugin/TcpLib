package com.osard.udplib.event.client;


import com.osard.udplib.event.UdpBaseEvent;

/**
 * 用途：客户端向服务器发送消息事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/06/04
 */
public class UdpClientSendMessageEvent extends UdpBaseEvent {

    /**
     * 发送的原始数据
     */
    private Object content;

    /**
     * @param servicePort 服务端口
     * @param address     服务器地址，serverIp:(serverPort)
     */
    public UdpClientSendMessageEvent(int servicePort, String address) {
        super(servicePort, address);
    }

    public UdpClientSendMessageEvent(String address, Object content) {
        super(address);
        this.content = content;
    }

    /**
     * 发送的原始数据
     */
    public Object getContent() {
        return content;
    }

}
