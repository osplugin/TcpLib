# TcpLibApp
[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)
[![](https://jitpack.io/v/com.gitee.osard/TcpLib.svg)](https://jitpack.io/#com.gitee.osard/TcpLib)

### 介绍
安卓 Java tcp提炼封装工具, 目前已支持一台手机建立多个端口监听服务器且使用各自的报文处理规则，一个手机对多个端口服务器进行连接且使用各自的报文处理规则。

### 更新
#### V1.1.3 （2022-11-09）
- 修改缓存区相关方法，使用提取谷歌高效转换方法，提高缓冲区读取速率。
#### V1.1.2 （2022-09-08）
- 缓冲区列表对象修改，避免出现集合修改错误。
#### V1.1.1 （2022-02-17）
- 缓冲区列表对象增加写操作时的线程锁，避免出现集合修改错误。
- 缓冲区为避免方法函数错用，以将常规方法函数进行删除标记。

### 一、项目介绍

1.  **TcpLib**  aar资源项目，需要引入的资源包项目，aar资源已申请联网权限。 **现已支持jitpack引入。** 
2.  **TcpService**  为APP类型，服务端演示程序。
3.  **tcpclient**  为APP类型，客户端演示程序。 

### 二、工程引入工具包
 **com.android.tools.build:gradle:7.0.0以下版本，工程的build.gradle文件添加** 

```
allprojects {
    repositories {
        google()
        mavenCentral()

        //jitpack 仓库
        maven { url 'https://jitpack.io' }
    }
}
```
**com.android.tools.build:gradle:7.0.0及以上版本，在工程的 settings.gradle 文件添加**

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        //jitpack 仓库
        maven {
            url 'https://jitpack.io'
        }
    }
}
```

**APP的build.gradle文件添加** 
```
dependencies {
    ...
    implementation 'com.gitee.osard:TcpLib:1.1.3'
    implementation 'org.greenrobot:eventbus:3.3.1'
}
```
### 三、配置debug模式
在application下注册debug模式，可以打印更多log日志。

```
//TCP服务设定debug模式，在debug下打印log日志
TcpLibConfig.getInstance()
        .setDebugMode(BuildConfig.DEBUG)
        //设置连接断开后缓存区数据继续保留的时间，单位：分钟，超出此时间后缓存数据扔未处理完时将会被自动清理。
        .setRetentionTime(30);
```
### 四、重写服务报文接收及发送处理
 **- 接收报文处理** 

 **此为简单示例** ，也可以定义带报文头、报文尾、数据验证等的处理方式，具体规则完全由自己定义。bufferQueue处理一帧报文后需要在队列中移除这一帧报文数据。
必须实现接口 **TcpBaseDataDispose** 

```
public class DataDispose implements TcpBaseDataDispose {

    private final static String TAG = DataDispose.class.getSimpleName();

    @Override
    public void dispose(ByteQueueList bufferQueue, int servicePort, String clientAddress) {
        byte[] b = bufferQueue.copyAndRemove(bufferQueue.size());
        //todo 按照解析后的指令分发事件
        EventBus.getDefault().post(new TcpServiceReceiveDataEvent(servicePort, clientAddress, new String(b)));
    }
}
```
 **- 发送报文处理** 

 **此为简单示例** ，也可以定义带报文头、报文尾、数据验证等的处理方式，具体规则完全由自己定义。
必须实现接口 **TcpBaseDataGenerate** 

```
public class DataGenerate implements TcpBaseDataGenerate {

    @Override
    public byte[] generate(Object content) {
        //仅作为参考，不推荐此做法，缓冲区为10kb，请做好报文头和报文尾区分，避免缓冲区读取不完整
        if (content instanceof byte[]) {
            return (byte[]) content;
        } else if (content instanceof String) {
            return ((String) content).getBytes(Charset.forName("UTF-8"));
        } else {
            return content.toString().getBytes(Charset.forName("UTF-8"));
        }
    }
}
```
### 五、服务端的使用
 **- 服务端启动，需提供启动的端口号以及报文的处理和生成实现类** 

```
int port = 50000;
TcpLibService.getInstance()
                .bindService(port, TcpDataBuilder.builder(new DataGenerate(), new DataDispose()));
