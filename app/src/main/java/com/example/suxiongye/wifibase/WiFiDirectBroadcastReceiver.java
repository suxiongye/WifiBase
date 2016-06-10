package com.example.suxiongye.wifibase;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

                //listAllPeers(peers);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (manager == null) return;
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Log.e("wifi", "connected");
            } else {
                Log.e("wifi", "disconnected");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    /**
     * list the devices in the log
     *
     * @param peers
     */
    private void listAllPeers(List<WifiP2pDevice> peers) {
        for (WifiP2pDevice device : peers) {
            Log.e("wifi", "device address : " + device.deviceAddress);
            Log.e("wifi", "device name :" + device.deviceName);
        }

    }

    /**
     * get the devices list
     */
    public List<WifiP2pDevice> getPeersList() {
        return peers;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList list) {
        peers.clear();
        peers.addAll(list.getDeviceList());
    }

    public static class SendFileTask extends AsyncTask {

        private Intent intent;
        private Context context;
        private WiFiDirectBroadcastReceiver receiver;

        public SendFileTask(Intent intent, Context context, WiFiDirectBroadcastReceiver receiver) {
            this.intent = intent;
            this.context = context;
            this.receiver = receiver;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            WifiP2pDevice device;
            File path1;
            String path = null;
            //find the sd card
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // 创建一个文件夹对象，赋值为外部存储器的目录
                File sdcardDir = Environment.getExternalStorageDirectory();
                //得到一个路径，内容是sdcard的文件夹路径和名字

                path = sdcardDir.getPath() + "/WifiBase";

                path1 = new File(path);

                if (!path1.exists()) {

                    path1.mkdirs();

                }

                path = path + "/123.txt";
                path1 = new File(path);
                if (!path1.exists()) {
                    try {
                        path1.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            List<WifiP2pDevice> list = receiver.getPeersList();
            Log.e("wifi", "size:" + list.size());
            if (list.size() > 0)

            {
                device = list.get(0);

                Utils.printConnect();
                String host = Utils.getP2pIP();
                if (host != null) {
                    Log.e("wifi", host);
                    Socket socket = new Socket();
                    int len = 0;
                    int port = 8888;
                    byte buf[] = new byte[1024];
                    Log.e("send", "start");
                    try {
                        socket.bind(null);
                        socket.connect((new InetSocketAddress(host, port)), 500);

                        OutputStream outputStream = socket.getOutputStream();
                        ContentResolver cr = context.getContentResolver();
                        InputStream inputStream = null;
                        if (new File(path).exists()){
                            Log.e("file", path+":exist");
                        }
                        inputStream = cr.openInputStream(Uri.parse("file://"+path));
                        while ((len = inputStream.read(buf)) != -1) {
                            outputStream.write(buf, 0, len);
                        }
                        outputStream.close();
                        inputStream.close();
                    } catch (Exception e) {
                        Log.e("wifi send", e.toString());
                    } finally {
                        if (socket != null) {
                            if (socket.isConnected()) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    Log.e("send", "finish");
                }
            }
            return null;
        }
    }

    public static class RecieveFileTask extends AsyncTask{


        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();
                final File f = new File(Environment.getExternalStorageDirectory()+"/WifiBase/recieve-"+System.currentTimeMillis()+".txt");
                File dirs = new File(f.getParent());
                if (!dirs.exists()) dirs.mkdirs();
                f.createNewFile();
                InputStream inputStream = client.getInputStream();
                copyFile(inputStream, new FileOutputStream(f));
                serverSocket.close();
            }
            catch (Exception e){
                e.printStackTrace();
                Log.e("receive",e.toString());
            }
            return null;
        }
        public static boolean copyFile(InputStream inputStream, OutputStream out) {
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);

                }
                out.close();
                inputStream.close();
            } catch (IOException e) {

                return false;
            }
            return true;
        }
    }



    public void sendFile(Intent intent, Context context) {
        new SendFileTask(intent, context, this).execute();
    }

    public void recieveFile(){
        new RecieveFileTask().execute();
    }
}
