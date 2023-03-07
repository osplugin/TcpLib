package com.mjsoftking.tcplib.list;

import android.annotation.SuppressLint;
import android.util.Log;

import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.utils.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 用途：线程安全的 byte 列表
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/23
 */
public class ByteQueueList extends ArrayList<Byte> {

    private final static String TAG = ByteQueueList.class.getSimpleName();

    /**
     * 将byte[]按照数组顺序逐个添加到队列指定索引的末尾
     *
     * @param c byte[]
     */
    public synchronized boolean add(byte... c) {
        //数组为null或者大小为0时，直接返回false
        if (null == c || c.length == 0) {
            return false;
        }
        return super.addAll(Bytes.asList(c));
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    @Deprecated
    public boolean add(Byte aByte) {
        return false;
    }

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends Byte> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean addAll(Collection<? extends Byte> c) {
        return false;
    }

    @Override
    @Deprecated
    public Byte remove(int index) {
        return null;
    }

    @Override
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    @Deprecated
    public boolean removeIf(Predicate<? super Byte> filter) {
        return false;
    }

    @Override
    @Deprecated
    public boolean remove(Object o) {
        return false;
    }

    /**
     * 从缓存区移除所有与给定报文头不匹配的数据
     * <p>
     * 第一个头数据找不到时移除当前缓存区执行方法时记录的缓存区数据；
     * <p>
     * 找到第一个头数据匹配时查找对应位置开始是否与头数据完全匹配：
     * 若匹配则移除第一个头数据匹配时索引前的全部数据；
     * 反之移除一个字节，等待下次执行方法。
     */
    public synchronized boolean removeFrameToHeader(byte[] header) {
        if (null == header || header.length == 0) return false;

        int size = size();
        int le = header.length;

        int index = indexOf(header[0]);
        if (index == -1) {
            removeCountFrame(size);
            return false;
        }
        if (Arrays.equals(header, copy(index, le))) {
            removeCountFrame(index);
            return true;
        } else {
            if (index == 0) {
                removeFirstFrame();
            } else {
                removeCountFrame(index);
            }
            return false;
        }
    }

    /**
     * 从开始位置移除一个数据
     */
    public synchronized Byte removeFirstFrame() {
        try {
            if (size() > 0) {
                return super.remove(0);
            }
            return null;
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
    public synchronized void removeCountFrame(int count) {
        if (count > 0) {
            int c = Math.min(size(), count);
            super.subList(0, c).clear();
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
     * @return 参数无效或索引与长度相加超出列表大小时，返回null
     */
    public synchronized byte[] copy(int start, int count) {
        if (start < 0 || count <= 0) return null;
        if ((start + count) > super.size()) return null;

        List<Byte> buffer = super.subList(start, start + count);
        return Bytes.toArray(buffer);
    }

    /**
     * 从开始位置复制一定长度的数组并移除此长度数组
     *
     * @param count 长度，大于0
     * @return 长度不合法时返回null
     */
    public synchronized byte[] copyAndRemove(int count) {
        if (count <= 0) return null;
        if ((count) > super.size()) return null;

//            Log.w("TCP-TAG", "复制数据：" + System.currentTimeMillis());
        List<Byte> buffer = super.subList(0, count);
//            Log.w("TCP-TAG", "转换数据：" + System.currentTimeMillis());
        byte[] b = Bytes.toArray(buffer);
//            Log.w("TCP-TAG", "清除数据：" + System.currentTimeMillis());
        buffer.clear();
//            Log.w("TCP-TAG", "完成数据：" + System.currentTimeMillis());
        return b;
    }

}
