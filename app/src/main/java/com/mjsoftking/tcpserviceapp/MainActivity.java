package com.mjsoftking.tcpserviceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.CollectionUtils;
import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.TcpLibService;
import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.TcpBaseEvent;
import com.mjsoftking.tcplib.event.service.TcpClientConnectEvent;
import com.mjsoftking.tcplib.event.service.TcpClientDisconnectEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindFailEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindSuccessEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceCloseEvent;
import com.mjsoftking.tcpserviceapp.databinding.ActivityMainBinding;
import com.mjsoftking.tcpserviceapp.test.dispose.ServiceDataDispose;
import com.mjsoftking.tcpserviceapp.test.event.TcpServiceReceiveDataEvent;
import com.mjsoftking.tcpserviceapp.test.generate.ServiceDataGenerate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final int port = 50000;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setClick(this);
        EventBus.getDefault().register(this);

        //配置debug模式
        TcpLibConfig.getInstance()
                .setDebugMode(BuildConfig.DEBUG);
        //启动服务
        TcpLibService.getInstance()
                .bindService(port, TcpDataBuilder.builder(new ServiceDataGenerate(),
                        new ServiceDataDispose()));
//
//        TcpLibService.getInstance().close();


//        TcpLibClient.getInstance()
//                .connect("192.168.1.245", 8088
//                        , TcpDataBuilder.builder(new DataGenerate(), new DataDispose()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.send)) {

            TcpLibService.getInstance()
                    .sendAllClientMessage(port, "The message sent by the service");
//            TcpLibClient.getInstance().sendMessage("192.168.1.245:8088", "192.168.1.245");
        } else if (v.equals(binding.start)) {
            //启动服务
            TcpLibService.getInstance()
                    .bindService(port,
                            TcpDataBuilder.builder(new ServiceDataGenerate(),
                                    new ServiceDataDispose()));
        }
        //
        else if (v.equals(binding.closeClient)) {
            List<String> clients = TcpLibService.getInstance().getOnlineClient(port);
            if (CollectionUtils.isNotEmpty(clients)) {
                Log.w(TAG, String.format("客户端%s被关闭", clients.get(0)));
                TcpLibService.getInstance().closeClient(port, clients.get(0));
            }
//            TcpLibClient.getInstance().close("192.168.1.245:8088");
        }
        //
        else if (v.equals(binding.close)) {

            TcpLibService.getInstance().close(port);
//            TcpLibClient.getInstance().close("192.168.1.245:8088");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpBaseEvent et) {
        //todo 自行处理的报文数据分发事件
        if (et instanceof TcpServiceReceiveDataEvent) {
            TcpServiceReceiveDataEvent event = (TcpServiceReceiveDataEvent) et;
            Log.w(TAG, "服务端端口: " + event.getServicePort() + ", 地址: " + event.getAddress() + ", 接收到数据: " + event.getMessage());

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
    }

}