package com.mjsoftking.tcplib;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mjsoftking.tcplib.dispose.TcpDataDisposeBuilder;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectEvent;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectFailEvent;
import com.mjsoftking.tcplib.tcpthread.TcpDataReceiveThread;

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

    public static synchronized TcpLibClient getInstance() {
        if (null == TCP_CLIENT) {
            TCP_CLIENT = new TcpLibClient();
        }
        return TCP_CLIENT;
    }

    private TcpLibClient() {
        serviceMap = new ConcurrentHashMap<>();
    }

    //存储连接上的服务端
    private Map<String, TcpDataDisposeBuilder> serviceMap;

    public synchronized void connect(String ipAddress, int port, @NonNull TcpDataDisposeBuilder builder) {
        final String address = ipAddress + ":" + port;
        if (null != serviceMap.get(address)) {
            Log.w(TAG, "指定服务端已经连接上");
            return;
        }
        new Thread(() -> {
            try {
                Socket socket = new Socket(ipAddress, port);
                //超时不限制
                socket.setSoTimeout(0);
                //线程安全的map
                serviceMap.put(address, builder.setSocket(socket));
                //发送服务器已连接事件
                EventBus.getDefault().post(new TcpServiceConnectEvent(address));

                //socket关闭时，接收方法就会被关闭
                new TcpDataReceiveThread(address, serviceMap, true).start();
            } catch (IOException e) {
                Log.e(TAG, "服务器连接失败", e);
                //发送服务器连接失败事件
                EventBus.getDefault().post(new TcpServiceConnectFailEvent(address));
            }
        }).start();
    }

    /**
     * 关闭服务监听
     */
    public synchronized void close(String address) {
        TcpDataDisposeBuilder disposeBuilder = serviceMap.get(address);
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
     * 向指定的服务端端按照指定数据格式发送数据
     *
     * @param address 服务端地址带端口号
     * @param content 内容
     */
    public void sendMessage(String address, String content) {
        TcpDataDisposeBuilder disposeBuilder = serviceMap.get(address);
        if (null == disposeBuilder) {
            Log.w(TAG, "指定服务端未连接");
            return;
        }

        new Thread(() -> {
            try {
                OutputStream outputStream = disposeBuilder.getSocket().getOutputStream();
                outputStream.write(disposeBuilder.getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "向指定服务端发送消息异常", e);
            }
        }).start();
    }


}
