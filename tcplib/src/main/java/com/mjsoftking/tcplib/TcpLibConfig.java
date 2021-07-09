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

    private TcpLibConfig() {
        this.retentionTime = 30;
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
}
