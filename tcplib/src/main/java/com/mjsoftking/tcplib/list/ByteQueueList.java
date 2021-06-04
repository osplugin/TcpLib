package com.mjsoftking.tcplib.list;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用途：线程安全的 byte 列表
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/23
 */
public class ByteQueueList extends CopyOnWriteArrayList<Byte> {

    /**
     * 添加一个byte数据到队列末尾
     */
    public boolean add(byte aByte) {
        return super.add(aByte);
    }

    /**
     * 将byte[]按照数组顺序逐个添加到队列末尾
     * @param c byte[]
     */
    public boolean addAll(@NonNull byte[] c) {
        List<Byte> list = new ArrayList<>();
        for (byte b : c) {
            list.add(b);
        }
        return super.addAll(list);
    }

    /**
     * 从开始位置移除一个数据
     */
    public Byte removeFirstFrame() {
        return remove(0);
    }

    /**
     * 从开始位置移除一定长度数据
     * @param count 长度，大于0
     */
    public void removeCountFrame(int count) {
        while (count-- > 0) {
            removeFirstFrame();
        }
    }

    /**
     * 从开始位置复制一定长度的数组
     *
     * @param count 长度，大于0
     */
    public byte[] copy(int count) {
        if (count <= 0) return null;

        byte[] buffer = new byte[count];
        for (int i = 0; i < count; ++i) {
            buffer[i] = get(i);
        }
        return buffer;
    }

    /**
     * 从开始位置复制一定长度的数组并移除此长度数组
     *
     * @param count 长度，大于0
     */
    public byte[] copyAndRemove(int count) {
        if (count <= 0) return null;

        byte[] buffer = new byte[count];
        for (int i = 0; i < count; ++i) {
            buffer[i] = get(i);
        }

        removeCountFrame(count);

        return buffer;
    }

}
