package com.mjsoftking.tcplib;

import android.util.Log;

import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectEvent;
import com.mjsoftking.tcplib.tcpthread.TcpClientDataReceiveThread;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
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
    private Map<String, Socket> serviceMap;
    //接收数据报文的处理接口
    private TcpBaseDataDispose dataDispose;
    //发送数据报文的生成接口
    private TcpBaseDataGenerate dataGenerate;

    public TcpBaseDataDispose getDataDispose() {
        if (null == dataDispose) {
            dataDispose = (clientAddress, bufferQueue) -> {
                Log.e(TAG, "未实现数据解析器，使用默认规则");
                byte[] b = new byte[bufferQueue.size()];
                for (int i = 0; i < bufferQueue.size(); ++i) {
                    b[i] = bufferQueue.get(i);
                }
                Log.e(TAG, "接收到数据: " + Arrays.toString(b));
                bufferQueue.clear();
            };
        }
        return dataDispose;
    }

    public TcpBaseDataGenerate getDataGenerate() {
        if (null == dataGenerate) {
            dataGenerate = content -> content.getBytes(Charset.forName("UTF-8"));
        }
        return dataGenerate;
    }

    /**
     * 设置发送数据的报文格式
     */
    public TcpLibClient setDataGenerate(TcpBaseDataGenerate dataGenerate) {
        this.dataGenerate = dataGenerate;
        return this;
    }

    /**
     * 设置接收数据报文的解析规则
     */
    public TcpLibClient setDataDispose(TcpBaseDataDispose dataDispose) {
        this.dataDispose = dataDispose;
        return this;
    }

    public synchronized void connect(String ipAddress, int port) {
        final String address = ipAddress + ":" + port;
        Socket service = serviceMap.get(address);
        if (null != service) {
            Log.w(TAG, "指定服务已经连接了");
            return;
        }
        new Thread(() -> {
            try {
                Socket socket = new Socket(ipAddress, port);
                //超时不限制
                socket.setSoTimeout(0);
                //线程安全的map
                serviceMap.put(address, socket);
                //发送服务器已连接事件
                EventBus.getDefault().post(new TcpServiceConnectEvent(address));

                //socket关闭时，接收方法就会被关闭
                new TcpClientDataReceiveThread(socket, address, serviceMap, getDataDispose()).start();
            } catch (IOException e) {
                //todo 服务器连接失败
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 关闭服务监听
     */
    public synchronized void close(String address) {
        Socket client = serviceMap.get(address);
        if (null == client) {
            return;
        }
        if (null != client) {
            try {
                client.close();
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
        Socket service = serviceMap.get(address);
        if (null == service) {
            Log.w(TAG, "指定服务端未连接");
            return;
        }

        new Thread(() -> {
            try {
                OutputStream outputStream = service.getOutputStream();
                outputStream.write(getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "向指定服务端发送消息异常", e);
            }
        }).start();
    }


}
