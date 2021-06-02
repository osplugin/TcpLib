package com.mjsoftking.tcplib;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.service.TcpServiceBindFailEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindSuccessEvent;
import com.mjsoftking.tcplib.thread.TcpServiceAcceptThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    /**
     * 对绑定端口的服务端进行缓存
     */
    private final Map<Integer, ServerSocket> serverSocketMap;
    /**
     * 对绑定端口的服务端的连接客户端进行缓存
     */
    private final Map<Integer, Map<String, TcpDataBuilder>> portMap;

    TcpLibService() {
        portMap = new ConcurrentHashMap<>();
        serverSocketMap = new ConcurrentHashMap<>();
    }

    public static synchronized TcpLibService getInstance() {
        if (null == TCP_SERVICE) {
            TCP_SERVICE = new TcpLibService();
        }
        return TCP_SERVICE;
    }

    private static boolean debugMode;

    public static void setDebugMode(boolean debugMode) {
        TcpLibService.debugMode = debugMode;
    }

    public synchronized void bindService(int port, @NonNull TcpDataBuilder builder) {
        bindService(port, 255, builder);
    }

    public synchronized void bindService(int port, int backlog, @NonNull TcpDataBuilder builder) {
        if (null != serverSocketMap.get(port)) {
            if(debugMode) {
                Log.w(TAG, "TCP服务在端口: " + port + "下已经启动，请勿多次启动");
            }
            return;
        }
        try {
            ServerSocket serverSocket = new ServerSocket(port, backlog);
            //超时不限制
            serverSocket.setSoTimeout(0);
            //线程安全的map
            portMap.put(port, new ConcurrentHashMap<>());
            serverSocketMap.put(port, serverSocket);

            //发送服务器已监听事件
            EventBus.getDefault().post(new TcpServiceBindSuccessEvent(port, "0.0.0.0:" + port));

            //服务关闭时，接收方法就会被关闭
            new TcpServiceAcceptThread(serverSocket, portMap.get(port), builder).start();
        } catch (IOException e) {
            if(debugMode) {
                Log.e(TAG, "服务开启失败", e);
            }
            //发送服务器监听失败事件
            EventBus.getDefault().post(new TcpServiceBindFailEvent(port, "0.0.0.0:" + port));
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
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null != map) {
            for (String address : map.keySet()) {
                try {
                    TcpDataBuilder builder = map.get(address);
                    if (builder != null && null != builder.getSocket()) {
                        builder.getSocket().close();
                    }
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
     * 获取指定端口服务器是否在运行
     *
     * @param port 指定端口服务器
     */
    public boolean isRun(int port) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null == serverSocket) {
            return false;
        }
        return !serverSocket.isClosed();
    }

    /**
     * 获取指定端口服务器的在线客户端数量
     *
     * @param port 指定端口服务器
     * @return 在线客户端数；-1:服务器未启动，反之为在线数量
     */
    public int getOnlineClientCount(int port) {
        if (null == serverSocketMap.get(port)) {
            return -1;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            return 0;
        }
        return map.size();
    }

    /**
     * 获取指定端口服务器的在线客户端
     *
     * @param port 指定端口服务器
     * @return 在线客户端；null:服务器未启动，反之为在线客户端的ip:port形式列表，此内容可以直接由服务器向其发送数据
     */
    public List<String> getOnlineClient(int port) {
        if (null == serverSocketMap.get(port)) {
            return null;
        }
        List<String> addressList = new ArrayList<>();
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            return addressList;
        }
        addressList.addAll(map.keySet());
        Collections.sort(addressList, String::compareTo);
        return addressList;
    }

    /**
     * 向指定的客户端按照指定数据格式发送数据
     *
     * @param port         指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param address      在线客户端地址带端口号
     * @param contentBytes 内容
     */
    public void sendMessage(int port, String address, byte[] contentBytes) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null == serverSocket) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        TcpDataBuilder disposeBuilder = map.get(address);
        if (null == disposeBuilder) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", " +
                        "客户端: " + address + ", 指定客户端未连接服务器");
            }
            return;
        }
        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(contentBytes));
                outputStream.flush();
            } catch (IOException e) {
                if(debugMode) {
                    Log.e(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 向指定客户端发送消息异常", e);
                }
            }
        }).start();
    }

    /**
     * 通过指定服务器向与此服务器连接的所有客户端按照指定数据格式发送数据
     *
     * @param port         指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param contentBytes 内容
     */
    public void sendAllClientMessage(int port, byte[] contentBytes) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null == serverSocket) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        for (String address : map.keySet()) {
            TcpDataBuilder disposeBuilder = map.get(address);
            if (null == disposeBuilder) {
                if(debugMode) {
                    Log.w(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 指定客户端未连接服务器");
                }
                return;
            }
            new Thread(() -> {
                try {
                    OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                    outputStream.write(disposeBuilder.getDataGenerate().generate(contentBytes));
                    outputStream.flush();
                } catch (IOException e) {
                    if(debugMode) {
                        Log.e(TAG, "服务端端口: " + port + ", " +
                                "客户端: " + address + ", 向指定客户端发送消息异常", e);
                    }
                }
            }).start();
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
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        TcpDataBuilder disposeBuilder = map.get(address);
        if (null == disposeBuilder) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", " +
                        "客户端: " + address + ", 指定客户端未连接服务器");
            }
            return;
        }
        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                if(debugMode) {
                    Log.e(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 向指定客户端发送消息异常", e);
                }
            }
        }).start();
    }

    /**
     * 通过指定服务器向与此服务器连接的所有客户端按照指定数据格式发送数据
     *
     * @param port    指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param content 内容
     */
    public void sendAllClientMessage(int port, String content) {
        ServerSocket serverSocket = serverSocketMap.get(port);
        if (null == serverSocket) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if(debugMode) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        for (String address : map.keySet()) {
            TcpDataBuilder disposeBuilder = map.get(address);
            if (null == disposeBuilder) {
                if(debugMode) {
                    Log.w(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 指定客户端未连接服务器");
                }
                return;
            }
            new Thread(() -> {
                try {
                    OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                    outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                    outputStream.flush();
                } catch (IOException e) {
                    if(debugMode) {
                        Log.e(TAG, "服务端端口: " + port + ", " +
                                "客户端: " + address + ", 向指定客户端发送消息异常", e);
                    }
                }
            }).start();
        }
    }


}
