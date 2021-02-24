package com.mjsoftking.tcplib;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mjsoftking.tcplib.dispose.TcpDataDisposeBuilder;
import com.mjsoftking.tcplib.tcpthread.TcpServiceAcceptThread;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途：TCP服务类
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/20
 */
public class TcpLibService {

    private final static String TAG = TcpLibService.class.getSimpleName();

    private static TcpLibService TCP_SERVICE;

    public static synchronized TcpLibService getInstance() {
        if (null == TCP_SERVICE) {
            TCP_SERVICE = new TcpLibService();
        }
        return TCP_SERVICE;
    }

    private TcpLibService() {
        portMap = new ConcurrentHashMap<>();
        serverSocketMap = new ConcurrentHashMap<>();
    }

    //服务
//    private ServerSocket serverSocket;
    //存储连接上的客户端
    //存储连接上的客户端
    private final Map<Integer, ServerSocket> serverSocketMap;
    private final Map<Integer, Map<String, TcpDataDisposeBuilder>> portMap;

    public synchronized void bindService(int port, @NonNull TcpDataDisposeBuilder builder) {
        bindService(port, 250, builder);
    }

    public synchronized void bindService(int port, int backlog, @NonNull TcpDataDisposeBuilder builder) {
        if (null != serverSocketMap.get(port)) {
            Log.w(TAG, "TCP服务在端口: " + port + "下已经启动，请勿多次启动");
            return;
        }
        try {
            ServerSocket serverSocket = new ServerSocket(port, backlog);
            //超时不限制
            serverSocket.setSoTimeout(0);
            //线程安全的map
            portMap.put(port, new ConcurrentHashMap<>());
            serverSocketMap.put(port, serverSocket);

            //服务关闭时，接收方法就会被关闭
            new TcpServiceAcceptThread(serverSocket, portMap.get(port), builder).start();
        } catch (IOException e) {
            Log.e(TAG, "服务开启失败", e);
        }
    }

    /**
     * 关闭服务监听
     */
    public synchronized void close(int port) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String, TcpDataDisposeBuilder> map = portMap.get(port);
        if (null != map) {
            for (String address : map.keySet()) {
                try {
                    TcpDataDisposeBuilder builder = map.get(address);
                    builder.getSocket().close();
                    map.remove(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        serverSocketMap.remove(port);
        portMap.remove(port);
    }

    /**
     * 关闭服务监听
     */
    public synchronized void closeAll() {
        for (Integer port : serverSocketMap.keySet()) {
            close(port);
        }
    }

    /**
     * 向指定的客户端按照指定数据格式发送数据
     *
     * @param port    指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param address 在线客户端地址带端口号
     * @param content 内容
     */
    public void sendMessage(int port, String address, String content) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null == serverSocket) {
            Log.w(TAG, "服务端未启动");
            return;
        }
        Map<String, TcpDataDisposeBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            Log.w(TAG, "服务端没有客户端连接");
            return;
        }
        TcpDataDisposeBuilder disposeBuilder = map.get(address);
        if (null == disposeBuilder) {
            Log.w(TAG, "指定客户端未连接服务器");
            return;
        }
        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "向指定客户端发送消息异常", e);
            }
        }).start();
    }


}
