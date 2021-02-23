package com.mjsoftking.tcplib.dispose;

/**
 * 用途：发送数据是的报文生成器
 * 按照报文格式生成报文数据
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public interface TcpBaseDataGenerate {

    byte[] generate(String content);

}
