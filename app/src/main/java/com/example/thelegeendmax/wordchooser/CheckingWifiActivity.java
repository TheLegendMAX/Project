package com.example.thelegeendmax.wordchooser;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CheckingWifiActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    private static final int ALL_PERMISSIONS = 101;
    private static final String permissions[] = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_wifi);
        requestPermissions();
        Switch wifiSwitch = (Switch) findViewById(R.id.switch1) ;
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
                if(isChecked) {
                    wifiManager.setWifiEnabled(true);
                }
                else
                    wifiManager.setWifiEnabled(false);
            }
        });
        Button WifiSettings = (Button) findViewById(R.id.button);
        WifiSettings.setOnClickListener(new View.OnClickListener() {
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
    @AfterPermissionGranted(ALL_PERMISSIONS)
    private void requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, "this application need the permission to read files and know the location", ALL_PERMISSIONS, permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            requestPermissions();
        }
    }

    BroadcastReceiver awaitIPAddress = null;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                if (intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) == SupplicantState.COMPLETED) {
                    //WiFi is associated
                    WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                    WifiInfo wi = wifiManager.getConnectionInfo();
                    if (wi != null) {
                        // Wifi info available (should be, we are associated)
                        if (wi.getIpAddress() != 0) {
                            // Lucky us, we already have an ip address.
                            // This happens when a connection is complete, e.g. after rekeying
                            if (unornamatedSsid(wi.getSSID()).equals("Print_Server")) {
                                TextView connection = (TextView) findViewById(R.id.textView6);
                                connection.setText("Connected");
                                connection.setTextColor(Color.parseColor("#409038"));
                                context.unregisterReceiver(this);
                                Intent i = new Intent(getApplicationContext(), PrintOrScan.class);
                                startActivity(i);
                                finish();  //Kill the activity from which you will go to next activity
                            }
                        } else {
                            // No ip address yet, we need to wait...
                            // Battery friendly method, using events
                            if (awaitIPAddress == null) {
                                awaitIPAddress = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context ctx, Intent in) {
                                        WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
                                        WifiInfo wi = wifiManager.getConnectionInfo();
                                        if (wi != null) {
                                            if (wi.getIpAddress() != 0) {
                                                if (unornamatedSsid(wi.getSSID()).equals("Print_Server")) {
                                                    TextView connection = (TextView) findViewById(R.id.textView6);
                                                    connection.setText("Connected");
                                                    connection.setTextColor(Color.parseColor("#409038"));
                                                    ctx.unregisterReceiver(this);
                                                    Intent i = new Intent(getApplicationContext(), PrintOrScan.class);
                                                    startActivity(i);
                                                    finish();  //Kill the activity from which you will go to next activity
                                                }
                                            }
                                        } else {
                                            ctx.unregisterReceiver(this);
                                            awaitIPAddress = null;
                                        }
                                    }
                                };
                                // We register a new receiver for connectivity events
                                // (getting a new IP address for example)
                                context.registerReceiver(awaitIPAddress, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                            }
                        }
                    }
                } else {
                    // wifi connection not complete, release ip address receiver if registered
                    if (awaitIPAddress != null) {
                        context.unregisterReceiver(awaitIPAddress);
                        awaitIPAddress = null;
                    }
                }
            }
            else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                Switch sw1 = (Switch) findViewById(R.id.switch1) ;
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

    private String unornamatedSsid(String ssid) {
        ssid = ssid.replaceFirst("^\"", "");
        return ssid.replaceFirst("\"$", "");
    }
}
