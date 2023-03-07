package com.mjsoftking.tcplib;

/**
 * 用途：配置
 * <p>
 * 作者：MJSoftKing
 * 时间：2021/06/02
 */
public class TcpLibConfig {

    private final static String TAG = TcpLibConfig.class.getSimpleName();

    private static TcpLibConfig TCP_CONFIG;
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
     * 每次从TCP缓存区内取出数据放入buffer处理区的最大数据大小
     * <p>
     * 默认：512
     * <p>
     * 注：如存在实时接收数据时不建议设置过大，过大会导致buffer处理区无法完成处理
     */
    private int receiveReadSize;

    /**
     * TCP接收缓存区的大小
     * <p>
     * 默认：100 * 1024 (100Kb)
     * <p>
     * 注：需要按照实际一包不会超出的大小进行定义，不建议过大，过大时处理过慢
     */
    private int receiveCacheBufferSize;

    /**
     * TCP服务器的缓存区大小
     * <p>
     * 默认：1Mb
     */
    private int TcpServiceReceiveBufferSize;

    /**
     * TCP客户器的缓存区大小
     * <p>
     * 默认：1Mb
     */
    private int TcpClientReceiveBufferSize;

    /**
     * 服务器端读取客户端数据超时时间
     * <p>
     * 默认：5秒
     */
    private int serverReadTimeout;

    private TcpLibConfig() {
        this.serverReadTimeout = 5 * 1000;
        this.retentionTime = 30;
        this.receiveReadSize = 512;
        this.receiveCacheBufferSize = 100 * 1024;
        this.TcpServiceReceiveBufferSize = 1024 * 1024;
        this.TcpClientReceiveBufferSize = 1024 * 1024;
        this.debugMode = true;
    }

    public static synchronized TcpLibConfig getInstance() {
        if (null == TCP_CONFIG) {
            TCP_CONFIG = new TcpLibConfig();
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
    public TcpLibConfig setDebugMode(boolean debugMode) {
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
    public TcpLibConfig setRetentionTime(int retentionTime) {
        this.retentionTime = retentionTime;
        return this;
    }

    public int getReceiveReadSize() {
        return receiveReadSize;
    }

    public TcpLibConfig setReceiveReadSize(int receiveReadSize) {
        this.receiveReadSize = receiveReadSize;
        return this;
    }

    public int getTcpServiceReceiveBufferSize() {
        return TcpServiceReceiveBufferSize;
    }

    public TcpLibConfig setTcpServiceReceiveBufferSize(int tcpServiceReceiveBufferSize) {
        TcpServiceReceiveBufferSize = tcpServiceReceiveBufferSize;
        return this;
    }

    public int getTcpClientReceiveBufferSize() {
        return TcpClientReceiveBufferSize;
    }

    public TcpLibConfig setTcpClientReceiveBufferSize(int tcpClientReceiveBufferSize) {
        TcpClientReceiveBufferSize = tcpClientReceiveBufferSize;
        return this;
    }

    public int getReceiveCacheBufferSize() {
        return receiveCacheBufferSize;
    }

    public TcpLibConfig setReceiveCacheBufferSize(int receiveCacheBufferSize) {
        this.receiveCacheBufferSize = receiveCacheBufferSize;
        return this;
    }

    public int getServerReadTimeout() {
        return serverReadTimeout;
    }

    public TcpLibConfig setServerReadTimeout(int serverReadTimeout) {
        this.serverReadTimeout = serverReadTimeout;
        return this;
    }
}