```

 **- 服务端关闭，关闭时需提供启动的端口号** 

```
int port = 50000;
TcpLibService.getInstance().close(port);
```
 **1. 服务端的启动、客户端事件处理** 

 **在任意对象下，创建实例时，以下以activity为例** 

- 注册EventBus
```
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        ...

    }
```
- 接收EventBus事件
```
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpBaseEvent et) {
        //服务端的事件在com.mjsoftking.tcplib.event.service包下
        //todo 自行处理的报文数据分发事件，
        if (et instanceof TcpServiceReceiveDataEvent) {
            TcpServiceReceiveDataEvent event = (TcpServiceReceiveDataEvent) et;
            Log.e(TAG, "服务端端口: " + event.getServicePort() + ", 地址: " + event.getAddress() + ", 接收到数据: " + event.getMessage());

            TcpLibService.getInstance().sendMessage(event.getServicePort(), event.getAddress(), "shou dao xiao xi");
        }
        //todo 服务启动成功
        else if (et instanceof TcpServiceBindSuccessEvent) {
            Log.w(TAG, String.format("服务器启动成功，端口：%d", et.getServicePort()));
        }
        //todo 服务启动失败
        else if (et instanceof TcpServiceBindFailEvent) {
            Log.w(TAG, String.format("服务器启动失败，端口：%d", et.getServicePort()));
        }
        //todo 服务关闭
        else if (et instanceof TcpServiceCloseEvent) {
            Log.w(TAG, String.format("服务器已关闭，端口：%d", et.getServicePort()));
        }
        //todo 客户端上线
        else if (et instanceof TcpClientConnectEvent) {
            Log.w(TAG, String.format("新客户端连接，服务端口：%d, 客户端地址：%s", et.getServicePort(), et.getAddress()));
        }
        //todo 客户端下线
        else if (et instanceof TcpClientDisconnectEvent) {
            Log.w(TAG, String.format("客户端连接断开，服务端口：%d, 客户端地址：%s", et.getServicePort(), et.getAddress()));
        }
        //todo 服务端发送消息事件
        else if (et instanceof TcpServiceSendMessageEvent) {
            TcpServiceSendMessageEvent event = (TcpServiceSendMessageEvent) et;
            Log.w(TAG, String.format("服务端发送消息，服务端口：%d, 客户端地址：%s，发送消息内容：%s", event.getServicePort(), event.getAddress(), event.getContent().toString());
        }
    }
```
- 注销EventBus

```
 @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
```

 **2. 服务端向客户端发送消息** 

```
int port = 50000;//服务端启动服务的端口
String address = "127.0.0.1:1233"; //服务端收到客户端连接事件时的地址 （ip:port）形式。
Object content = "数据";//此参数会进入TcpBaseDataGenerate 实现内，根据具体业务定义数据类型
TcpLibService.getInstance().sendMessage(port, address, content);
```
 **3. 服务端其他api** 

TcpLibService提供以下api
```
获取指定端口服务器是否在运行
boolean isRun(int port){}
```
```
获取指定端口服务器的在线客户端数量，在线客户端数；-1:服务器未启动，反之为在线数量
int getOnlineClientCount(int port){}
```
```
获取指定端口服务器的在线客户端，返回：null:服务器未启动，反之为在线客户端的ip:port形式列表，此内容可以直接在服务器向其发送数据
List<String> getOnlineClient(int port){}
```
```
关闭指定端口服务器下的客户端连接
void closeClient(int port, String address) {}
```

### 六、客户端的使用

**- 客户端启动，需提供IP和端口号以及报文的处理和生成实现类** 

```
Sting address = "127.0.0.1";
int port = 50000;
TcpLibClient.getInstance()
                   .connect(address, port ),
                            TcpDataBuilder.builder(new ClientDataGenerate(), new ClientDataDispose()));
```

 **- 客户端关闭，关闭时需提供IP和端口号** 

```
Sting address = "127.0.0.1";
int port = 50000;
TcpLibClient.getInstance()
                    .close(address, port);
```
**1. 客户端的启动、客户端事件处理** 

 **在任意对象下，创建实例时，以下以activity为例** 

- 注册EventBus
```
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        ...

    }
