package com.osard.udplib.bean;

import com.osard.udplib.dispose.UdpDataBuilder;
import com.osard.udplib.list.ByteQueueList;
import com.osard.udplib.thread.UdpDataDisposeThread;

import java.net.DatagramPacket;

public class ReceiveBean {

    private ByteQueueList bufferQueue;
    private UdpDataDisposeThread dataDisposeThread;
    private final UdpDataBuilder builder;


    public ReceiveBean(UdpDataBuilder builder) {
        this.builder = builder;
        this.bufferQueue = new ByteQueueList();
    }

    public void addBufferAndRun(DatagramPacket p, int servicePort, String address) {
        ///限制缓存区不可超出此大小，一旦超出需要等待处理线程处理缓存区
        while ((bufferQueue.size() + p.getLength()) >= bufferQueue.getMaxLength())
            ;

        bufferQueue.add(p.getOffset(), p.getLength(), p.getData());

        if (null == dataDisposeThread || !dataDisposeThread.isAlive() || dataDisposeThread.isInterrupted()) {
            if (null != dataDisposeThread) {
                dataDisposeThread.interrupt();
            }
            dataDisposeThread = new UdpDataDisposeThread(servicePort, address, bufferQueue, builder.getDataDispose());
            dataDisposeThread.start();
        }
    }

    public void destroy() {
        try {
            bufferQueue.clear();
            bufferQueue = null;
            if (null != dataDisposeThread && dataDisposeThread.isAlive()) {
                dataDisposeThread.interrupt();
                dataDisposeThread = null;
            }
        } catch (Exception ignore) {

        }
    }

}
