package com.mjsoftking.tcplib;

import android.util.Log;

import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.service.TcpServiceBindFailEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindSuccessEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceSendMessageEvent;
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

    public void bindService(int port, TcpDataBuilder builder) {
        bindService(port, 255, builder);
    }

    public synchronized void bindService(int port, int backlog, TcpDataBuilder builder) {
        if (null != serverSocketMap.get(port)) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
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
            if (TcpLibConfig.getInstance().isDebugMode()) {
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
     * 关闭端口服务监听
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     */
    public synchronized void close() {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return;
        }

        close(port);
    }

    /**
     * 关闭所有端口服务监听
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
     * 指定端口服务器的关闭指定客户端
     */
    public synchronized void closeClient(int port, String address) {
        if (!isRun(port)) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        TcpDataBuilder disposeBuilder = map.get(address);
        if (null == disposeBuilder) {
            return;
        }
        try {
            disposeBuilder.getSocket().close();
        } catch (IOException e) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.e(TAG, "服务端端口: " + port + ", " +
                        "客户端: " + address + ", 关闭客户端时出现异常", e);
            }
        }
    }

    /**
     * 指定端口服务器的关闭指定客户端
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     */
    public synchronized void closeClient(String address) {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return;
        }

        closeClient(port, address);
    }

    /**
     * 获取指定端口服务器的在线客户端数量
     *
     * @param port 指定端口服务器
     * @return 在线客户端数；-1:服务器未启动，反之为在线数量
     */
    public int getOnlineClientCount(int port) {
        if (!isRun(port)) {
            return -1;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            return 0;
        }
        return map.size();
    }

    /**
     * 获取指定端口服务器的在线客户端数量
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     *
     * @return 在线客户端数；-1:服务器未启动，反之为在线数量
     */
    public int getOnlineClientCount() {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return -1;
        }

        return getOnlineClientCount(port);
    }

    /**
     * 获取指定端口服务器的在线客户端
     *
     * @param port 指定端口服务器
     * @return 在线客户端；null:服务器未启动，反之为在线客户端的ip:port形式列表，此内容可以直接在服务器向其发送数据
     */
    public List<String> getOnlineClient(int port) {
        if (!isRun(port)) {
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
     * 获取指定端口服务器的在线客户端
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     *
     * @return 在线客户端；null:服务器未启动，反之为在线客户端的ip:port形式列表，此内容可以直接在服务器向其发送数据
     */
    public List<String> getOnlineClient() {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return null;
        }

        return getOnlineClient(port);
    }

    /**
     * 向指定的客户端按照指定数据格式发送数据
     *
     * @param port    指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param address 在线客户端地址带端口号
     * @param content 需要发送的原始数据
     */
    public void sendMessage(int port, String address, Object content) {
        if (!isRun(port)) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        TcpDataBuilder disposeBuilder = map.get(address);
        if (null == disposeBuilder) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
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
                //发送消息发送成功事件
                EventBus.getDefault().post(new TcpServiceSendMessageEvent(port, address, content));
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 向指定客户端发送消息异常", e);
                }
            }
        }).start();
    }

    /**
     * 向指定的客户端按照指定数据格式发送数据
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     *
     * @param address 在线客户端地址带端口号
     * @param content 需要发送的原始数据
     */
    public void sendMessage(String address, Object content) {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return;
        }

        sendMessage(port, address, content);
    }

    /**
     * 通过指定服务器向与此服务器连接的所有客户端按照指定数据格式发送数据
     *
     * @param port    指定服务器端口号，使用此端口启动的服务发起数据发送
     * @param content 需要发送的原始数据
     */
    public void sendAllClientMessage(int port, Object content) {
        if (!isRun(port)) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端未启动");
            }
            return;
        }
        Map<String, TcpDataBuilder> map = portMap.get(port);
        if (null == map || map.isEmpty()) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + port + ", 服务端没有客户端连接");
            }
            return;
        }
        for (String address : map.keySet()) {
            sendMessage(port, address, content);
        }
    }

    /**
     * 通过指定服务器向与此服务器连接的所有客户端按照指定数据格式发送数据
     * <p>
     * 如果运行了多个服务器时，则操作端口号最小的服务器
     *
     * @param content 需要发送的原始数据
     */
    public void sendAllClientMessage(Object content) {
        Integer port = onlyRunServicePort();
        if (null == port) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有正在运行的服务器");
            }
            return;
        }

        sendAllClientMessage(port, content);
    }

    /**
     * 如果运行的服务器数量大于0，则返回端口号正序排列后的第一个，反之返回null
     */
    private Integer onlyRunServicePort() {
        List<Integer> s = new ArrayList<>(serverSocketMap.keySet());
        Collections.sort(s);
        return s.size() > 0 ? s.get(0) : null;
    }

}
