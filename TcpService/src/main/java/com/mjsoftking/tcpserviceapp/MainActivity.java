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
import com.mjsoftking.tcplib.BuildConfig;
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
import com.osard.udplib.UdpLibConfig;
import com.osard.udplib.UdpLibService;
import com.osard.udplib.dispose.UdpDataBuilder;

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

    public static int gamma_A[] = {
            0x00, 0x09, 0x0d, 0x11, 0x15, 0x18, 0x1a, 0x1d,
            0x1f, 0x22, 0x24, 0x26, 0x28, 0x2a, 0x2c, 0x2e,
            0x30, 0x32, 0x33, 0x35, 0x37, 0x39, 0x3a, 0x3c,
            0x3d, 0x3f, 0x40, 0x42, 0x43, 0x45, 0x46, 0x48,
            0x49, 0x4a, 0x4c, 0x4d, 0x4e, 0x50, 0x51, 0x52,
            0x53, 0x55, 0x56, 0x57, 0x58, 0x5a, 0x5b, 0x5c,
            0x5d, 0x5e, 0x5f, 0x61, 0x62, 0x63, 0x64, 0x65,
            0x66, 0x67, 0x68, 0x69, 0x6b, 0x6c, 0x6d, 0x6e,
            0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
            0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e,
            0x7f, (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85,
            (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8a, (byte) 0x8b, (byte) 0x8c, (byte) 0x8d,
            (byte) 0x8d, (byte) 0x8e, (byte) 0x8f, (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94,
            (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0x99, (byte) 0x9a,
            (byte) 0x9b, (byte) 0x9c, (byte) 0x9d, (byte) 0x9e, (byte) 0x9e, (byte) 0x9f, (byte) 0xa0, (byte) 0xa1,
            (byte) 0xa2, (byte) 0xa3, (byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7, (byte) 0xa7,
            (byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xab, (byte) 0xab, (byte) 0xac, (byte) 0xad, (byte) 0xae,
            (byte) 0xae, (byte) 0xaf, (byte) 0xb0, (byte) 0xb1, (byte) 0xb1, (byte) 0xb2, (byte) 0xb3, (byte) 0xb4,
            (byte) 0xb4, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7, (byte) 0xb7, (byte) 0xb8, (byte) 0xb9, (byte) 0xba,
            (byte) 0xba, (byte) 0xbb, (byte) 0xbc, (byte) 0xbd, (byte) 0xbd, (byte) 0xbe, (byte) 0xbf, (byte) 0xc0,
            (byte) 0xc0, (byte) 0xc1, (byte) 0xc2, (byte) 0xc2, (byte) 0xc3, (byte) 0xc4, (byte) 0xc5, (byte) 0xc5,
            (byte) 0xc6, (byte) 0xc7, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9, (byte) 0xca, (byte) 0xca, (byte) 0xcb,
            (byte) 0xcc, (byte) 0xcc, (byte) 0xcd, (byte) 0xce, (byte) 0xce, (byte) 0xcf, (byte) 0xd0, (byte) 0xd0,
            (byte) 0xd1, (byte) 0xd2, (byte) 0xd3, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5, (byte) 0xd5, (byte) 0xd6,
            (byte) 0xd7, (byte) 0xd7, (byte) 0xd8, (byte) 0xd9, (byte) 0xd9, (byte) 0xda, (byte) 0xdb, (byte) 0xdb,
            (byte) 0xdc, (byte) 0xdd, (byte) 0xdd, (byte) 0xde, (byte) 0xdf, (byte) 0xdf, (byte) 0xe0, (byte) 0xe1,
            (byte) 0xe1, (byte) 0xe2, (byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe4, (byte) 0xe5, (byte) 0xe6,
            (byte) 0xe6, (byte) 0xe7, (byte) 0xe8, (byte) 0xe8, (byte) 0xe9, (byte) 0xea, (byte) 0xea, (byte) 0xeb,
            (byte) 0xeb, (byte) 0xec, (byte) 0xed, (byte) 0xed, (byte) 0xee, (byte) 0xef, (byte) 0xef, (byte) 0xf0,
            (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf4, (byte) 0xf5,
            (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf7, (byte) 0xf8, (byte) 0xf8, (byte) 0xf9, (byte) 0xfa,
            (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfc, (byte) 0xfd, (byte) 0xfd, (byte) 0xfe, (byte) 0xff,

//gamma08:
            0x00, 0x03, 0x05, 0x07, 0x09, 0x0a, 0x0c, 0x0e,
            0x0f, 0x11, 0x13, 0x14, 0x16, 0x17, 0x19, 0x1a,
            0x1b, 0x1d, 0x1e, 0x1f, 0x21, 0x22, 0x23, 0x25,
            0x26, 0x27, 0x29, 0x2a, 0x2b, 0x2c, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x34, 0x35, 0x36, 0x37, 0x38,
            0x39, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41,
            0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
            0x4b, 0x4c, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53,
            0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b,
            0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62, 0x63,
            0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b,
            0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73,
            0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b,
            0x7c, 0x7d, 0x7e, 0x7f, (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83,
            (byte) 0x84, (byte) 0x85, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8a,
            (byte) 0x8b, (byte) 0x8c, (byte) 0x8d, (byte) 0x8e, (byte) 0x8f, (byte) 0x90, (byte) 0x91, (byte) 0x92,
            (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99,
            (byte) 0x9a, (byte) 0x9b, (byte) 0x9c, (byte) 0x9c, (byte) 0x9d, (byte) 0x9e, (byte) 0x9f, (byte) 0xa0,
            (byte) 0xa1, (byte) 0xa2, (byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7,
            (byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xab, (byte) 0xac, (byte) 0xad, (byte) 0xad, (byte) 0xae,
            (byte) 0xaf, (byte) 0xb0, (byte) 0xb1, (byte) 0xb2, (byte) 0xb3, (byte) 0xb4, (byte) 0xb4, (byte) 0xb5,
            (byte) 0xb6, (byte) 0xb7, (byte) 0xb8, (byte) 0xb9, (byte) 0xba, (byte) 0xbb, (byte) 0xbb, (byte) 0xbc,
            (byte) 0xbd, (byte) 0xbe, (byte) 0xbf, (byte) 0xc0, (byte) 0xc1, (byte) 0xc1, (byte) 0xc2, (byte) 0xc3,
            (byte) 0xc4, (byte) 0xc5, (byte) 0xc6, (byte) 0xc7, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9, (byte) 0xca,
            (byte) 0xcb, (byte) 0xcc, (byte) 0xcc, (byte) 0xcd, (byte) 0xce, (byte) 0xcf, (byte) 0xd0, (byte) 0xd1,
            (byte) 0xd2, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5, (byte) 0xd6, (byte) 0xd7, (byte) 0xd7,
            (byte) 0xd8, (byte) 0xd9, (byte) 0xda, (byte) 0xdb, (byte) 0xdc, (byte) 0xdc, (byte) 0xdd, (byte) 0xde,
            (byte) 0xdf, (byte) 0xe0, (byte) 0xe1, (byte) 0xe1, (byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe5,
            (byte) 0xe5, (byte) 0xe6, (byte) 0xe7, (byte) 0xe8, (byte) 0xe9, (byte) 0xea, (byte) 0xea, (byte) 0xeb,
            (byte) 0xec, (byte) 0xed, (byte) 0xee, (byte) 0xee, (byte) 0xef, (byte) 0xf0, (byte) 0xf1, (byte) 0xf2,
            (byte) 0xf3, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf7, (byte) 0xf8,
            (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff,

//gamma10:
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
            0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f,
            0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27,
            0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
            0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,
            0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57,
            0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f,
            0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67,
            0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
            0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77,
            0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
            (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87,
            (byte) 0x88, (byte) 0x89, (byte) 0x8a, (byte) 0x8b, (byte) 0x8c, (byte) 0x8d, (byte) 0x8e, (byte) 0x8f,
            (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
            (byte) 0x98, (byte) 0x99, (byte) 0x9a, (byte) 0x9b, (byte) 0x9c, (byte) 0x9d, (byte) 0x9e, (byte) 0x9f,
            (byte) 0xa0, (byte) 0xa1, (byte) 0xa2, (byte) 0xa3, (byte) 0xa4, (byte) 0xa5, (byte) 0xa6, (byte) 0xa7,
            (byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xab, (byte) 0xac, (byte) 0xad, (byte) 0xae, (byte) 0xaf,
            (byte) 0xb0, (byte) 0xb1, (byte) 0xb2, (byte) 0xb3, (byte) 0xb4, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7,
            (byte) 0xb8, (byte) 0xb9, (byte) 0xba, (byte) 0xbb, (byte) 0xbc, (byte) 0xbd, (byte) 0xbe, (byte) 0xbf,
            (byte) 0xc0, (byte) 0xc1, (byte) 0xc2, (byte) 0xc3, (byte) 0xc4, (byte) 0xc5, (byte) 0xc6, (byte) 0xc7,
            (byte) 0xc8, (byte) 0xc9, (byte) 0xca, (byte) 0xcb, (byte) 0xcc, (byte) 0xcd, (byte) 0xce, (byte) 0xcf,
            (byte) 0xd0, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5, (byte) 0xd6, (byte) 0xd7,
            (byte) 0xd8, (byte) 0xd9, (byte) 0xda, (byte) 0xdb, (byte) 0xdc, (byte) 0xdd, (byte) 0xde, (byte) 0xdf,
            (byte) 0xe0, (byte) 0xe1, (byte) 0xe2, (byte) 0xe3, (byte) 0xe4, (byte) 0xe5, (byte) 0xe6, (byte) 0xe7,
            (byte) 0xe8, (byte) 0xe9, (byte) 0xea, (byte) 0xeb, (byte) 0xec, (byte) 0xed, (byte) 0xee, (byte) 0xef,
            (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7,
            (byte) 0xf8, (byte) 0xf9, (byte) 0xfa, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xfe, (byte) 0xff,

//gamma13:
            0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x02,
            0x02, 0x03, 0x03, 0x04, 0x04, 0x05, 0x05, 0x06,
            0x06, 0x07, 0x08, 0x08, 0x09, 0x09, 0x0a, 0x0b,
            0x0b, 0x0c, 0x0d, 0x0d, 0x0e, 0x0f, 0x0f, 0x10,
            0x11, 0x11, 0x12, 0x13, 0x14, 0x14, 0x15, 0x16,
            0x16, 0x17, 0x18, 0x19, 0x19, 0x1a, 0x1b, 0x1c,
            0x1d, 0x1d, 0x1e, 0x1f, 0x20, 0x21, 0x21, 0x22,
            0x23, 0x24, 0x25, 0x26, 0x26, 0x27, 0x28, 0x29,
            0x2a, 0x2b, 0x2c, 0x2c, 0x2d, 0x2e, 0x2f, 0x30,
            0x31, 0x32, 0x33, 0x33, 0x34, 0x35, 0x36, 0x37,
            0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
            0x3f, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e,
            0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56,
            0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e,
            0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x67,
            0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
            0x70, 0x71, 0x72, 0x73, 0x74, 0x76, 0x77, 0x78,
            0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f, (byte) 0x81,
            (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x8a,
            (byte) 0x8b, (byte) 0x8c, (byte) 0x8d, (byte) 0x8e, (byte) 0x8f, (byte) 0x90, (byte) 0x91, (byte) 0x93,
            (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x9a, (byte) 0x9b, (byte) 0x9c,
            (byte) 0x9d, (byte) 0x9e, (byte) 0x9f, (byte) 0xa0, (byte) 0xa2, (byte) 0xa3, (byte) 0xa4, (byte) 0xa5,
            (byte) 0xa6, (byte) 0xa8, (byte) 0xa9, (byte) 0xaa, (byte) 0xab, (byte) 0xac, (byte) 0xad, (byte) 0xaf,
            (byte) 0xb0, (byte) 0xb1, (byte) 0xb2, (byte) 0xb3, (byte) 0xb5, (byte) 0xb6, (byte) 0xb7, (byte) 0xb8,
            (byte) 0xb9, (byte) 0xbb, (byte) 0xbc, (byte) 0xbd, (byte) 0xbe, (byte) 0xc0, (byte) 0xc1, (byte) 0xc2,
            (byte) 0xc3, (byte) 0xc4, (byte) 0xc6, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9, (byte) 0xcb, (byte) 0xcc,
            (byte) 0xcd, (byte) 0xce, (byte) 0xd0, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd6,
            (byte) 0xd7, (byte) 0xd8, (byte) 0xd9, (byte) 0xdb, (byte) 0xdc, (byte) 0xdd, (byte) 0xdf, (byte) 0xe0,
            (byte) 0xe1, (byte) 0xe2, (byte) 0xe4, (byte) 0xe5, (byte) 0xe6, (byte) 0xe7, (byte) 0xe9, (byte) 0xea,
            (byte) 0xeb, (byte) 0xec, (byte) 0xee, (byte) 0xef, (byte) 0xf0, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4,
            (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0xfb, (byte) 0xfc, (byte) 0xfd, (byte) 0xff
    };

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

        //Udp服务设定debug模式，在debug下打印log日志
        UdpLibConfig.getInstance()
                .setDebugMode(BuildConfig.DEBUG)
                .setReceiveBufferSize(1024 * 1024)
                .setReceiveCacheBufferSize(12 * 1024)
                //断开连接时，缓冲区内数据留存时间，单位：分钟
                .setRetentionTime(0);

        ///启动Udp服务
        UdpLibService.getInstance().bindService(5000,
                UdpDataBuilder.builder(null,
                        null));

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