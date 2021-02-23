package com.mjsoftking.tcplib.tcpthread;


import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.event.service.TcpClientConnectEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceAcceptThread extends Thread {

    private final ServerSocket serverSocket;
    private final Map<String, Socket> clientMap;
    private final TcpBaseDataDispose dataDispose;

    public TcpServiceAcceptThread(ServerSocket serverSocket, Map<String, Socket> clientMap, TcpBaseDataDispose dataDispose) {
        this.serverSocket = serverSocket;
        this.clientMap = clientMap;
        this.dataDispose = dataDispose;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                String address = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                //存入在线客户端缓存
                clientMap.put(address, client);

                //发送客户端上线事件
                EventBus.getDefault().post(new TcpClientConnectEvent(address));

                //对客户端开启数据接收线程
                TcpServiceDataReceiveThread tcpServiceDataReceiveThread = new TcpServiceDataReceiveThread(client, address, clientMap, dataDispose);
                tcpServiceDataReceiveThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
