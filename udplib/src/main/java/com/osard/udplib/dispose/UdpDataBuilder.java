package com.osard.udplib.dispose;

import android.util.Log;

import com.osard.udplib.UdpLibConfig;
import com.osard.udplib.event.client.UdpClientSendMessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
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
public class UdpDataBuilder {

    private final static String TAG = UdpDataBuilder.class.getSimpleName();
    //接收数据报文的处理接口
    private final UdpBaseDataDispose dataDispose;
    //发送数据报文的生成接口
    private final UdpBaseDataGenerate dataGenerate;
    //单例线程池
    private final ExecutorService sendMessageExecutorService;
    /**
     * 客户端隧道
     * <p>
     * 1. 服务端接收到的客户端连接隧道；
     * 2. 客户端连接到服务器的隧道；
     */
    private DatagramSocket socket;

    UdpDataBuilder(UdpBaseDataGenerate dataGenerate, UdpBaseDataDispose dataDispose) {
        if (null == dataDispose) {
            dataDispose = (bufferQueue, servicePort, address) -> {
                Log.w(TAG, "未实现数据解析器，使用默认规则");
                byte[] b = new byte[bufferQueue.size()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = bufferQueue.get(i);
                }
                Log.w(TAG, "地址: " + address + ", 接收到数据: " + Arrays.toString(b));
                bufferQueue.removeCountFrame(b.length);

                try {
                    sendMessage(address, b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }

        if (null == dataGenerate) {
            dataGenerate = new UdpBaseDataGenerate() {
                @Override
                public byte[] generate(Object content) {
                    Log.w(TAG, "未实现数据生成器，使用默认规则");
                    if (content instanceof byte[]) {
                        return (byte[]) content;
                    } else if (content instanceof String) {
                        return ((String) content).getBytes(Charset.forName("UTF-8"));
                    } else {
                        return content.toString().getBytes(Charset.forName("UTF-8"));
                    }
                }
            };
        }

        this.dataGenerate = dataGenerate;
        this.dataDispose = dataDispose;

        this.sendMessageExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 设置发送和解析数据报文的处理器
     *
     * @param dataGenerate 数据报文生成器
     * @param dataDispose  数据报文处理器
     */
    public static UdpDataBuilder builder(UdpBaseDataGenerate dataGenerate, UdpBaseDataDispose dataDispose) {
        return new UdpDataBuilder(dataGenerate, dataDispose);
    }

    public UdpBaseDataDispose getDataDispose() {
        return dataDispose;
    }

    public UdpBaseDataGenerate getDataGenerate() {
        return dataGenerate;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * 客户端建立连接后/服务端接收到客户端连接后自动设置值，连接服务器/开启服务器时无需设置
     *
     * @param socket 客户端与服务器的连接/服务端接收到客户端的连接
     */
    public UdpDataBuilder setSocket(DatagramSocket socket) {
        this.socket = socket;
        return this;
    }

    /**
     * 复制处理规则，返回新对象
     */
    public UdpDataBuilder copy() {
        return new UdpDataBuilder(getDataGenerate(), getDataDispose());
    }

    public void serviceSendMessage(String serviceAddress, String address, Object content) {
        sendMessageExecutorService.submit(() -> {
            String[] s = serviceAddress.split(":");
            if (s.length < 2) {
                return;
            }
            int port = Integer.parseInt(s[1]);

            try {
                sendMessage(address, content);
                //发送消息发送成功事件
//                EventBus.getDefault().post(new UdpServiceSendMessageEvent(port, address, content));
            } catch (IOException e) {
                if (UdpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + port + ", " +
                            "客户端: " + address + ", 向指定客户端发送消息异常", e);
                }
            }
        });
    }

    public void clientSendMessage(String address, Object content) {
        sendMessageExecutorService.submit(() -> {
            try {
                sendMessage(address, content);
                //发送消息发送成功事件
                EventBus.getDefault().post(new UdpClientSendMessageEvent(address, content));
            } catch (IOException e) {
                if (UdpLibConfig.getInstance().isDebugMode()) {
                    Log.e(TAG, "服务端端口: " + address + ", 向指定服务端发送消息异常", e);
                }
            }
        });
    }

    private void sendMessage(String address, Object content) throws IOException {
        String[] s = address.split(":");
        if (s.length < 2) {
            return;
        }
        byte[] buffer = dataGenerate.generate(content);
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length,
                Inet4Address.getByName(s[0]), Integer.parseInt(s[1]));
        socket.send(datagramPacket);
    }


}
