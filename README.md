# TcpLibApp

#### 介绍
安卓 Java tcp提炼封装工具, 目前已支持一台手机建立多个端口监听服务器且使用各自的报文处理规则，一个手机对多个端口服务器进行连接且使用各自的报文处理规则。

#### 一、项目介绍
1. APP 使用示例项目，libs下含有已编译最新的aar资源。
2.  **TcpLib**  aar资源项目，需要引入的资源包项目，aar资源已申请联网权限。
3.  **TcpService**  为APP类型，服务端演示程序。
4.  **tcpclient**  为APP类型，客户端演示程序。 

#### 二、工程引入工具包
下载项目，可以在tcplib项目的libs文件下找到*.aar文件（已编译为最新版），选择其中一个引入自己的工程

```
dependencies {
   //引入tcplib.aar资源
   implementation fileTree(dir: 'libs', include: ['tcplib.aar'])
   //eventbus，引入后你的项目将支持EventBus，EventBus是一种用于Android的事件发布-订阅总线，替代广播的传值方式，使用方法可以度娘查询。
   implementation 'org.greenrobot:eventbus:3.2.0'
   ...
}
```
#### 三、配置debug模式
在application下注册debug模式，可以打印更多log日志。

```
 //配置debug模式
 TcpLibConfig.getInstance()
           .setDebugMode(BuildConfig.DEBUG);
```
#### 四、重写服务报文接收及发送处理
 **- 接收报文处理** 

简单示例，也可以定义带报文头、报文尾、数据验证等的处理方式，具体规则完全由自己定义。bufferQueue处理一帧报文后需要在队列中移除这一帧报文数据。
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
#### 五、服务端的使用
 **- 服务端启动，需提供启动的端口号** 

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
            //contentStr字符串为发送的消息字符串，contentBytes为发送消息的bute[]数据，按照发送消息的内容类型，2个参数仅有一个不为null
            TcpServiceSendMessageEvent event = (TcpServiceSendMessageEvent) et;
            Log.w(TAG, String.format("服务端发送消息，服务端口：%d, 客户端地址：%s，发送消息内容：%s", event.getServicePort(), event.getAddress(), event.getContentStr());
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

#### 六、客户端的使用

**- 客户端启动，需提供IP和端口号** 

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
            //contentStr字符串为发送的消息字符串，contentBytes为发送消息的bute[]数据，按照发送消息的内容类型，2个参数仅有一个不为null
            TcpClientSendMessageEvent event = (TcpClientSendMessageEvent) et;
            Log.w(TAG, String.format("客户端发送消息，服务端口：%d, 服务端地址：%s，发送消息内容：%s", event.getServicePort(), event.getAddress(), event.getContentStr());
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
