package com.mjsoftking.tcplib;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectFailEvent;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectSuccessEvent;
import com.mjsoftking.tcplib.thread.TcpDataReceiveThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途：TCP服务类
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/20
 */
public class TcpLibClient {

    private final static String TAG = TcpLibClient.class.getSimpleName();

    private static TcpLibClient TCP_CLIENT;
    //存储连接上的服务端和对应此服务器连接的发送和接收数据的处理规则
    private final Map<String, TcpDataBuilder> SERVICE_MAP;

    private TcpLibClient() {
        SERVICE_MAP = new ConcurrentHashMap<>();
    }

    public static synchronized TcpLibClient getInstance() {
        if (null == TCP_CLIENT) {
            TCP_CLIENT = new TcpLibClient();
        }
        return TCP_CLIENT;
    }

    /**
     * 发起连接
     *
     * @param ipAddress ip地址
     * @param port      端口号
     * @param builder   使用builder生成对发送数据生成和接收数据解析的实现
     */
    public synchronized void connect(String ipAddress, int port, @NonNull TcpDataBuilder builder) {
        connect(ipAddress + ":" + port, builder);
    }

    /**
     * 发起连接
     *
     * @param address ip:port
     * @param builder 使用builder生成对发送数据生成和接收数据解析的实现
     */
    public synchronized void connect(String address, @NonNull TcpDataBuilder builder) {
        String[] ads = address.split(":");
        String ipAddress = ads[0];
        int port = Integer.parseInt(ads[1]);

        if (null != SERVICE_MAP.get(address)) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "指定服务端已经连接上");
            }
            return;
        }
        new Thread(() -> {
            try {
                Socket socket = new Socket(ipAddress, port);
                //超时不限制
                socket.setSoTimeout(0);
                //线程安全的map
                SERVICE_MAP.put(address, builder.setSocket(socket));
                //发送服务器已连接事件
                EventBus.getDefault().post(new TcpServiceConnectSuccessEvent(port, address));

                //socket关闭时，接收方法就会被关闭
                new TcpDataReceiveThread(port, address, SERVICE_MAP, true).start();
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务器连接失败", e);
                }
                //发送服务器连接失败事件
                EventBus.getDefault().post(new TcpServiceConnectFailEvent(port, address));
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
        return isConnect(ipAddress + ":" + port);
    }


    /**
     * 对指定服务端是否已连接
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     */
    public boolean isConnect(String address) {
        TcpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + address + ", 指定服务端未连接");
            }
            return false;
        }
        return disposeBuilder.getSocket().isConnected();
    }

    /**
     * 关闭对指定服务端的连接
     *
     * @param ipAddress 服务端ip
     * @param port      端口
     */
    public synchronized void close(String ipAddress, int port) {
        close(ipAddress + ":" + port);
    }


    /**
     * 关闭对指定服务端的连接
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     */
    public synchronized void close(String address) {
        TcpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            return;
        }
        if (null != disposeBuilder.getSocket()) {
            try {
                disposeBuilder.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向指定的服务端按照指定数据格式发送数据
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     * @param content 内容
     */
    public void sendMessage(String address, String content) {
        TcpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + address + ", 指定服务端未连接");
            }
            return;
        }

        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + address + ", 向指定服务端发送消息异常", e);
                }
            }
        }).start();
    }


    /**
     * 向所有已连接的服务端按照指定数据格式发送数据
     *
     * @param content 内容
     */
    public void sendAllMessage(String content) {
        for (String address : SERVICE_MAP.keySet()) {
            sendMessage(address, content);
        }
    }

    /**
     * 向指定的服务端按照指定数据格式发送数据
     *
     * @param address 服务端地址，ip:port 形式，如：0.0.0.0:30000
     * @param content 内容
     */
    public void sendMessage(String address, byte[] content) {
        TcpDataBuilder disposeBuilder = SERVICE_MAP.get(address);
        if (null == disposeBuilder) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "服务端端口: " + address + ", 指定服务端未连接");
            }
            return;
        }

        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + address + ", 向指定服务端发送消息异常", e);
                }
            }
        }).start();
    }


    /**
     * 向所有已连接的服务端按照指定数据格式发送数据
     *
     * @param content 内容
     */
    public void sendAllMessage(byte[] content) {
        for (String address : SERVICE_MAP.keySet()) {
            sendMessage(address, content);
        }
    }
}
