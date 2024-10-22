package com.mjsoftking.tcpclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.RegexUtils;
import com.mjsoftking.tcpclient.databinding.ActivityMainBinding;
import com.mjsoftking.tcpclient.databinding.LayoutTextBinding;
import com.mjsoftking.tcpclient.test.Datagram;
import com.mjsoftking.tcpclient.test.dispose.ClientDataDispose;
import com.mjsoftking.tcpclient.test.event.TcpClientReceiveDataEvent;
import com.mjsoftking.tcpclient.test.generate.ClientDataGenerate;
import com.mjsoftking.tcplib.BuildConfig;
import com.mjsoftking.tcplib.TcpLibClient;
import com.mjsoftking.tcplib.TcpLibConfig;
import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcplib.event.TcpBaseEvent;
import com.mjsoftking.tcplib.event.client.TcpClientSendMessageEvent;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectFailEvent;
import com.mjsoftking.tcplib.event.client.TcpServiceConnectSuccessEvent;
import com.mjsoftking.tcplib.event.client.TcpServiceDisconnectEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;

    //单例线程池
    private ExecutorService sendMessageExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        //配置debug模式
        TcpLibConfig.getInstance()
                .setDebugMode(BuildConfig.DEBUG);
        this.sendMessageExecutorService = Executors.newSingleThreadExecutor();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setClick(this);
        binding.setIsConnect(false);

        binding.setEtIp("192.168.1.105");
        binding.setEtPort("5000");

        binding.tipLayout.setOnLongClickListener(v -> {
            binding.tipLayout.removeAllViews();
            return true;
        });
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
        TcpLibClient.getInstance()
                .close();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpBaseEvent et) {
        //todo 自行处理的报文数据分发事件
        if (et instanceof TcpClientReceiveDataEvent) {
            TcpClientReceiveDataEvent event = (TcpClientReceiveDataEvent) et;
            printf("服务端端口: " + event.getServicePort() + ", 地址: " + event.getAddress() + ", 接收到数据: " + event.getMessage(), false);
        }
        //todo 连接服务成功
        else if (et instanceof TcpServiceConnectSuccessEvent) {
            binding.setIsConnect(true);
            printf("连接服务端成功, 服务端地址:  " + et.getAddress(), false);
        }
        //todo 连接服务失败
        else if (et instanceof TcpServiceConnectFailEvent) {
            binding.setIsConnect(false);
            printf("连接服务端失败, 服务端地址:  " + et.getAddress(), true);
        }
        //todo 连接服务关闭
        else if (et instanceof TcpServiceDisconnectEvent) {
            binding.setIsConnect(false);
            printf("与服务端连接关闭, 服务端地址:  " + et.getAddress(), true);
        }
        //todo 客户端发送消息事件
        else if (et instanceof TcpClientSendMessageEvent) {
            TcpClientSendMessageEvent event = (TcpClientSendMessageEvent) et;
            if (event.getContent() instanceof Datagram) {
                Datagram datagram = (Datagram) event.getContent();

                printf("向服务端端口: " + et.getServicePort() + ", 服务端地址: "
                        + et.getAddress() + ", 发送消息字节数: " + datagram.getData().length, false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        //
        if (v.equals(binding.connect)) {
            if (!RegexUtils.isIP(binding.getEtIp())
                    || TextUtils.isEmpty(binding.getEtPort())
                    || Integer.parseInt(binding.getEtPort()) > 65565
            ) {
                printf("Ip或Port输入不正确", true);
                return;
            }

            //发起连接
            TcpLibClient.getInstance()
                    .connect(binding.getEtIp(), Integer.parseInt(binding.getEtPort()),
                            TcpDataBuilder.builder(ClientDataGenerate.class, ClientDataDispose.class));
        }
        //
        else if (v.equals(binding.disconnect)) {
            if (TextUtils.isEmpty(binding.getEtIp())
                    || TextUtils.isEmpty(binding.getEtPort())
            ) {
                return;
            }
            TcpLibClient.getInstance()
                    .close();
        }
        //
        else if (v.equals(binding.send)) {
            if (TextUtils.isEmpty(binding.getEtIp())
                    || TextUtils.isEmpty(binding.getEtPort())
            ) {
                return;
            }

            if (TextUtils.isEmpty(binding.getEtContent())) {
                printf("消息内容为空", true);
                return;
            }
            if (!TcpLibClient.getInstance()
                    .isConnect(binding.getEtIp(), Integer.parseInt(binding.getEtPort()))) {
                printf("与服务器连接已断开", true);
                return;
            }

//            for (int i = 0; i < 159; ++i) {
//                byte[] b = new byte[]{0x01, 0x00, (byte) i,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        1, 2, 3, 4, 5, (byte) 205, (byte) 205, 8, 9, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                        0, 0, 0, 0
//                };
//
//                TcpLibClient.getInstance().sendMessage(new Datagram(new byte[]{(byte) 0x10},
//                        b));


            TcpLibClient.getInstance().sendMessage(new Datagram(new byte[]{(byte) 0xB1},
                    new byte[]{0x01, 0x31, 0x2E, 0x31}));

//        }

//            sendMessageExecutorService.submit(new Runnable() {
//                @Override
//                public void run() {
//
//
//                }
//            });


//            TcpLibClient.getInstance().sendMessage(new Datagram(new byte[]{0x00, 0x01},
//                    binding.getEtContent().getBytes(Charset.forName("UTF-8"))));
        }

    }


    private void printf(String result, boolean error) {
        LayoutTextBinding textBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.layout_text, null, false);
        textBinding.setMessage(result);
        textBinding.setTimeStr(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        textBinding.setError(error);
        binding.tipLayout.addView(textBinding.getRoot());

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.tipScroll.fullScroll(View.FOCUS_DOWN);
        });
    }
}