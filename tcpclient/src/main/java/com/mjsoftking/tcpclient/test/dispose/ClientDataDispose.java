package com.mjsoftking.tcpclient.test.dispose;

import com.blankj.utilcode.util.LogUtils;
import com.mjsoftking.tcpclient.test.CommonDataUtils;
import com.mjsoftking.tcpclient.test.Datagram;
import com.mjsoftking.tcpclient.test.event.TcpClientReceiveDataEvent;
import com.mjsoftking.tcplib.dispose.TcpBaseDataDispose;
import com.mjsoftking.tcplib.list.ByteQueueList;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

/**
 * 用途：
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/22
 */
public class ClientDataDispose implements TcpBaseDataDispose {

    private final static String TAG = ClientDataDispose.class.getSimpleName();

    //接收到不符合最小长度数据时，每次运行时线程挂起等待的时间，单位毫秒
    private static final int SLEEP_TIME = 100;
    //接收到不符合最小长度数据时，等待的超时时间，单位秒
    private static final int TIMEOUT = 10 * 60 * (1000 / SLEEP_TIME);
    //计次，最低报文完成长度
    private int metering = 0;
    //计次，完成报文长度
    private int metering2 = 0;

    @Override
    public void dispose(ByteQueueList bufferQueue, int servicePort, String clientAddress) {
        try {
            //缓冲区数据长度必须满足无数据最小的整包长度，方可计算
            int bufferSize = bufferQueue.size();
            if (bufferSize < (Datagram.HEADER_LENGTH +
                    Datagram.COMMAND_LENGTH + Datagram.DATA_LENGTH + Datagram.SIGN_LENGTH + Datagram.FOOTER_LENGTH)) {
                ++metering;
                if (metering > TIMEOUT) {
                    metering = 0;
                    byte[] b = bufferQueue.copy(bufferSize);
                    bufferQueue.removeCountFrame(bufferSize);
                    LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文长度不足，且等待缓冲区超时，移除队列数据：" +
                            CommonDataUtils.bytesToHexString(b, " "));
                } else {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ignore) {
                    }
                }

                return;
            }
            metering = 0;

            //验证报文头是否匹配
            if (!Arrays.equals(Datagram.HEADER, bufferQueue.copy(Datagram.HEADER_LENGTH))) {
                //不匹配时移除首位byte
                byte b = bufferQueue.removeFirstFrame();
                LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文头不匹配，移除首帧：" + CommonDataUtils.bytesToHexString(b));
                return;
            }
            //读取数据的长度
            int dataLength = Datagram.dataLength(bufferQueue.copy(Datagram.HEADER_LENGTH +
                    Datagram.COMMAND_LENGTH, Datagram.DATA_LENGTH));
            //报文的完整长度
            int length = Datagram.HEADER_LENGTH +
                    Datagram.COMMAND_LENGTH + Datagram.DATA_LENGTH + dataLength + Datagram.SIGN_LENGTH + Datagram.FOOTER_LENGTH;

            //等待报文完整长度，超时时清除无效数据
            int bufferSize2 = bufferQueue.size();
            if (bufferSize2 < length) {
                ++metering2;
                if (metering2 > TIMEOUT) {
                    metering2 = 0;
                    byte[] b = bufferQueue.copyAndRemove(bufferSize2);
                    LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文长度不足，且等待缓冲区超时，移除队列数据：" +
                            CommonDataUtils.bytesToHexString(b, " "));
                } else {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException ignore) {
                    }
                }

                return;
            }
            metering2 = 0;

            //验证报文尾
            if (!Arrays.equals(Datagram.FOOTER,
                    bufferQueue.copy(Datagram.HEADER_LENGTH +
                            Datagram.COMMAND_LENGTH + Datagram.DATA_LENGTH + dataLength + Datagram.SIGN_LENGTH, Datagram.FOOTER_LENGTH))) {
                int index2 = -1;
                for (int i = Datagram.HEADER_LENGTH; i < bufferQueue.size() - Datagram.HEADER_LENGTH; ++i) {
                    boolean exist = false;
                    for (int y = 0; y < Datagram.HEADER_LENGTH; ++y) {
                        if (bufferQueue.get(i + y) != Datagram.HEADER[y]) {
                            exist = false;
                            break;
                        }
                        exist = true;
                    }
                    if (exist) {
                        index2 = i;
                        break;
                    }
                }
                //在完成数据包内发现新包头时，移除无效包，反之移除首帧
                if (-1 == index2) {
                    byte[] b = bufferQueue.copyAndRemove(Datagram.HEADER_LENGTH +
                            Datagram.COMMAND_LENGTH + Datagram.DATA_LENGTH + dataLength + Datagram.SIGN_LENGTH + Datagram.FOOTER_LENGTH);
                    LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文尾不匹配，且不存在新报文头，移除完整包长度：" + CommonDataUtils.bytesToHexString(b));
                } else {
                    byte[] b = bufferQueue.copyAndRemove(index2);
                    LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文尾不匹配，且存在新报文头，移除新报文头前的报文：" + CommonDataUtils.bytesToHexString(b));
                }
                return;
            }

            //取出报文并移除队列, 解析报文数据至对象
            byte[] full = bufferQueue.copyAndRemove(length);
            if (null == full) {
                LogUtils.w(TAG, "客户端地址：" + clientAddress + "，截取报文失败，获取为null");
                return;
            }
            Datagram datagram = new Datagram(full);
            if (!datagram.checkSign()) {
                //签名校验失败
                LogUtils.w(TAG, "客户端地址：" + clientAddress + "，报文签名校验失败：" + CommonDataUtils.bytesToHexString(full, " "));
                return;
            }

            EventBus.getDefault().post(new TcpClientReceiveDataEvent(servicePort, clientAddress, new String(datagram.getData())));
        } catch (Exception e) {
            LogUtils.e(TAG, "客户端地址：" + clientAddress + "，缓冲区数据处理发生异常", e);
        }
    }
}
