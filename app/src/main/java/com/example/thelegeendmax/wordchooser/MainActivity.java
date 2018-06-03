package com.example.thelegeendmax.wordchooser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.*;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AllPermission;
import java.security.MessageDigest;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static int REQUEST_CODE = 43;
    private static final int ALL_PERMISSIONS = 101;
    private static final String permissions[] =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        TextView ssid=(TextView) findViewById(R.id.textView2);
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(this.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        ssid.setText(wifiInfo.getBSSID()+"  "+wifiInfo.getMacAddress());


    }
    @AfterPermissionGranted(ALL_PERMISSIONS)
    private void requestPermissions(){
        if(!EasyPermissions.hasPermissions(this,permissions)){
            EasyPermissions.requestPermissions(this,"this application need the permission to read files and know the location",ALL_PERMISSIONS,permissions);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    protected void onResume(){
        super.onResume();
        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSearch();
            }
        });
    }

    private void startSearch(){
        String[] mimeTypes =
                {"image/*","application/pdf","text/*","application/vnd.ms-powerpoint","application/vnd.ms-excel"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), REQUEST_CODE);
    }


        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                TextView HASH = (TextView) findViewById(R.id.hash);
                String hash="";
                try{
                    ContentResolver cr = this.getContentResolver();
                    InputStream inputStream = cr.openInputStream(uri);
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    byte[] bytesBuffer = new byte[1024];
                    int bytesRead = -1;

                    while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                        digest.update(bytesBuffer, 0, bytesRead);
                    }

                    byte[] hashedBytes = digest.digest();
                    hash= bytesToHex(hashedBytes);
                }
                catch(Exception e){

                }
                HASH.setText(hash);


            }
        }
        if(requestCode==AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            requestPermissions();
        }
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
