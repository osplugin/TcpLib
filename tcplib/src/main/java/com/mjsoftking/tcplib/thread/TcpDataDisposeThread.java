package com.mjsoftking.tcplib.thread;


import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpDataDisposeThread extends Thread {

    private final String clientIpAddress;
    private final ByteQueueList bufferQueue;
    private final TcpBaseDataDispose dataDispose;


    public TcpDataDisposeThread(String clientIpAddress, ByteQueueList bufferQueue, TcpBaseDataDispose dataDispose) {
        this.clientIpAddress = clientIpAddress;
        this.bufferQueue = bufferQueue;
        this.dataDispose = dataDispose;
    }

    @Override
    public void run() {
        while (bufferQueue.size() > 0) {
            dataDispose.dispose(clientIpAddress, bufferQueue);
        }
    }
}