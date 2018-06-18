package com.example.thelegeendmax.wordchooser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Switch sw1 = (Switch) findViewById(R.id.switch1) ;
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            sw1.setChecked(true);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo.getBSSID().equals("b8:27:eb:1c:0a:69")) {
                TextView connection = (TextView) findViewById(R.id.textView6);
                connection.setText("Connected");
                connection.setTextColor(Color.parseColor("#409038"));
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        }
        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
                if(isChecked) {
                    wifiManager.setWifiEnabled(true);
                }
                else
                    wifiManager.setWifiEnabled(false);
            }
        });
        Button b1 = (Button) findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
                if(wifiManager.isWifiEnabled()){
                    Intent wifiSettings = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    startActivityForResult(wifiSettings,0);
                }
                else{
                    Toast.makeText(getApplicationContext(), "You must turn on the Wi-Fi",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    protected void onResume() {
        super.onResume();
        Switch sw1 = (Switch) findViewById(R.id.switch1) ;
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()) {
            sw1.setChecked(true);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getBSSID().equals("b8:27:eb:1c:0a:69")) {
                TextView connection = (TextView) findViewById(R.id.textView6);
                connection.setText("Connected");
                connection.setTextColor(Color.parseColor("#409038"));
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
            if(wifiManager.isWifiEnabled()){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if(wifiInfo.getBSSID().equals("b8:27:eb:1c:0a:69")) {
                    TextView connection = (TextView) findViewById(R.id.textView6);
                    connection.setText("Connected");
                    connection.setTextColor(Color.parseColor("#409038"));
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }

            }
        }
    }
}
