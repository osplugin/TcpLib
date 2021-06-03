package com.mjsoftking.tcpclient.test.dispose;

import com.mjsoftking.tcpclient.test.event.TcpClientReceiveDataEvent;
import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;

import org.greenrobot.eventbus.EventBus;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class ClientDataDispose implements TcpBaseDataDispose {

    private final static String TAG = ClientDataDispose.class.getSimpleName();

    @Override
    public void dispose(ByteQueueList bufferQueue, int servicePort, String clientAddress) {
        byte[] b = new byte[bufferQueue.size()];
        for (int i = 0; i < bufferQueue.size(); ++i) {
            b[i] = bufferQueue.get(i);
        }
        EventBus.getDefault().post(new TcpClientReceiveDataEvent(servicePort, clientAddress, new String(b)));
        bufferQueue.clear();
    }
}
