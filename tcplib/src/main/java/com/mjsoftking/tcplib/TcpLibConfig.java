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

    public static synchronized TcpLibConfig getInstance() {
        if (null == TCP_CONFIG) {
            TCP_CONFIG = new TcpLibConfig();
        }
        return TCP_CONFIG;
    }

    private TcpLibConfig() {
    }

    /**
     * debug模式，开启时会打印log，关闭时不打印
     */
    private boolean debugMode;

    public boolean isDebugMode() {
        return debugMode;
    }

    public TcpLibConfig setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }
}
