package com.osard.udplib.thread;


import com.osard.udplib.dispose.UdpBaseDataDispose;
import com.osard.udplib.list.ByteQueueList;

/**
 * 用途：对接收到数据缓冲区的数据进行处理的线程，具体处理规则由传入的处理器处理
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpDataDisposeThread extends Thread {

    private final String address;
    private final ByteQueueList bufferQueue;
    private final UdpBaseDataDispose dataDispose;
    private final int servicePort;


    public UdpDataDisposeThread(int servicePort, String address, ByteQueueList bufferQueue, UdpBaseDataDispose dataDispose) {
        this.servicePort = servicePort;
        this.address = address;
        this.bufferQueue = bufferQueue;
        this.dataDispose = dataDispose;

//        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (bufferQueue.size() > 0) {
                if (null != dataDispose) {
                    dataDispose.dispose(bufferQueue, servicePort, address);
                }
            }
        } catch (Exception ignore) {
        }
    }
}