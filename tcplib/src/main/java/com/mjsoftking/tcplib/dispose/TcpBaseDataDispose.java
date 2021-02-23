package com.mjsoftking.tcplib.dispose;

import com.mjsoftking.tcplib.list.ByteQueueList;


/**
 * 用途：接收数据处理器
 * <p>
 * 按照定义的报文格式解析缓冲区数据，解析数据后需要移除缓冲区对应的数据
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public interface TcpBaseDataDispose {

    void dispose(String clientAddress, ByteQueueList bufferQueue);

}
