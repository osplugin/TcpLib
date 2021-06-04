package com.mjsoftking.tcpserviceapp.test.dispose;

import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;
import com.mjsoftking.tcpserviceapp.test.event.TcpServiceReceiveDataEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class ServiceDataDispose implements TcpBaseDataDispose {

    private final static String TAG = ServiceDataDispose.class.getSimpleName();

    @Override
    public void dispose(ByteQueueList bufferQueue, int servicePort, String clientAddress) {
        byte[] b = bufferQueue.copyAndRemove(bufferQueue.size());
        EventBus.getDefault().post(new TcpServiceReceiveDataEvent(servicePort, clientAddress, new String(b)));

    }
}
