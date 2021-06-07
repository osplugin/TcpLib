package com.mjsoftking.tcplib.dispose;

/**
 * 用途：发送数据时的报文生成器
 * <p>
 * 按照报文格式生成报文数据
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public interface TcpBaseDataGenerate {

    /**
     * 根据数据转成byte数组，并添加验证位、数据头、数据尾等
     */
    byte[] generate(Object content);

}
