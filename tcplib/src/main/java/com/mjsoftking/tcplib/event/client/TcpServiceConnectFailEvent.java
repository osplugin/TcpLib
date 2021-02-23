package com.mjsoftking.tcplib.event.client;


import com.mjsoftking.tcplib.event.TcpBaseEvent;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class TcpServiceConnectFailEvent extends TcpBaseEvent {

    public TcpServiceConnectFailEvent(String address) {
        super(address);
    }
}
