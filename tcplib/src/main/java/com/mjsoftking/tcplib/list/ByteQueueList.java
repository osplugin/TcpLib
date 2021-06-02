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

    public boolean add(byte aByte) {
        return super.add(aByte);
    }

    public boolean addAll(@NonNull byte[] c) {
        List<Byte> list = new ArrayList<>();
        for (byte b : c) {
            list.add(b);
        }
        return super.addAll(list);
    }

    public Byte removeFirstFrame() {
        return remove(0);
    }

    public void removeCountFrame(int count) {
        while (count-- > 0) {
            removeFirstFrame();
        }
    }

}
