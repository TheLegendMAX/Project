package com.example.thelegeendmax.wordchooser;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WiFiReceiver {
    public BroadcastReceiver WifiReceiverBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            if(wifiManager.isWifiEnabled()){ //check the current wifi network
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                try{
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if(!(unornamatedSsid(wifiInfo.getSSID()).equals("Print_Server"))||!(networkInfo.isConnected()&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI)) {
                        Intent i = new Intent();
                        i.setClassName(context.getPackageName(), CheckingWifiActivity.class.getName());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.unregisterReceiver(WifiReceiverBroadcastReceiver);
                        ((Activity) context).finish();
                        context.startActivity(i);
                    }
                }
                catch(Exception e){
                    //finish();
                }
            }
            else{ //if the wifi is disabled
                Intent i = new Intent();
                i.setClassName(context.getPackageName(), CheckingWifiActivity.class.getName());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.unregisterReceiver(WifiReceiverBroadcastReceiver);
                ((Activity) context).finish();
                context.startActivity(i);
            }
        }
    };;

    /*
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            if(wifiManager.isWifiEnabled()){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                try{
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if(!(unornamatedSsid(wifiInfo.getSSID()).equals("Print_Server"))||!(networkInfo.isConnected()&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI)) {
                        Intent i = new Intent();
                        String pakagename = context.getPackageName();
                        i.setClassName(pakagename, pakagename+"CheckingWifiActivity");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                }
                catch(Exception e){
                    //finish();
                }
            }
            else{
                Intent i = new Intent();
                String pakagename = context.getPackageName();
                i.setClassName(pakagename, pakagename+"CheckingWifiActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
*/
    private String unornamatedSsid(String ssid) {
        ssid = ssid.replaceFirst("^\"", "");
        return ssid.replaceFirst("\"$", "");
    }
}
