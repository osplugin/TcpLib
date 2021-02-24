package com.mjsoftking.tcplib.thread;


import android.util.Log;

import com.mjsoftking.tcplib.dispose.TcpDataDisposeBuilder;
import com.mjsoftking.tcplib.event.service.TcpClientConnectEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceCloseEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * 用途：服务器启动后开启的等待客户端连接线程
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceAcceptThread extends Thread {

    private final static String TAG = TcpServiceAcceptThread.class.getSimpleName();

    private final ServerSocket serverSocket;
    private final Map<String, TcpDataDisposeBuilder> clientMap;
    private final TcpDataDisposeBuilder builder;
    private final int port;

    public TcpServiceAcceptThread(ServerSocket serverSocket, Map<String, TcpDataDisposeBuilder> clientMap, TcpDataDisposeBuilder builder) {
        this.port = serverSocket.getLocalPort();
        this.serverSocket = serverSocket;
        this.clientMap = clientMap;
        this.builder = builder;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                String address = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                //存入在线客户端缓存
                clientMap.put(address, builder.setSocket(client));

                //发送客户端上线事件
                EventBus.getDefault().post(new TcpClientConnectEvent(address));

                //对客户端开启数据接收线程
                TcpDataReceiveThread tcpDataReceiveThread = new TcpDataReceiveThread(address, clientMap, false);
                tcpDataReceiveThread.start();
            } catch (IOException e) {
                Log.e(TAG, "服务监听关闭", e);
                //发送服务器监听关闭事件
                EventBus.getDefault().post(new TcpServiceCloseEvent("0.0.0.0:" + port));
                return;
            }
        }
    }
}
