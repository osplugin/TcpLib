package com.osard.udplib.thread;


import com.osard.udplib.UdpLibConfig;
import com.osard.udplib.dispose.UdpDataBuilder;
import com.osard.udplib.event.service.UdpServiceCloseEvent;
import com.osard.udplib.list.ByteQueueList;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Locale;

/**
 * 用途：服务器启动后开启的等待客户端连接线程
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpDataReceiveThread extends Thread {

    private final static String TAG = UdpDataReceiveThread.class.getSimpleName();
    private final static String IP_ADDRESS = "%s:%d";

    private final ByteQueueList bufferQueue;
    private final DatagramSocket serverSocket;
    private final UdpDataBuilder builder;
    private final int servicePort;
    private UdpDataDisposeThread dataDisposeThread;
    private final boolean client;

    public UdpDataReceiveThread(DatagramSocket serverSocket, UdpDataBuilder builder, boolean client) {
        this.bufferQueue = new ByteQueueList();
        this.servicePort = serverSocket.getLocalPort();
        this.serverSocket = serverSocket;
        this.builder = builder;
        this.client = client;

        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[UdpLibConfig.getInstance().getReceiveCacheBufferSize()];
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(p);

                ///限制缓存区不可超出此大小，一旦超出需要等待处理线程处理缓存区
                while ((bufferQueue.size() + p.getLength()) >= (1024 * 1024))
                    ;
//                this.bufferQueue = new ByteQueueList(p.getLength());
                bufferQueue.add(p.getLength(), p.getData());

                String address = String.format(Locale.getDefault(), IP_ADDRESS, p.getAddress().getHostAddress(), p.getPort());
//                builder.getDataDispose().dispose(bufferQueue, servicePort, address);

                if (null == dataDisposeThread || !dataDisposeThread.isAlive() || dataDisposeThread.isInterrupted()) {
                    if (null != dataDisposeThread) {
                        dataDisposeThread.interrupt();
                    }
                    dataDisposeThread = new UdpDataDisposeThread(this.servicePort, address, bufferQueue, builder.getDataDispose());
                    dataDisposeThread.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
//                if ("Socket closed".equals(e.getMessage())) {
//                    if (TcpLibConfig.getInstance().isDebugMode()) {
//                        Log.w(TAG, "服务监听关闭, 服务端口: " + servicePort);
//                    }
//                } else {
//                    if (TcpLibConfig.getInstance().isDebugMode()) {
//                        Log.e(TAG, e.getMessage(), e);
//                    }
//                }
//                //主动调用一次，确保关闭
//                try {
//                    serverSocket.close();
//                } catch (IOException ignore) {
//                }
                //发送服务器监听关闭事件
                EventBus.getDefault().post(new UdpServiceCloseEvent(this.servicePort, String.format(Locale.getDefault(), IP_ADDRESS, "0.0.0.0", servicePort)));
                return;
            }
        }
    }
}
