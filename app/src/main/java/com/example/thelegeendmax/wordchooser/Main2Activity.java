package com.example.thelegeendmax.wordchooser;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Switch sw1 = (Switch) findViewById(R.id.switch1) ;
        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
                if(isChecked) {
                    wifiManager.setWifiEnabled(true);
                    startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
                else
                    wifiManager.setWifiEnabled(false);
            }
        });
    }
}
