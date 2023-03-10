package com.osard.udplib;

/**
 * 用途：配置
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class UdpLibConfig {

    private final static String TAG = UdpLibConfig.class.getSimpleName();

    private static UdpLibConfig TCP_CONFIG;
    /**
     * debug模式，开启时会打印log，关闭时不打印
     */
    private boolean debugMode;
    /**
     * 断开连接时，缓冲区内数据留存时间，单位：分钟
     * <p>
     * 默认：30分钟
     */
    private int retentionTime;


    /**
     * 接收缓存区的大小
     * <p>
     * 默认：100 * 1024 (100Kb)
     * <p>
     * 注：需要按照实际一包不会超出的大小进行定义，不建议过大，过大时处理过慢
     */
    private int receiveCacheBufferSize;

    /**
     * 缓存区大小
     * <p>
     * 默认：1Mb
     */
    private int receiveBufferSize;

    private UdpLibConfig() {
        this.retentionTime = 30;
        this.receiveCacheBufferSize = 100 * 1024;
        this.receiveBufferSize = 1024 * 1024;
        this.debugMode = true;
    }

    public static synchronized UdpLibConfig getInstance() {
        if (null == TCP_CONFIG) {
            TCP_CONFIG = new UdpLibConfig();
        }
        return TCP_CONFIG;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * debug模式，开启时会打印log，关闭时不打印
     * <p>
     * 建议设置为主项目的 BuildConfig.DEBUG 参数
     */
    public UdpLibConfig setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public int getRetentionTime() {
        return retentionTime;
    }

    /**
     * 断开连接时，缓冲区内数据留存时间，单位：分钟
     * <p>
     * 默认：30分钟
     */
    public UdpLibConfig setRetentionTime(int retentionTime) {
        this.retentionTime = retentionTime;
        return this;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public UdpLibConfig setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    public int getReceiveCacheBufferSize() {
        return receiveCacheBufferSize;
    }

    public UdpLibConfig setReceiveCacheBufferSize(int receiveCacheBufferSize) {
        this.receiveCacheBufferSize = receiveCacheBufferSize;
        return this;
    }

}
