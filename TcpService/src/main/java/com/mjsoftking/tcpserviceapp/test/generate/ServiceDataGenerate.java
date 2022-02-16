package com.mjsoftking.tcpserviceapp.test.generate;

import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;
import com.mjsoftking.tcpserviceapp.test.Datagram;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class ServiceDataGenerate implements TcpBaseDataGenerate {


    @Override
    public byte[] generate(Object content) {
        if (content instanceof Datagram) {
            Datagram datagram = (Datagram) content;
            return datagram.fullData();
        }
        return new byte[]{ 0 };
    }

}
