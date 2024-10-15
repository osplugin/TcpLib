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
import java.io.InputStream;
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

        setPriority(Thread.MAX_PRIORITY);
    }

    long time;

    @Override
    public void run() {
        if (null == client) return;

        try {
            int bufferLength;
            byte[] buffer = new byte[TcpLibConfig.getInstance().getReceiveReadSize()];
            InputStream inputStream = client.getInputStream();
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                ///限制缓存区不可超出此大小，一旦超出需要等待处理线程处理缓存区
                while (bufferQueue.size() + bufferLength >= TcpLibConfig.getInstance().getReceiveCacheBufferSize())
                    ;
//                Log.e("TCP", "读取缓存区数据, 时间：" + (System.currentTimeMillis()) + "，数据大小：" + bufferLength);
//
                bufferQueue.add(bufferLength, buffer);

                if (null == dataDisposeThread || !dataDisposeThread.isAlive() || dataDisposeThread.isInterrupted()) {
                    if (null != dataDisposeThread) {
                        dataDisposeThread.interrupt();
                    }
                    dataDisposeThread = new TcpDataDisposeThread(this.servicePort, address, bufferQueue, dataDispose);
                    dataDisposeThread.start();
                }
            }

        } catch (Exception e) {
            String message = e.getMessage();
            if ("Socket is closed".equalsIgnoreCase(message) ||
                    "Socket input is shutdown".equalsIgnoreCase(message) ||
                    "Socket is not connected".equalsIgnoreCase(message) ||
                    "Socket closed".equalsIgnoreCase(message) ||
                    "Connection reset".equalsIgnoreCase(message) ||
                    "Read timed out".equalsIgnoreCase(message)) {
//                if (TcpLibConfig.getInstance().isDebugMode()) {
//                    Log.w(TAG, "连接中断," + (isClient ? "服务器地址：" : "客户端地址：") + address);
//                }
            } else {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        } finally {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "连接中断," + (isClient ? "服务器地址：" : "客户端地址：") + address);
            }
            //主动关闭一次
            try {
                client.close();
            } catch (IOException ignore) {
            }
            //已经断开连接
            //清空对应缓存区，延时清理，给处理器一点处理时间
            delayClear();
            //移除缓存队列
            clientMap.remove(address);
            if (isClient) {
                // 发送与服务器断开事件
                EventBus.getDefault().post(new TcpServiceDisconnectEvent(this.servicePort, address));
            } else {
                //发送客户端下线事件
                EventBus.getDefault().post(new TcpClientDisconnectEvent(this.servicePort, address));
            }
        }

    }

    private void delayClear() {
        new Thread(() -> {
            int time = 0;
            while (time < TcpLibConfig.getInstance().getRetentionTime() * 60 * 10) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignore) {
                }
                //如果在延时等待期间处理完缓存区数据，则立即结束
                if (bufferQueue.size() <= 0) {
                    return;
                }
                ++time;
            }

            //超出延时等待处理时间时，立即清理缓存区数据，避免处理线程一直处于工作状态，增加机器耗电
            if (bufferQueue.size() > 0) {
                bufferQueue.clear();
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.w(TAG, (isClient ? "服务器地址：" : "客户端地址：") + address + "断开后, 达到设置的缓存清理时间，自动清理缓存区");
                }
            }
        }).start();
    }
}