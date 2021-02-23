package com.mjsoftking.tcplib.event.service;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpClientDisconnectEvent extends TcpBaseEvent {

    public TcpClientDisconnectEvent(String address) {
        super(address);
    }
}
