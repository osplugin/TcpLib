package com.mjsoftking.tcplib.tcpthread;


import android.util.Log;

import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.dispose.TcpDataDisposeBuilder;
import com.mjsoftking.tcplib.event.client.TcpServiceDisconnectEvent;
import com.mjsoftking.tcplib.event.service.TcpClientDisconnectEvent;
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
public class TcpDataReceiveThread extends Thread {

    private final static String TAG = TcpDataReceiveThread.class.getSimpleName();

    private final Socket client;
    private final String address;
    private final Map<String, TcpDataDisposeBuilder> clientMap;

    private final ByteQueueList bufferQueue;
    private final TcpBaseDataDispose dataDispose;

    private final boolean isClient;

    private TcpDataDisposeThread dataDisposeThread;

    /**
     * 构造方法
     */
    public TcpDataReceiveThread(String address, Map<String, TcpDataDisposeBuilder> clientMap, boolean isClient) {
        this.client = clientMap.get(address).getSocket();
        this.address = address;
        this.clientMap = clientMap;
        this.dataDispose = clientMap.get(address).getDataDispose();
        this.bufferQueue = new ByteQueueList();
        this.isClient = isClient;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                int bufferLength = client.getInputStream().read(buffer);
                if (bufferLength <= 0) {
                    throw new IOException("客户端断开了");
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
                Log.w(TAG, "客户端连接中断", e);
                //客户端已经断开
                clientMap.remove(address);
                if (isClient) {
                    // 发送与服务器断开事件
                    EventBus.getDefault().post(new TcpServiceDisconnectEvent(address));
                } else {
                    //发送客户端下线事件
                    EventBus.getDefault().post(new TcpClientDisconnectEvent(address));
                }
                return;
            }
        }


    }
}