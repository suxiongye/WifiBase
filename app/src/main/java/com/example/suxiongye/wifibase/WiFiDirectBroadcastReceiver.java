package com.example.suxiongye.wifibase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by suxiongye on 6/10/16.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    private List<WifiP2pDevice> devices;

    /**
     * 初始化wifi广播接收器
     *
     * @param manager
     * @param channel
     * @param activity
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        devices = new ArrayList<WifiP2pDevice>();
    }

    /**
     * 接受消息处理动作
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.e("wifi", "p2p state enabled");
            } else {
                // Wi-Fi P2P is not enabled
                Log.e("wifi", "p2p state not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (manager != null) {
                manager.requestPeers(channel, this);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (manager == null) return;
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                WiFiAdmin.isConnected = true;
                Log.e("wifi", "connected");
                //启动接收
                if (WiFiAdmin.fileServer == null){
                    Log.e("wifi", "初始化文件服务器");
                    WiFiAdmin.fileServer = new Thread(new FileServer());
                    WiFiAdmin.fileServer.start();
                }
            } else {
                WiFiAdmin.fileServer = null;
                WiFiAdmin.isConnected = false;
                Log.e("wifi", "not connected");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    /**
     * 回调函数获取可用的wifi设备
     *
     * @param peers
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        devices.clear();
        devices.addAll(peers.getDeviceList());
    }

    public List<WifiP2pDevice> getDevices() {
        return devices;
    }
}
