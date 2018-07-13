package com.example.thelegeendmax.wordchooser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main4Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        Button Print = (Button) findViewById(R.id.Print);
        Button Scann = (Button) findViewById(R.id.Scann);
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);
        Print.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main4Activity.this, MainActivity.class);
                startActivity(i);
                finish();  //Kill the activity from which you will go to next activity
                unregisterReceiver(receiver);
            }
        });
        Scann.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main4Activity.this, Main5Activity.class);
                startActivity(i);
                finish();  //Kill the activity from which you will go to next activity
                unregisterReceiver(receiver);
            }
        });
    }

    private String unornamatedSsid(String ssid) {
        ssid = ssid.replaceFirst("^\"", "");
        return ssid.replaceFirst("\"$", "");
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            if(wifiManager.isWifiEnabled()){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                try{
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if(!(unornamatedSsid(wifiInfo.getSSID()).equals("Print_Server"))||!(networkInfo.isConnected()&&networkInfo.getType()==ConnectivityManager.TYPE_WIFI)) {
                        unregisterReceiver(receiver);
                        Intent i = new Intent(Main4Activity.this, Main2Activity.class);
                        startActivity(i);
                        finish();  //Kill the activity from which you will go to next activity
                    }
                }
                catch(Exception e){
                    //finish();
                }
            }
            else{
                Intent i = new Intent(Main4Activity.this, Main2Activity.class);
                startActivity(i);
                finish();  //Kill the activity from which you will go to next activity
                unregisterReceiver(receiver);
            }
        }
    };
}
