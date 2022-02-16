package com.mjsoftking.tcpclient.test.generate;

import com.mjsoftking.tcpclient.test.Datagram;
import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class ClientDataGenerate implements TcpBaseDataGenerate {


    @Override
    public byte[] generate(Object content) {
        if (content instanceof Datagram) {
            Datagram datagram = (Datagram) content;
            return datagram.fullData();
        }
        return new byte[]{ 0 };
    }

}
