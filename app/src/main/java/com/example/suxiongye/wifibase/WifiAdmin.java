package com.example.suxiongye.wifibase;


import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Use to control the wifi device
 * Created by suxiongye on 6/7/16.
 */
public class WifiAdmin {

    private WifiManager wifiManager;

    public WifiAdmin(Context context){
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * open the wifi if the wifi off
     */
    public void openWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * close the wifi if the wifi on
     */
    public void closeWifi(){
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
    }
}
