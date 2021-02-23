package com.mjsoftking.tcplib;

import android.util.Log;

import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;
import com.mjsoftking.tcplib.tcpthread.TcpServiceAcceptThread;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
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
    }

    //服务
    private ServerSocket serverSocket;
    //存储连接上的客户端
    private Map<String, Socket> clientMap;
    //接收数据报文的处理接口
    private TcpBaseDataDispose dataDispose;
    //发送数据报文的生成接口
    private TcpBaseDataGenerate dataGenerate;

    //标记服务是否开启
    private boolean isOpen;

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
    public TcpLibService setDataGenerate(TcpBaseDataGenerate dataGenerate) {
        this.dataGenerate = dataGenerate;
        return this;
    }

    /**
     * 设置接收数据报文的解析规则
     */
    public TcpLibService setDataDispose(TcpBaseDataDispose dataDispose) {
        this.dataDispose = dataDispose;
        return this;
    }

    public synchronized void bindService(int port) {
        bindService(port, 250);
    }

    public synchronized void bindService(int port, int backlog) {
        if (isOpen) {
            Log.w(TAG, "TCP服务正在运行，请勿多次开启");
            return;
        }
        try {
            serverSocket = new ServerSocket(port, backlog);
            //超时不限制
            serverSocket.setSoTimeout(0);
            //线程安全的map
            clientMap = new ConcurrentHashMap<>();

            //服务关闭时，接收方法就会被关闭
            new TcpServiceAcceptThread(serverSocket, clientMap, getDataDispose()).start();

            isOpen = true;
        } catch (IOException e) {
            Log.e(TAG, "服务开启失败", e);
        }
    }

    /**
     * 关闭服务监听
     */
    public synchronized void close() {
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != clientMap) {
            clientMap.clear();
            clientMap = null;
        }

        isOpen = false;
    }

    /**
     * 向指定的客户端按照指定数据格式发送数据
     *
     * @param address 在线客户端地址带端口号
     * @param content   内容
     */
    public void sendMessage(String address, String content) {
        if (!isOpen) {
            Log.w(TAG, "服务未启动");
            return;
        }
        Socket client = clientMap.get(address);
        if (null == client) {
            Log.w(TAG, "指定客户端未连接");
            return;
        }

        new Thread(() -> {
            try {
                OutputStream outputStream = client.getOutputStream();
                outputStream.write(getDataGenerate().generate(content));
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "向指定客户端发送消息异常", e);
            }
        }).start();
    }


}
