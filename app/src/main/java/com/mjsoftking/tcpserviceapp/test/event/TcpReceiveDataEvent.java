package com.mjsoftking.tcpserviceapp.test.event;

import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpReceiveDataEvent extends TcpBaseEvent {

    private String message;

    public TcpReceiveDataEvent(String ipAddress, String message) {
        super(ipAddress);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