```
- 接收EventBus事件
```
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpBaseEvent et) {
        //客户端的事件在com.mjsoftking.tcplib.event.client包下
        //todo 自行处理的报文数据分发事件
        if (et instanceof TcpClientReceiveDataEvent) {
            TcpClientReceiveDataEvent event = (TcpClientReceiveDataEvent) et;
            Log.w(TAG, "服务端端口: " + event.getServicePort() + ", 服务端地址: " + event.getAddress() + ", 接收到数据: " + event.getMessage());
        }
        //todo 连接服务成功
        else if (et instanceof TcpServiceConnectSuccessEvent) {
            Log.w(TAG, "连接服务成功，服务端端口: " + et.getServicePort() + ", 服务端地址: " + et.getAddress());
        }
        //todo 连接服务失败
        else if (et instanceof TcpServiceConnectFailEvent) {
           Log.w(TAG, "连接服务失败，服务端端口: " + et.getServicePort() + ", 服务端地址: " + et.getAddress());
        }
        //todo 连接关闭
        else if (et instanceof TcpServiceDisconnectEvent) {
           Log.w(TAG, "连接关闭，服务端端口: " + et.getServicePort() + ", 服务端地址: " + et.getAddress());
        }
        //todo 客户端发送消息事件
        else if (et instanceof TcpClientSendMessageEvent) {
            TcpClientSendMessageEvent event = (TcpClientSendMessageEvent) et;
            Log.w(TAG, String.format("客户端发送消息，服务端口：%d, 服务端地址：%s，发送消息内容：%s", event.getServicePort(), event.getAddress(), event.getContent().toString();
        }
    }

```
- 注销EventBus

```
 @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
```

 **2. 客户端向服务端发送消息** 

```
int port = 50000;//服务端启动服务的端口
String address = "127.0.0.1"; //服务端IP地址。
Object content = "数据";//此参数会进入TcpBaseDataGenerate 实现内，根据具体业务定义数据类型
TcpLibClient.getInstance().sendMessage(address, port, content);
```
 **3. 服务端其他api** 

TcpLibClient提供以下api
```
获取是否指定的服务器处于连接状态
boolean boolean isConnect(String ipAddress, int port) {}
```
```
关闭与指定服务器的连接
void close(String ipAddress, int port)
```
### 七、比较复杂的报文解析处理
 **- 报文处理类** 

```
/**
 * 用途：报文结构类
 * <p>
 * 完整报文包含：
 * 4位报文头长
 * 4位指令长
 * 4位数据长度
 * 不定位数据长度
 * 1位签名长度
 * 4位报文尾长
 */
public class Datagram {

    /**
     * 报文头，0xDD,0xDD,0xDD,0xDD
     */
    public final static byte[] HEADER = new byte[]{(byte) 0xDD, (byte) 0xDD, (byte) 0xDD, (byte) 0xDD};
    /**
     * 报文尾，0xFF,0xFF,0xFF,0xFF
     */
    public final static byte[] FOOTER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 命令，预留4位
     */
    private byte[] command;
    /**
     * 数据长度，预留4位
     */
    private byte[] length;
    /**
     * 数据
     */
    private byte[] data;
    /**
     * 签名 1位，数据部分的和 &0xFF 的值
     */
    private byte sign;

    /**
     * 不建议使用，供序列化用
     */
    @Deprecated
    public Datagram() {
    }

    /**
     * 生成发送的数据
     *
     * @param command 4位长度命令
     * @param data    有效数据
     */
    public Datagram(byte[] command, byte[] data) {
        this.command = command;
        this.length = dataLengthBytes(data.length);
        this.data = data;
        sign();
    }

    /**
     * 将收取到的完整报文解析回原始数据
     *
     * @param fullData 完整一帧数据
     */
    public Datagram(byte[] fullData) {
        this.command = new byte[]{fullData[4], fullData[5], fullData[6], fullData[7]};
        this.length = new byte[]{fullData[8], fullData[9], fullData[10], fullData[11]};
        int dataLength = dataLength();
        this.data = new byte[dataLength];
        System.arraycopy(fullData, 12, this.data, 0, dataLength);
        this.sign = fullData[12 + dataLength];
    }

    /**
     * 获取数据有效长度
     */
    public static int dataLength(byte[] lengthBytes) {
        //取得数据长度
        return new BigInteger(lengthBytes).intValue();
    }

    public byte[] getCommand() {
        return command;
    }

    public byte[] getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public byte getSign() {
        return sign;
    }

    /**
     * 4位表示的data长度
     *
     * @param len
     * @return
     */
    public byte[] dataLengthBytes(int len) {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) (len >>> 24);
        buffer[1] = (byte) (len >>> 16);
        buffer[2] = (byte) (len >>> 8);
        buffer[3] = (byte) (len);
        return buffer;
    }

    /**
     * 获取数据有效长度
     */
    public int dataLength() {
        //取得数据长度
        return dataLength(length);
    }

    /**
     * 签名
     */
    private void sign() {
        long mSum = 0;
        for (int i = 0; i < data.length; ++i) {
            mSum += (long) data[i];
        }
        sign = (byte) (mSum & 0xff);
    }

    /**
     * 求和签名验证
     */
    public boolean checkSign() {
        long mSum = 0;
        for (int i = 0; i < data.length; ++i) {
            mSum += (long) data[i];
        }
        return sign == (byte) (mSum & 0xff);
    }

    /**
     * 取得完整数据报文
     * <p>
     * 4位报文头长
     * 4位指令长
     * 4位数据长度
     * 不定位数据长度
     * 1位签名长度
     * 4位报文尾长
     */
    public byte[] fullData() {
        byte[] buffer = new byte[4 + 4 + 4 + dataLength() + 1 + 4];
        System.arraycopy(HEADER, 0, buffer, 0, HEADER.length);
        System.arraycopy(command, 0, buffer, 4, command.length);
        System.arraycopy(length, 0, buffer, 8, length.length);
        System.arraycopy(data, 0, buffer, 12, data.length);
        buffer[12 + data.length] = sign;
        System.arraycopy(FOOTER, 0, buffer, 12 + data.length + 1, FOOTER.length);
        return buffer;
    }
}

```
 **- 报文解析类的处理方案** 

```
public class TcpServiceDispose implements TcpBaseDataDispose {

    TcpServiceDispose() {
    }

    public static synchronized TcpServiceDispose creteObject() {
        return new TcpServiceDispose();
    }

    @Override
    public void dispose(ByteQueueList bufferQueue, int servicePort, String address) {
        //缓冲区数据长度必须满足无数据大小的整包长度，方可计算
        if (bufferQueue.size() < (4 + 4 + 4 + 1 + 4)) {
            return;
        }
        //验证报文头是否匹配
        if (!Arrays.equals(Datagram.HEADER, bufferQueue.copy(4))) {
            //不匹配时移除首位byte
            bufferQueue.removeFirstFrame();
            return;
        }
        //读取数据的长度
        int dataLength = Datagram.dataLength(new byte[]{
                bufferQueue.get(8),
                bufferQueue.get(9),
                bufferQueue.get(10),
                bufferQueue.get(11)});
        //报文的完整长度
        int length = 4 + 4 + 4 + dataLength + 1 + 4;
        //取出报文并移除队列
        byte[] bytes = bufferQueue.copyAndRemove(length);
        //验证报文尾
        if (!Arrays.equals(Datagram.FOOTER, new byte[]{
                bufferQueue.get(4 + 4 + 4 + dataLength + 1),
                bufferQueue.get(4 + 4 + 4 + dataLength + 1 + 1),
                bufferQueue.get(4 + 4 + 4 + dataLength + 1 + 2),
                bufferQueue.get(4 + 4 + 4 + dataLength + 1 + 3)
        })) {
            //todo 报文无效
            return;
        }

        Datagram datagram = new Datagram(bytes);
        if (!datagram.checkSign()) {
            //签名校验失败
            return;
        }
        //todo 根据命令做事件分发，以及针对命令对数据做处理

        //命令 byte[4]
        datagram.getCommand();
        //实际数据 byte[n]
        datagram.getData();

        。。。
    }
}
```


License
-------

    Copyright 2021 mjsoftking

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



