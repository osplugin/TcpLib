package com.mjsoftking.tcpserviceapp.test;

import com.mjsoftking.tcplib.dispose.TcpBaseDataGenerate;

import java.nio.charset.Charset;

/**
 * 用途：发送数据是的报文生成器
 * 按照报文格式生成报文数据
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class DataGenerate implements TcpBaseDataGenerate {

    @Override
    public byte[] generate(String content) {
        return content.getBytes(Charset.forName("UTF-8"));
    }
}
