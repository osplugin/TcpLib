package com.mjsoftking.tcpclient.test.generate;

import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;

import java.nio.charset.Charset;

/**
 * 用途：
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class ClientDataGenerate implements TcpBaseDataGenerate {


    @Override
    public byte[] generate(String content) {
        return content.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public byte[] generate(byte[] contentBytes) {
        return contentBytes;
    }
}
