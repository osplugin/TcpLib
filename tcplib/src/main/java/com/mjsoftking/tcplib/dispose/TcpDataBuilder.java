package com.mjsoftking.tcplib.dispose;

import android.util.Log;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 用途：数据处理器
 * <p>
 * 创建连接或者服务器时均需要使用builder构建此类传入报文生成器和报文处理器
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/24
 */
public class TcpDataBuilder {

    private final static String TAG = TcpDataBuilder.class.getSimpleName();

    TcpDataBuilder(TcpBaseDataGenerate dataGenerate, TcpBaseDataDispose dataDispose) {
        this.dataGenerate = dataGenerate;
        this.dataDispose = dataDispose;
    }

    //接收数据报文的处理接口
    private TcpBaseDataDispose dataDispose;
    //发送数据报文的生成接口
    private TcpBaseDataGenerate dataGenerate;

    /**
     * 客户端隧道
     * <p>
     * 1. 服务端接收到的客户端连接隧道；
     * 2. 客户端连接到服务器的隧道；
     */
    private Socket socket;

    public TcpBaseDataDispose getDataDispose() {
        if (null == dataDispose) {
            dataDispose = (servicePort, address, bufferQueue) -> {
                Log.w(TAG, "未实现数据解析器，使用默认规则");
                byte[] b = new byte[bufferQueue.size()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = bufferQueue.get(i);
                }
                Log.w(TAG, "地址: " + address + ", 接收到数据: " + Arrays.toString(b));
                bufferQueue.removeCountFrame(b.length);
            };
        }
        return dataDispose;
    }

    public TcpBaseDataGenerate getDataGenerate() {
        if (null == dataGenerate) {
            Log.w(TAG, "未实现数据生成器，使用默认规则");
            dataGenerate = content -> content.getBytes(Charset.forName("UTF-8"));
        }
        return dataGenerate;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * 客户端建立连接后/服务端接收到客户端连接后自动设置值，连接服务器/开启服务器时无需设置
     *
     * @param socket 客户端与服务器的连接/服务端接收到客户端的连接
     */
    public TcpDataBuilder setSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    /**
     * 设置发送和解析数据报文的处理器
     *
     * @param dataGenerate 数据报文生成器
     * @param dataDispose  数据报文处理器
     */
    public static TcpDataBuilder builder(TcpBaseDataGenerate dataGenerate, TcpBaseDataDispose dataDispose) {
        return new TcpDataBuilder(dataGenerate, dataDispose);
    }


}
