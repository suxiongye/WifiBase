package com.example.suxiongye.wifibase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    private Button openWifiButton;
    private Button closeWifiButton;
    private Button findPeerButton;
    private Button refreshButton;
    private Button connectButton;
    private TextView textView;
    private TextView showPeersTextView;

    private WifiAdmin wifiAdmin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //p2p wifi setup
        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager,channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);



        openWifiButton = (Button)findViewById(R.id.openWifiButton);
        closeWifiButton = (Button)findViewById(R.id.closeWIfiButton);
        findPeerButton = (Button)findViewById(R.id.findPeerButton);
        refreshButton = (Button)findViewById(R.id.refreshPeersButton);
        connectButton = (Button)findViewById(R.id.connectButton);
        textView = (TextView) findViewById(R.id.textView);
        showPeersTextView = (TextView)findViewById(R.id.showPeersTextView);
        wifiAdmin = new WifiAdmin(this);


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
                wifiAdmin.closeWifi();
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


    private void discoverPeers(){
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

    private void refreshPeers(){
        List<WifiP2pDevice>list = ((WiFiDirectBroadcastReceiver)receiver).getPeersList();
        showPeersTextView.setText("");
        for (WifiP2pDevice device: list){
            showPeersTextView.append(device.deviceName+":"+device.deviceAddress+"\n");
        }
    }

    private void connectFirst(){
        List<WifiP2pDevice>list = ((WiFiDirectBroadcastReceiver)receiver).getPeersList();
        if(list.size() > 0){
         connectDevice(list.get(0));
        }
    }


    private void connectDevice(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
}
