package com.osard.udplib.event.service;


import com.osard.udplib.event.UdpBaseEvent;

/**
 * 用途：服务端绑定启动触发事件
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class UdpServiceBindSuccessEvent extends UdpBaseEvent {

    /**
     * @param servicePort 服务端口
     * @param address     服务地址，0。0.0.0:(servicePort)
     */
    public UdpServiceBindSuccessEvent(int servicePort, String address) {
        super(servicePort, address);
    }
}
