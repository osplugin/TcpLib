package com.mjsoftking.tcplib.list;

import android.annotation.SuppressLint;
import android.util.Log;

import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.utils.Bytes;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * 用途：线程安全的 byte 列表
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/23
 */
public class ByteQueueList extends CopyOnWriteArrayList<Byte> {

    private final static String TAG = ByteQueueList.class.getSimpleName();
//    final static transient Object lock = new Object();

//    /**
//     * 将byte[]按照数组顺序逐个添加到队列末尾
//     *
//     * @param c byte[]
//     */
//    public boolean add(byte... c) {
//        synchronized (lock) {
//            return this.add(-1, c);
//        }
//    }

    /**
     * 将byte[]按照数组顺序逐个添加到队列指定索引的末尾
     *
     * @param c byte[]
     */
    public boolean add(byte... c) {
//        synchronized (lock) {
        //数组为null或者大小为0时，直接返回false
        if (null == c || c.length == 0) {
            return false;
        }
        return super.addAll(Bytes.asList(c));
//        }
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
     * 从开始位置移除一个数据
     */
    public Byte removeFirstFrame() {
//        synchronized (lock) {
        try {
            return super.remove(0);
        } catch (IndexOutOfBoundsException e) {
            if (TcpLibConfig.getInstance().isDebugMode()) {
                Log.w(TAG, "队列移除首帧失败，" + e.getMessage());
            }
            return null;
        }
//        }
    }

    /**
     * 从开始位置移除一定长度数据
     *
     * @param count 长度，大于0
     */
    public void removeCountFrame(int count) {
//        synchronized (lock) {
        int c = Math.min(size(), count);
        super.subList(0, c).clear();
//        }
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
    public byte[] copy(int start, int count) {
        if (start < 0 || count <= 0) return null;
        if ((start + count) > super.size()) return null;

        List<Byte> buffer = super.subList(start, count);
        return Bytes.toArray(buffer);
    }

    /**
     * 从开始位置复制一定长度的数组并移除此长度数组
     *
     * @param count 长度，大于0
     * @return 长度不合法时返回null
     */
    public byte[] copyAndRemove(int count) {
        if (count <= 0) return null;
        if (count > super.size()) return null;

        byte[] buffer = copy(count);

        removeCountFrame(count);

        return buffer;
    }

}
