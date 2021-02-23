package com.mjsoftking.tcplib.tcpthread;


import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.event.client.TcpServiceDisconnectEvent;
import com.mjsoftking.tcplib.list.ByteQueueList;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpClientDataReceiveThread extends Thread {

    private final static String TAG = TcpClientDataReceiveThread.class.getSimpleName();

    private final Socket service;
    private final String address;
    private final Map<String, Socket> serviceMap;

    private final ByteQueueList bufferQueue;
    private final TcpBaseDataDispose dataDispose;

    private TcpDataDisposeThread dataDisposeThread;

    /**
     * 构造方法
     */
    public TcpClientDataReceiveThread(Socket service, String address, Map<String, Socket> serviceMap, TcpBaseDataDispose dataDispose) {
        this.service = service;
        this.address = address;
        this.serviceMap = serviceMap;
        this.dataDispose = dataDispose;
        this.bufferQueue = new ByteQueueList();
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                int bufferLength = service.getInputStream().read(buffer);
                if (bufferLength <= 0) {
                    //todo 暂定
                    throw new IOException("认为客户端断开了");
                }
                byte[] dataBuffer = new byte[bufferLength];
                System.arraycopy(buffer, 0, dataBuffer, 0, bufferLength);
                bufferQueue.addAll(dataBuffer);

                if (null == dataDisposeThread || !dataDisposeThread.isAlive()) {
                    dataDisposeThread = null;
                    dataDisposeThread = new TcpDataDisposeThread(address, bufferQueue, dataDispose);
                    dataDisposeThread.start();
                }
            } catch (Exception e) {
//                Log.e(TAG, "客户端连接中断", e);
                //服务器已经断开
                serviceMap.remove(address);
                //todo 发送服务器下线事件
                EventBus.getDefault().post(new TcpServiceDisconnectEvent(address));
                return;
            }
        }


    }
}