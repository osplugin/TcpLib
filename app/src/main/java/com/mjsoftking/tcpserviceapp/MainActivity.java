package com.mjsoftking.tcpserviceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mjsoftking.tcplib.TcpLibService;
import com.mjsoftking.tcplib.dispose.TcpDataBuilder;
import com.mjsoftking.tcpserviceapp.databinding.ActivityMainBinding;
import com.mjsoftking.tcpserviceapp.test.DataDispose;
import com.mjsoftking.tcpserviceapp.test.DataGenerate;
import com.mjsoftking.tcpserviceapp.test.event.TcpReceiveDataEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setClick(this);
        EventBus.getDefault().register(this);


        TcpLibService.getInstance()
                .bindService(30000, TcpDataBuilder.builder(new DataGenerate(), new DataDispose()));
//
//        TcpLibService.getInstance().close();


//        TcpLibClient.getInstance()
//                .setDataDispose(new DataDispose())
//                .setDataGenerate(new DataGenerate())
//                .connect("192.168.1.245", 8088);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(binding.send)){

            TcpLibService.getInstance()
                    .bindService(30000, TcpDataBuilder.builder(new DataGenerate(), new DataDispose()));
//            TcpLibClient.getInstance().sendMessage("192.168.1.245:8088", "192.168.1.245");
        } else if(v.equals(binding.close)){

            TcpLibService.getInstance().close(30000);
//            TcpLibClient.getInstance().close("192.168.1.245:8088");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventFun(TcpReceiveDataEvent event) {
        Log.e(TAG, "客户端IP: " + event.getAddress() + "\n接收到数据: " + event.getMessage());

        TcpLibService.getInstance().sendMessage(30000, event.getAddress(), "shou dao xiao xi");
    }

}