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
     * 发送文本数据时，此参数不为null
     */
    private Object contentStr;
    /**
     * 发送byte[]数据时，此参数不为null
     */
    private byte[] contentBytes;

    /**
     * @param servicePort 服务端口
     * @param address     服务器地址，serverIp:(serverPort)
     */
    public TcpServiceSendMessageEvent(int servicePort, String address) {
        super(servicePort, address);
    }

    public TcpServiceSendMessageEvent(String address, Object contentStr) {
        super(address);
        this.contentStr = contentStr;
        this.contentBytes = null;
    }

    public TcpServiceSendMessageEvent(String address, byte[] contentBytes) {
        super(address);
        this.contentStr = null;
        this.contentBytes = contentBytes;
    }

    /**
     * 发送文本数据时，此参数不为null
     */
    public Object getContentStr() {
        return contentStr;
    }

    /**
     * 发送byte[]数据时，此参数不为null
     */
    public byte[] getContentBytes() {
        return contentBytes;
    }
}
