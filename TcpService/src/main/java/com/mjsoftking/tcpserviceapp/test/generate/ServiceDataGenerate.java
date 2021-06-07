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
        return content.toString().getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public byte[] generate(byte[] contentBytes) {
        return contentBytes;
    }
}
