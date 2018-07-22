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

public class PrintOrScan extends AppCompatActivity {


    final WiFiReceiver wifiRec = new WiFiReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_or_scan);
        Button Print = (Button) findViewById(R.id.Print);
        Button Scan = (Button) findViewById(R.id.Scann);
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiRec.WifiReceiverBroadcastReceiver,filter);
        Print.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                unregisterReceiver(wifiRec.WifiReceiverBroadcastReceiver);
                Intent i = new Intent(PrintOrScan.this, ChooseFileActivity.class);
                startActivity(i);
            }
        });
        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(wifiRec.WifiReceiverBroadcastReceiver);
                Intent i = new Intent(PrintOrScan.this, ScanningActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiRec.WifiReceiverBroadcastReceiver,filter);
    }
}
