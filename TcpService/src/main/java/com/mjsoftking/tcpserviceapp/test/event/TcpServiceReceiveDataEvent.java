package com.mjsoftking.tcpserviceapp.test.event;

import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceReceiveDataEvent extends TcpBaseEvent {

    private final String message;
    private final int count;

    public TcpServiceReceiveDataEvent(int servicePort, String ipAddress, String message, int count) {
        super(servicePort, ipAddress);
        this.message = message;
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }
}
