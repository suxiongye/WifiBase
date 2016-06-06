package com.example.suxiongye.wifibase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by suxiongye on 6/6/16.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements PeerListListener {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    private List<WifiP2pDevice> peers;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        peers = new ArrayList<WifiP2pDevice>();
    }

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

                listAllPeers(peers);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    /**
     * list the devices in the log
     * @param peers
     */
    private void listAllPeers(List<WifiP2pDevice> peers) {
        for (WifiP2pDevice device : peers){
            Log.e("wifi","device address : "+device.deviceAddress);
            Log.e("wifi","device name :"+device.deviceName);
        }

    }

    /**
     * get the devices list
     */
    public List<WifiP2pDevice> getPeersList(){
        return peers;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList list) {
        peers.addAll(list.getDeviceList());
    }
}
