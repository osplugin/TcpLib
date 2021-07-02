package com.mjsoftking.tcplib.list;

import android.util.Log;

import com.mjsoftking.tcplib.TcpLibConfig;

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

    private final static String TAG = ByteQueueList.class.getSimpleName();

    /**
     * 添加一个byte数据到队列末尾
     */
    public boolean add(byte aByte) {
        return super.add(aByte);
    }

    /**
     * 将byte[]按照数组顺序逐个添加到队列末尾
     *
     * @param c byte[]
     */
    public boolean addAll(byte[] c) {
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
        try {
            return remove(0);
        } catch (IndexOutOfBoundsException e) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "队列移除首帧失败，" + e.getMessage());
            }
            return null;
        }
    }

    /**
     * 从开始位置移除一定长度数据
     *
     * @param count 长度，大于0
     */
    public void removeCountFrame(int count) {
        int c = Math.min(size(), count);
        while (c-- > 0) {
            removeFirstFrame();
        }
    }

    /**
     * 从开始位置复制一定长度的数组
     *
     * @param count 长度，大于0
     */
    public byte[] copy(int count) {
        return copy(0, count);
    }

    /**
     * 从指定索引位置复制一定长度的数组
     *
     * @param start 开始的索引位置
     * @param count 复制的长度
     */
    public byte[] copy(int start, int count) {
        if (start < 0 || count <= 0) return null;

        byte[] buffer = new byte[count];
        for (int i = 0; i < count; ++i) {
            buffer[i] = get(start + i);
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
        if (count > size()) return null;

        byte[] buffer = new byte[count];
        for (int i = 0; i < count; ++i) {
            buffer[i] = get(i);
        }

        removeCountFrame(count);

        return buffer;
    }

}
