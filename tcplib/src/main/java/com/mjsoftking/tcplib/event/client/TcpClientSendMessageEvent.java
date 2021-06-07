package com.mjsoftking.tcplib.event.client;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：客户端向服务器发送消息事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/06/04
 */
public class TcpClientSendMessageEvent extends TcpBaseEvent {

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
    public TcpClientSendMessageEvent(int servicePort, String address) {
        super(servicePort, address);
    }

    public TcpClientSendMessageEvent(String address, Object contentStr) {
        super(address);
        this.contentStr = contentStr;
        this.contentBytes = null;
    }

    public TcpClientSendMessageEvent(String address, byte[] contentBytes) {
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
