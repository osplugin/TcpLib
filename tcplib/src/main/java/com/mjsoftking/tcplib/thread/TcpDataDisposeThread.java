package com.mjsoftking.tcplib.thread;


import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;

/**
 * 用途：对接收到数据缓冲区的数据进行处理的线程，具体处理规则由传入的处理器处理
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpDataDisposeThread extends Thread {

    private final String address;
    private final ByteQueueList bufferQueue;
    private final TcpBaseDataDispose dataDispose;
    private final int servicePort;

    public TcpDataDisposeThread(int servicePort, String address, ByteQueueList bufferQueue, TcpBaseDataDispose dataDispose) {
        this.servicePort = servicePort;
        this.address = address;
        this.bufferQueue = bufferQueue;
        this.dataDispose = dataDispose;
    }

    @Override
    public void run() {
        while (bufferQueue.size() > 0) {
            dataDispose.dispose(bufferQueue, servicePort, address);
        }
    }
}