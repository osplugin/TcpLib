package com.mjsoftking.tcplib.thread;


import android.util.Log;

import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.client.TcpServiceDisconnectEvent;
import com.mjsoftking.tcplib.event.service.TcpClientDisconnectEvent;
import com.mjsoftking.tcplib.list.ByteQueueList;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * 用途：数据接收线程，将数据存入缓冲区后由数据处理线程处理
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpDataReceiveThread extends Thread {

    private final static String TAG = TcpDataReceiveThread.class.getSimpleName();

    private final int servicePort;
    private final Socket client;
    private final String address;
    private final Map<String, TcpDataBuilder> clientMap;

    private final ByteQueueList bufferQueue;
    private final TcpBaseDataDispose dataDispose;

    private final boolean isClient;

    private TcpDataDisposeThread dataDisposeThread;

    /**
     * 构造方法
     */
    public TcpDataReceiveThread(int servicePort, String address, Map<String, TcpDataBuilder> clientMap, boolean isClient) {
        this.servicePort = servicePort;

        TcpDataBuilder builder = clientMap.get(address);
        if (null != builder) {
            this.client = builder.getSocket();
            this.dataDispose = builder.getDataDispose();
        } else {
            this.client = null;
            this.dataDispose = null;
        }
        this.address = address;
        this.clientMap = clientMap;
        this.bufferQueue = new ByteQueueList();
        this.isClient = isClient;
    }

    @Override
    public void run() {
        if (null == client) return;

        while (true) {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int bufferLength = client.getInputStream().read(buffer);
                if (bufferLength <= 0) {
                    throw new IOException("Socket closed");
                }
                byte[] dataBuffer = new byte[bufferLength];
                System.arraycopy(buffer, 0, dataBuffer, 0, bufferLength);
                bufferQueue.addAll(dataBuffer);

                if (null == dataDisposeThread || !dataDisposeThread.isAlive()) {
                    dataDisposeThread = null;
                    dataDisposeThread = new TcpDataDisposeThread(this.servicePort, address, bufferQueue, dataDispose);
                    dataDisposeThread.start();
                }
            } catch (Exception e) {
                if ("Socket closed".equals(e.getMessage())) {
                    if (TcpLibConfig.getInstance().isDebugMode()) {
                        Log.w(TAG, "连接中断," + (isClient ? "服务器地址：" : "客户端器地址：") + address);
                    }
                } else {
                    if (TcpLibConfig.getInstance().isDebugMode()) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                //客户端已经断开
                clientMap.remove(address);
                if (isClient) {
                    // 发送与服务器断开事件
                    EventBus.getDefault().post(new TcpServiceDisconnectEvent(this.servicePort, address));
                } else {
                    //发送客户端下线事件
                    EventBus.getDefault().post(new TcpClientDisconnectEvent(this.servicePort, address));
                }
                return;
            }
        }


    }
}