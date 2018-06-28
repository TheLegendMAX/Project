package com.example.thelegeendmax.wordchooser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.locks.Condition;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Switch sw1 = (Switch) findViewById(R.id.switch1) ;
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);
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
/*
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }
*/
    private boolean t = true;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Switch sw1 = (Switch) findViewById(R.id.switch1) ;
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            if(intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                try{
                    if(t&&wifiInfo.getBSSID().equals("b8:27:eb:a9:c2:f0")){
                        t=false;
                        TextView connection = (TextView) findViewById(R.id.textView6);
                        connection.setText("Connected");
                        connection.setTextColor(Color.parseColor("#409038"));
                        unregisterReceiver(receiver);
                        Intent i = new Intent(context, MainActivity.class);
                        startActivity(i);
                        finish();  //Kill the activity from which you will go to next activity
                    }
                    else{
                        TextView connection = (TextView) findViewById(R.id.textView6);
                        connection.setText("Disconnected");
                        connection.setTextColor(Color.parseColor("#ff0000"));
                    }
                }
                catch(Exception e){

                }
            }
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);

                switch (wifiStateExtra) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        sw1.setChecked(true);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        sw1.setChecked(false);
                        break;
                }
                if(wifiManager.isWifiEnabled()){
                    sw1.setChecked(true);
                }
            }
        }
    };




/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
            if(wifiManager.isWifiEnabled()){
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if(wifiInfo.getBSSID().equals("b8:27:eb:a9:c2:f0")) {
                    TextView connection = (TextView) findViewById(R.id.textView6);
                    connection.setText("Connected");
                    connection.setTextColor(Color.parseColor("#409038"));
                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }

            }
        }
    }
    */
}
