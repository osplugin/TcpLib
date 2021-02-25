package com.mjsoftking.tcpserviceapp.test;

import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;
import com.mjsoftking.tcpserviceapp.test.event.TcpReceiveDataEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class DataDispose implements TcpBaseDataDispose {

    private final static String TAG = DataDispose.class.getSimpleName();

    @Override
    public void dispose(int servicePort, String clientAddress, ByteQueueList bufferQueue) {
        byte[] b = new byte[bufferQueue.size()];
        for (int i = 0; i < bufferQueue.size(); ++i) {
            b[i] = bufferQueue.get(i);
        }
        EventBus.getDefault().post(new TcpReceiveDataEvent(servicePort, clientAddress, new String(b)));
        bufferQueue.clear();
    }
}
