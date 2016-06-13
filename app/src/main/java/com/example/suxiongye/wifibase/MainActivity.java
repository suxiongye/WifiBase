package com.example.suxiongye.wifibase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MainActivity extends Activity {

    //Log tag
    private static String TAG = "MainActivity";

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    private Button openWifiButton;
    private Button closeWifiButton;
    private Button findPeerButton;
    private Button refreshButton;
    private Button connectButton;
    private Button disconnectButton;
    private Button sendButton;
    private Button receiveButton;
    private TextView textView;
    private TextView fileStatusTextView;
    private TextView showPeersTextView;

    private WiFiAdmin wifiAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //p2p wifi setup
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        openWifiButton = (Button) findViewById(R.id.openWifiButton);
        closeWifiButton = (Button) findViewById(R.id.closeWIfiButton);
        findPeerButton = (Button) findViewById(R.id.findPeerButton);
        refreshButton = (Button) findViewById(R.id.refreshPeersButton);
        connectButton = (Button) findViewById(R.id.connectButton);
        disconnectButton = (Button) findViewById(R.id.diconnectButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        receiveButton = (Button) findViewById(R.id.receiveButton);
        textView = (TextView) findViewById(R.id.textView);
        fileStatusTextView = (TextView) findViewById(R.id.fileStatusTextView);
        showPeersTextView = (TextView) findViewById(R.id.showPeersTextView);
        wifiAdmin = new WiFiAdmin(manager, this);
        receiver = wifiAdmin.getWiFiBroadcastReceiver();

        openWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("wifi on");
                wifiAdmin.openWifi();
            }
        });

        closeWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("wifi off");
                wifiAdmin.createFile();
            }
        });

        findPeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverPeers();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPeers();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectFirst();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectDevice();
            }
        });

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });
    }

    /**
     * register the broadcast receiver with the intent values to be matched
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    /**
     * unregister the broadcast receiver
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                textView.setText("discover");
            }

            @Override
            public void onFailure(int reason) {
                textView.setText("no peers");
            }
        });
    }

    private void refreshPeers() {
        List<WifiP2pDevice> list = ((WiFiDirectBroadcastReceiver) receiver).getDevices();
        showPeersTextView.setText("");
        for (WifiP2pDevice device : list) {
            showPeersTextView.append(device.deviceName + ":" + device.deviceAddress + "\n");
        }
    }

    private void connectFirst() {
        List<WifiP2pDevice> list = ((WiFiDirectBroadcastReceiver) receiver).getDevices();
        if (list.size() > 0) {
            connectDevice(list.get(0));
        }
    }


    private void connectDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectButton.setText("Connecting");
                disconnectButton.setText("Disconnect");
            }

            @Override
            public void onFailure(int reason) {
                connectButton.setText("Connect");
            }
        });
    }

    private void disconnectDevice() {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectButton.setText("Connect");
                disconnectButton.setText("Disconnected");
            }

            @Override
            public void onFailure(int reason) {
                disconnectButton.setText("Disconnect");
                Log.e("wifi", "disconnect fail:" + reason);
            }
        });
    }

    public void sendFile(){
        wifiAdmin.sendFileByPath(Environment.getExternalStorageDirectory()+"/WifiBase/pic.png");
    }

}

