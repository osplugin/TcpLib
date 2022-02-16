package com.mjsoftking.tcpclient.test;

import com.blankj.utilcode.util.ObjectUtils;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 用途：报文结构类
 * <p>
 * 完整报文包含：
 * 4字节报文头长
 * 2字节指令长
 * 4字节数据长度
 * 不定字节数据长度
 * 1字节签名长度
 * 4字节报文尾长
 * <p>
 * 作者：MJSoftKing
 */
public class Datagram implements Serializable {

    /**
     * 报文头，0xDD,0xDD,0xDD,0xDD
     */
    public final static byte[] HEADER = new byte[]{(byte) 0xDD, (byte) 0xDD, (byte) 0xDD, (byte) 0xDD};
    /**
     * 报文尾，0xFF,0xFF,0xFF,0xFF
     */
    public final static byte[] FOOTER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    public final static int HEADER_LENGTH = 4;
    public final static int COMMAND_LENGTH = 2;
    public final static int DATA_LENGTH = 2;
    public final static int SIGN_LENGTH = 1;
    public final static int FOOTER_LENGTH = 4;

    /**
     * 命令，预留2字节
     */
    private byte[] command;
    /**
     * 数据长度，预留4位
     */
    private byte[] length;
    /**
     * 数据
     */
    private byte[] data;
    /**
     * 签名 1字节，数据部分的和 &0xFF 的值，数据区无数据时默认0xFF
     */
    private byte sign;

    /**
     * 不建议使用，供序列化用
     */
    @Deprecated
    public Datagram() {
    }

    /**
     * 生成发送的数据
     *
     * @param command 4位长度命令
     * @param data    有效数据
     */
    public Datagram(byte[] command, byte[] data) {
        this.command = command;
        this.length = dataLengthBytes(data.length);
        this.data = data;
        sign();
    }

    /**
     * 将收取到的完整报文解析回原始数据
     *
     * @param fullData 完整一帧数据
     */
    public Datagram(byte[] fullData) {
        //读取命令长度数据
        this.command = new byte[COMMAND_LENGTH];
        System.arraycopy(fullData, HEADER_LENGTH, command, 0, COMMAND_LENGTH);
        //读取数据长度数据
        this.length = new byte[DATA_LENGTH];
        System.arraycopy(fullData, HEADER_LENGTH + COMMAND_LENGTH, length, 0, DATA_LENGTH);
        //读取实际数据长度数据
        int dataLength = dataLength();
        this.data = new byte[dataLength];
        System.arraycopy(fullData, HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH, data, 0, dataLength);
        //读取签名长度数据
        this.sign = fullData[HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH + dataLength];
    }

    /**
     * 获取数据有效长度
     */
    public static int dataLength(byte[] lengthBytes) {
        byte[] b = new byte[1 + lengthBytes.length];
        b[0] = 0;
        System.arraycopy(lengthBytes, 0, b, 1, lengthBytes.length);
        //取得数据长度
        return new BigInteger(b).intValue();
    }

    public byte[] getCommand() {
        return command;
    }

    /**
     * 获取命令字符串形式
     */
    public String getCommandStr() {
        return CommonDataUtils.bytesToHexString(command);
    }

    public byte[] getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public byte getSign() {
        return sign;
    }

    /**
     * 4位表示的data长度
     */
    public byte[] dataLengthBytes(int len) {
        byte[] buffer = new byte[DATA_LENGTH];
        for (int i = 0; i < DATA_LENGTH; ++i) {
            buffer[i] = (byte) (len >>> ((DATA_LENGTH - (i + 1)) * 8));
        }
        return buffer;
    }

    /**
     * 获取数据有效长度
     */
    public int dataLength() {
        //取得数据长度
        return dataLength(length);
    }

    /**
     * 签名
     */
    private void sign() {
        if (ObjectUtils.isEmpty(data)) {
            sign = (byte) 0xFF;
            return;
        }
        long mSum = 0;
        for (byte datum : data) {
            mSum += (long) datum;
        }
        sign = (byte) (mSum & 0xFF);
    }

    /**
     * 求和签名验证
     */
    public boolean checkSign() {
        long mSum = 0;
        if (ObjectUtils.isEmpty(data)) {
            mSum = 0xFF;
        } else {
            for (byte datum : data) {
                mSum += (long) datum;
            }
        }
        return sign == (byte) (mSum & 0xFF);
    }

    /**
     * 取得完整数据报文
     * <p>
     * 4位报文头长
     * 2位指令长
     * 4位数据长度
     * 不定位数据长度
     * 1位签名长度
     * 4位报文尾长
     */
    public byte[] fullData() {
        int dl = dataLength();
        byte[] buffer = new byte[HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH
                + dl +
                SIGN_LENGTH + FOOTER_LENGTH];
        System.arraycopy(HEADER, 0, buffer, 0, HEADER.length);
        System.arraycopy(command, 0, buffer, HEADER_LENGTH, COMMAND_LENGTH);
        System.arraycopy(length, 0, buffer, HEADER_LENGTH + COMMAND_LENGTH, DATA_LENGTH);
        System.arraycopy(data, 0, buffer, HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH, dl);
        buffer[HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH
                + dl] = sign;
        System.arraycopy(FOOTER, 0, buffer, HEADER_LENGTH + COMMAND_LENGTH + DATA_LENGTH
                + dl
                + SIGN_LENGTH, FOOTER.length);
        return buffer;
    }
}
