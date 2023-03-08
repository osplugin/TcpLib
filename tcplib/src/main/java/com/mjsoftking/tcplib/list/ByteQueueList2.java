package com.mjsoftking.tcplib.list;

import java.util.Arrays;

/**
 * 用途：线程安全的 byte 列表
 * <p>
 * 作者：mjSoftKing
 * 时间：2021/02/23
 */
public class ByteQueueList2 {

    private final static String TAG = ByteQueueList2.class.getSimpleName();

    transient byte[] elementData;
    int size;

    private void ensureExplicitCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - Integer.MAX_VALUE > 0)
            newCapacity = Integer.MAX_VALUE;
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * 将byte[]按照数组顺序逐个添加到队列指定索引的末尾
     *
     * @param c byte[]
     */
    public synchronized boolean add(byte... c) {
        int numNew = c.length;
        ensureExplicitCapacity(size + numNew);
        System.arraycopy(c, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    public synchronized void clear() {
//        // clear to let GC do its work
//        for (int i = 0; i < size; i++)
//            elementData[i] = 0;

        size = 0;
    }

    public int indexOf(byte o) {
        {
            for (int i = 0; i < size; i++)
                if (o == (elementData[i]))
                    return i;
        }
        return -1;
    }

    public int size() {
        return size;
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
    public synchronized int removeFrameToHeader(byte[] header) {
        if (null == header || header.length == 0) return -1;

        int le = header.length;

        int index = indexOf(header[0]);
        if (index == -1) {
            return -1;
        }

        boolean noExist = false;
        for (int i = 1; i < le; ++i) {
            if (header[i] != elementData[index + i]) {
                noExist = true;
                break;
            }
        }

        if (!noExist) {
            return index;
        } else {
            return -1;
        }
    }

    /**
     * 从开始位置移除一个数据
     */
    public synchronized void removeFirstFrame() {
        removeCountFrame(1);
    }

    /**
     * 从开始位置移除一定长度数据
     *
     * @param count 长度，大于0
     */
    public synchronized void removeCountFrame(int count) {
        if (count > 0) {
            int c = Math.min(size, count);

            int numMoved = size - c;
            if (numMoved > 0)
                System.arraycopy(elementData, c, elementData, 0,
                        numMoved);

            size = numMoved;
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
        if ((start + count) > size) return null;

        byte[] buffer = new byte[count];
        System.arraycopy(elementData, start, buffer, 0,
                count);
        return buffer;
    }

    /**
     * 从开始位置复制一定长度的数组并移除此长度数组
     *
     * @param count 长度，大于0
     * @return 长度不合法时返回null
     */
    public synchronized byte[] copyAndRemove(int count) {
        if (count <= 0) return null;
        if ((count) > size) return null;

        byte[] buffer = copy(0, count);
        removeCountFrame(count);
        return buffer;
//
////            Log.w("TCP-TAG", "复制数据：" + System.currentTimeMillis());
//        List<Byte> buffer = super.subList(0, count);
////            Log.w("TCP-TAG", "转换数据：" + System.currentTimeMillis());
//        byte[] b = Bytes.toArray(buffer);
////            Log.w("TCP-TAG", "清除数据：" + System.currentTimeMillis());
//        buffer.clear();
////            Log.w("TCP-TAG", "完成数据：" + System.currentTimeMillis());
//        return b;
    }

}
