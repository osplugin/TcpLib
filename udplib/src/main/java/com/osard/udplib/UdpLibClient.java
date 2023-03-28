package com.osard.udplib;

import android.text.TextUtils;
import android.util.Log;

import com.osard.udplib.dispose.UdpDataBuilder;
import com.osard.udplib.event.client.UdpServiceConnectFailEvent;
import com.osard.udplib.event.client.UdpServiceConnectSuccessEvent;
import com.osard.udplib.thread.UdpDataReceiveThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途：TCP服务类
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/20
 */
public class UdpLibClient {

    private final static String TAG = UdpLibClient.class.getSimpleName();
    private final static String IP_ADDRESS = "%s:%d";

    private static UdpLibClient UDP_CLIENT;
    //存储连接上的服务端和对应此服务器连接的发送和接收数据的处理规则
    private final Map<String, UdpDataBuilder> SERVICE_MAP;

    private UdpLibClient() {
        SERVICE_MAP = new ConcurrentHashMap<>();
    }

    public static synchronized UdpLibClient getInstance() {
        if (null == UDP_CLIENT) {
            UDP_CLIENT = new UdpLibClient();
        }
        return UDP_CLIENT;
    }

    /**
     * 发起连接
     *
     * @param ipAddress ip地址
     * @param port      端口号
     * @param builder   使用builder生成对发送数据生成和接收数据解析的实现
     */
    public synchronized void connect(String ipAddress, int port, UdpDataBuilder builder) {
        connect(String.format(Locale.getDefault(), IP_ADDRESS, ipAddress, port), builder);
    }

    /**
     * 发起连接
     *
     * @param address ip:port
     * @param builder 使用builder生成对发送数据生成和接收数据解析的实现
     */
    public synchronized void connect(String address, UdpDataBuilder builder) {
        String[] ads = address.split(":");
        String ipAddress = ads[0];
        int port = Integer.parseInt(ads[1]);

        new Thread(() -> {
            try {
                synchronized (address.intern()) {
                    if (null != SERVICE_MAP.get(address)) {
                        if (UdpLibConfig.getInstance().isDebugMode()) {
                            Log.w(TAG, "指定服务端已经连接上");
                        }
                        return;
                    }

                    DatagramSocket socket = new DatagramSocket();
                    //超时不限制
                    socket.setSoTimeout(0);
                    socket.setReuseAddress(true);
                    socket.setBroadcast(true);
                    socket.setReceiveBufferSize(UdpLibConfig.getInstance().getReceiveBufferSize());
                    socket.setSendBufferSize(UdpLibConfig.getInstance().getReceiveBufferSize());
                    socket.connect(new InetSocketAddress(ipAddress, port));
                    //线程安全的map
                    SERVICE_MAP.put(address, builder.setSocket(socket));
                    //发送服务器已连接事件
                    EventBus.getDefault().post(new UdpServiceConnectSuccessEvent(port, address));

                    //socket关闭时，接收方法就会被关闭
                    new UdpDataReceiveThread(ipAddress, port, socket, builder, SERVICE_MAP, true).start();
                }
            } catch (IOException e) {
                if (UdpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务器连接失败", e);
                }
                //发送服务器连接失败事件
                EventBus.getDefault().post(new UdpServiceConnectFailEvent(port, address));
            }
        }).start();
    }

    /**
     * 对指定服务端是否已连接
     *
     * @param ipAddress 服务端ip
     * @param port      端口
     */
    public boolean isConnect(String ipAddress, int port) {
        return isConnect(String.format(Locale.getDefault(), IP_ADDRESS, ipAddress, port));
    }


    /**
     * 对指定服务端是否已连接
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     */
    public boolean isConnect(String address) {
        UdpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            if (UdpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + address + ", 指定服务端未连接");
            }
            return false;
        }
        return disposeBuilder.getSocket().isConnected();
    }

    /**
     * 对指定服务端是否已连接
     * <p>
     * 如果连接了多个服务器时，则操作服务器正序排列最小的那台
     */
    public boolean isConnect() {
        String serviceAddress = onlyConnectServicePort();
        if (TextUtils.isEmpty(serviceAddress)) {
            if (UdpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有连接中的服务器");
            }
            return false;
        }

        return isConnect(serviceAddress);
    }

    /**
     * 关闭对指定服务端的连接
     *
     * @param ipAddress 服务端ip
     * @param port      端口
     */
    public synchronized void close(String ipAddress, int port) {
        close(String.format(Locale.getDefault(), IP_ADDRESS, ipAddress, port));
    }


    /**
     * 关闭对指定服务端的连接
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     */
    public synchronized void close(String address) {
        UdpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            return;
        }
        if (null != disposeBuilder.getSocket()) {
            disposeBuilder.getSocket().close();
        }
        SERVICE_MAP.remove(address);
    }

    /**
     * 关闭对指定服务端的连接
     * <p>
     * 如果连接了多个服务器时，则操作服务器正序排列最小的那台
     */
    public synchronized void close() {
        String serviceAddress = onlyConnectServicePort();
        if (TextUtils.isEmpty(serviceAddress)) {
            if (UdpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有连接中的服务器");
            }
            return;
        }

        close(serviceAddress);
    }

    /**
     * 向指定的服务端按照指定数据格式发送数据
     *
     * @param ipAddress 服务端ip
     * @param port      端口
     * @param content   需要发送的原始数据
     */
    public void sendMessage(String ipAddress, int port, Object content) {
        sendMessage(String.format(Locale.getDefault(), IP_ADDRESS, ipAddress, port), content);
    }

    /**
     * 向指定的服务端按照指定数据格式发送数据
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     * @param content 需要发送的原始数据
     */
    public void sendMessage(String address, Object content) {
        UdpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            if (UdpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + address + ", 指定服务端未连接");
            }
            return;
        }

        disposeBuilder.clientSendMessage(address, content);
    }

    /**
     * 向指定的服务端按照指定数据格式发送数据
     * <p>
     * 如果连接了多个服务器时，则操作服务器正序排列最小的那台
     *
     * @param content 需要发送的原始数据
     */
    public void sendMessage(Object content) {
        String serviceAddress = onlyConnectServicePort();
        if (TextUtils.isEmpty(serviceAddress)) {
            if (UdpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "当前没有连接中的服务器");
            }
            return;
        }

        sendMessage(serviceAddress, content);
    }

    /**
     * 向所有已连接的服务端按照指定数据格式发送数据
     *
     * @param content 需要发送的原始数据
     */
    public void sendAllMessage(Object content) {
        for (String address : SERVICE_MAP.keySet()) {
            sendMessage(address, content);
        }
    }

    /**
     * 如果连接的服务器数量大于0，则返回服务器全地址正序排列后的第一个，反之返回null
     */
    private String onlyConnectServicePort() {
        List<String> s = new ArrayList<>(SERVICE_MAP.keySet());
        Collections.sort(s);
        return s.size() > 0 ? s.get(0) : null;
    }

}
