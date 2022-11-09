package com.mjsoftking.tcpserviceapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.CollectionUtils;
import com.mjsoftking.dialogutilslib.DialogLibCommon;
import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.TcpLibService;
import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.TcpBaseEvent;
import com.mjsoftking.tcplib.event.service.TcpClientConnectEvent;
import com.mjsoftking.tcplib.event.service.TcpClientDisconnectEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindFailEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceBindSuccessEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceCloseEvent;
import com.mjsoftking.tcplib.event.service.TcpServiceSendMessageEvent;
import com.mjsoftking.tcpserviceapp.adapter.ClientAdapter;
import com.mjsoftking.tcpserviceapp.bean.Client;
import com.mjsoftking.tcpserviceapp.databinding.ActivityMainBinding;
import com.mjsoftking.tcpserviceapp.databinding.LayoutTextBinding;
import com.mjsoftking.tcpserviceapp.test.dispose.ServiceDataDispose;
import com.mjsoftking.tcpserviceapp.test.event.TcpServiceReceiveDataEvent;
import com.mjsoftking.tcpserviceapp.test.generate.ServiceDataGenerate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private ClientAdapter adapter;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setClick(this);
        binding.setIsConnect(false);
        binding.setSelect(false);
        binding.setEtPort("5000");

        binding.tipLayout.setOnLongClickListener(v -> {
            binding.tipLayout.removeAllViews();
            count = 0;
            showByteCount();
            return true;
        });

        adapter = new ClientAdapter(this);
        binding.list.setAdapter(adapter);
        binding.list.setOnItemClickListener((parent, view, position, id) -> {
            Client client = adapter.getItem(position);
            adapter.refreshSelect(client);
            binding.setSelect(!TextUtils.isEmpty(adapter.getCurrentClient()));
        });
        binding.list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Client client = adapter.getItem(position);
                DialogLibCommon.create(MainActivity.this)
                        .setOnBtnOk(() -> TcpLibService.getInstance()
                                .closeClient(client.getAddress()))
                        .setMessage(String.format(Locale.getDefault(), "确定断开客户端'%s'连接吗", client.getAddress()))
                        .show();
                return true;
            }
        });

        //配置debug模式
        TcpLibConfig.getInstance()
                .setDebugMode(BuildConfig.DEBUG);

    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判断按下的是不是返回键
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > (2 * 1000)) {
                Toast.makeText(getApplicationContext(),
                        "再按一次退出",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        TcpLibService.getInstance().closeAll();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.start)) {
            if (TextUtils.isEmpty(binding.getEtPort())
                    || Integer.parseInt(binding.getEtPort()) > 65565
            ) {
                printf("Port输入不正确", true);
                return;
            }

            TcpLibService.getInstance()
                    .bindService(Integer.parseInt(binding.getEtPort()),
                            TcpDataBuilder.builder(new ServiceDataGenerate(),
                                    new ServiceDataDispose()));
        }
        //
        else if (v.equals(binding.close)) {
            //关闭服务
            TcpLibService.getInstance().close();
        }
        //
        else if (v.equals(binding.send)) {
            if (TextUtils.isEmpty(binding.getEtContent())) {
                printf("消息内容为空", true);
                return;
            }
            if (TextUtils.isEmpty(adapter.getCurrentClient())) {
                printf("选择客户端后发送消息", true);
                return;
            }

            TcpLibService.getInstance()
                    .sendMessage(adapter.getCurrentClient(), binding.getEtContent());
//            printf("发送消息: " + binding.getEtContent(), false);
//            TcpLibClient.getInstance().close("192.168.1.245:8088");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpBaseEvent et) {
        //todo 自行处理的报文数据分发事件
        if (et instanceof TcpServiceReceiveDataEvent) {
            TcpServiceReceiveDataEvent event = (TcpServiceReceiveDataEvent) et;
//            printf("服务端端口: " + event.getServicePort() + ", 地址: " + event.getAddress() + ", 接收到数据: " + event.getMessage(),
//                    false);
            printf("服务端端口: " + event.getServicePort() + ", 地址: " + event.getAddress() + ", 接收到数据数量: " + event.getMessage(),
                    false);
            count += event.getCount();
            showByteCount();
        }
        //todo 服务启动成功
        else if (et instanceof TcpServiceBindSuccessEvent) {
            binding.setIsConnect(true);
            printf(String.format(Locale.getDefault(), "服务器启动成功，端口：%d", et.getServicePort()), false);
        }
        //todo 服务启动失败
        else if (et instanceof TcpServiceBindFailEvent) {
            binding.setIsConnect(false);
            printf(String.format(Locale.getDefault(), "服务器启动失败，端口：%d", et.getServicePort()), true);
        }
        //todo 服务关闭
        else if (et instanceof TcpServiceCloseEvent) {
            binding.setIsConnect(false);
            printf(String.format(Locale.getDefault(), "服务器已关闭，端口：%d", et.getServicePort()), true);
        }
        //todo 客户端上线
        else if (et instanceof TcpClientConnectEvent) {
            refreshClient();
            printf(String.format(Locale.getDefault(), "新客户端连接，服务端口：%d, 客户端地址：%s", et.getServicePort(), et.getAddress()), false);
        }
        //todo 客户端下线
        else if (et instanceof TcpClientDisconnectEvent) {
            refreshClient();
            printf(String.format(Locale.getDefault(),
                    "客户端连接断开，服务端口：%d, 客户端地址：%s", et.getServicePort(), et.getAddress()), true);
        }
        //todo 服务端发送消息事件
        else if (et instanceof TcpServiceSendMessageEvent) {
            TcpServiceSendMessageEvent event = (TcpServiceSendMessageEvent) et;
            printf("从服务端端口: " + et.getServicePort() + ", 向客户端地址: "
                    + et.getAddress() + ", 发送消息: " + event.getContent().toString(), false);
        }
    }

    private void showByteCount() {
        binding.byteCount.setText(String.format(Locale.getDefault(), "%d 字节", count));
    }

    private void refreshClient() {
        List<String> clients = TcpLibService.getInstance().getOnlineClient();
        if (CollectionUtils.isEmpty(clients)) {
            adapter.clearAndRefresh();
        } else {
            adapter.clear();
            adapter.addAndRefresh(clients);
        }
        adapter.refreshSelect(adapter.getCurrentClient());
        binding.setSelect(!TextUtils.isEmpty(adapter.getCurrentClient()));
    }

    private void printf(String result, boolean error) {
        LayoutTextBinding textBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.layout_text, null, false);
        textBinding.setMessage(result);
        textBinding.setTimeStr(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.getDefault()).format(new Date()));
        textBinding.setError(error);
        binding.tipLayout.addView(textBinding.getRoot());

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.tipScroll.fullScroll(View.FOCUS_DOWN);
        });
    }
}