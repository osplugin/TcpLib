package com.osard.udplib.dispose;


import com.osard.udplib.list.ByteQueueList;

/**
 * 用途：接收数据处理器
 * <p>
 * 按照定义的报文格式解析缓冲区数据，解析数据后需要移除缓冲区对应的数据
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public interface UdpBaseDataDispose {

    /**
     * 数据接收处理
     *
     * @param servicePort 服务器的端口，为服务端多开提供区分
     * @param address     服务器/客户端的连接地址，ip:port 形式，如：0.0.0.0:30000
     *                    服务器使用时，返回的是客户端的地址
     *                    客户端使用时，返回的是服务器的地址
     * @param bufferQueue 报文缓冲区，处理的报文需要移除队列
     */
    void dispose(ByteQueueList bufferQueue, int servicePort, String address);

}
