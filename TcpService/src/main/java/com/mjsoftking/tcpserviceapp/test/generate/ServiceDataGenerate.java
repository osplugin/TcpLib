package com.mjsoftking.tcpserviceapp.test.generate;

import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;

import java.nio.charset.Charset;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class ServiceDataGenerate implements TcpBaseDataGenerate {


    @Override
    public byte[] generate(Object content) {
        if (content instanceof byte[]) {
            return (byte[]) content;
        } else if (content instanceof String) {
            return ((String) content).getBytes(Charset.forName("UTF-8"));
        } else {
            return content.toString().getBytes(Charset.forName("UTF-8"));
        }
    }

}
