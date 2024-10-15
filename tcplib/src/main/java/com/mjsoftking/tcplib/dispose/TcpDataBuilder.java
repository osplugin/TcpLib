package com.mjsoftking.tcplib.dispose;

import android.util.Log;

import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.event.client.TcpClientSendMessageEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceSendMessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    //接收数据报文的处理接口
    private final Class<? extends TcpBaseDataDispose> dataDisposeClass;
    private final TcpBaseDataDispose dataDispose;

    //发送数据报文的生成接口
    private final Class<? extends TcpBaseDataGenerate> dataGenerateClass;
    private final TcpBaseDataGenerate dataGenerate;

    //单例线程池
    private final ExecutorService sendMessageExecutorService;
    /**
     * 客户端隧道
     * <p>
     * 1. 服务端接收到的客户端连接隧道；
     * 2. 客户端连接到服务器的隧道；
     */
    private Socket socket;

    TcpDataBuilder(Class<? extends TcpBaseDataGenerate> dataGenerateClass, Class<? extends TcpBaseDataDispose> dataDisposeClass) {
        this.dataGenerateClass = dataGenerateClass;
        this.dataDisposeClass = dataDisposeClass;

        TcpBaseDataDispose dataDispose1 = instantiateDisposeClass(dataDisposeClass);
        TcpBaseDataGenerate dataGenerate1 = instantiateGenerateClass(dataGenerateClass);

        if (null == dataDispose1) {
            dataDispose1 = (bufferQueue, servicePort, address) -> {
                Log.w(TAG, "未实现数据解析器，使用默认规则");
                byte[] b = new byte[bufferQueue.size()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = bufferQueue.get(i);
                }
                Log.w(TAG, "地址: " + address + ", 接收到数据: " + Arrays.toString(b));
                bufferQueue.removeCountFrame(b.length);
            };
        }

        if (null == dataGenerate1) {
            dataGenerate1 = content -> {
                Log.w(TAG, "未实现数据生成器，使用默认规则");
                if (content instanceof byte[]) {
                    return (byte[]) content;
                } else if (content instanceof String) {
                    return ((String) content).getBytes(Charset.forName("UTF-8"));
                } else {
                    return content.toString().getBytes(Charset.forName("UTF-8"));
                }
            };
        }

        this.dataGenerate = dataGenerate1;
        this.dataDispose = dataDispose1;

        this.sendMessageExecutorService = Executors.newSingleThreadExecutor();
    }

    public TcpBaseDataGenerate instantiateGenerateClass(Class<? extends TcpBaseDataGenerate> clazz) {
        try {
            Constructor<? extends TcpBaseDataGenerate> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TcpBaseDataDispose instantiateDisposeClass(Class<? extends TcpBaseDataDispose> clazz) {
        try {
            Constructor<? extends TcpBaseDataDispose> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置发送和解析数据报文的处理器
     *
     * @param dataGenerate 数据报文生成器
     * @param dataDispose  数据报文处理器
     */
    public static TcpDataBuilder builder(Class<? extends TcpBaseDataGenerate> dataGenerate, Class<? extends TcpBaseDataDispose> dataDispose) {
        return new TcpDataBuilder(dataGenerate, dataDispose);
    }

    public TcpBaseDataDispose getDataDispose() {
        return dataDispose;
    }

    public TcpBaseDataGenerate getDataGenerate() {
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
     * 复制处理规则，返回新对象
     */
    public TcpDataBuilder copy() {
        return new TcpDataBuilder(dataGenerateClass, dataDisposeClass);
    }

    public void serviceSendMessage(int port, String address, Object content) {
        sendMessageExecutorService.submit(() -> {
            try {
                sendMessage(content);
                //发送消息发送成功事件
                EventBus.getDefault().post(new TcpServiceSendMessageEvent(port, address, content));
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 向指定客户端发送消息异常", e);
                }
            }
        });
    }

    public void clientSendMessage(String address, Object content) {
        sendMessageExecutorService.submit(() -> {
            try {
                sendMessage(content);
                //发送消息发送成功事件
                EventBus.getDefault().post(new TcpClientSendMessageEvent(address, content));
            } catch (IOException e) {
                if (TcpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + address + ", 向指定服务端发送消息异常", e);
                }
            }
        });
    }

    private void sendMessage(Object content) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(dataGenerate.generate(content));
        outputStream.flush();
    }


}
