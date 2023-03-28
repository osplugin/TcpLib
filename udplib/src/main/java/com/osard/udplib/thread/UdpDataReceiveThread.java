package com.osard.udplib.thread;


import com.osard.udplib.UdpLibConfig;
import com.osard.udplib.bean.ReceiveBean;
import com.osard.udplib.dispose.UdpDataBuilder;
import com.osard.udplib.event.service.UdpClientDisconnectEvent;
import com.osard.udplib.event.service.UdpServiceCloseEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途：服务器启动后开启的等待客户端连接线程
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpDataReceiveThread extends Thread {

    private final static String TAG = UdpDataReceiveThread.class.getSimpleName();
    public final static String IP_ADDRESS = "%s:%d";

    private final DatagramSocket serverSocket;
    private final UdpDataBuilder builder;
    private final int servicePort;
    private final String serviceIp;
    private final Map<String, ReceiveBean> map = new ConcurrentHashMap<>();
    private final boolean client;
    private final Map<String, UdpDataBuilder> manageMap;

    public UdpDataReceiveThread(String serviceIp, int servicePort, DatagramSocket serverSocket, UdpDataBuilder builder, Map<String, UdpDataBuilder> manageMap, boolean client) {
        this.serviceIp = serviceIp;
        this.servicePort = servicePort;
        this.serverSocket = serverSocket;
        this.builder = builder;
        this.client = client;
        this.manageMap = manageMap;

//        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        lock = manager.createMulticastLock(UUID.randomUUID().toString());

        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[UdpLibConfig.getInstance().getReceiveCacheBufferSize()];
        DatagramPacket p = new DatagramPacket(buffer, buffer.length);

        while (true) {
            try {
//                lock.acquire();
                serverSocket.receive(p);
//                lock.release();

                ReceiveBean receiveBean = map.get(p.getAddress().getHostAddress());
                if (null == receiveBean) {
                    receiveBean = new ReceiveBean(builder);
                    map.put(p.getAddress().getHostAddress(), receiveBean);
                }

                String address = String.format(Locale.getDefault(), IP_ADDRESS, p.getAddress().getHostAddress(), p.getPort());
                receiveBean.addBufferAndRun(p, servicePort, address);
            } catch (IOException e) {
                //主动调用一次，确保关闭
                serverSocket.close();

                if (client) {
                    //发送服务器监听关闭事件
                    EventBus.getDefault().post(new UdpClientDisconnectEvent(this.servicePort,
                            String.format(Locale.getDefault(), IP_ADDRESS, "0.0.0.0", servicePort)));
                } else {
                    //发送服务器监听关闭事件
                    EventBus.getDefault().post(new UdpServiceCloseEvent(this.servicePort,
                            String.format(Locale.getDefault(), IP_ADDRESS, "0.0.0.0", servicePort)));
                }

                manageMap.remove(String.format(Locale.getDefault(), IP_ADDRESS, serviceIp, servicePort));
                break;
            }
        }
        //跳出循环则服务器已停止
        for (String key : map.keySet()) {
            ReceiveBean receiveBean = map.remove(key);
            if (null != receiveBean) {
                receiveBean.destroy();
            }
        }

    }
}
