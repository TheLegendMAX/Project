package com.example.thelegeendmax.wordchooser;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AllPermission;
import java.security.MessageDigest;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_CODE = 43;
    private static final int ALL_PERMISSIONS = 101;
    private static final String permissions[] = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private String aaaa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
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

    protected void onResume() {
        super.onResume();
        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSearch();
            }
        });
    }

    private void startSearch() {

        String[] mimeTypes =
                {"image/*","application/pdf","text/*","application/vnd.ms-powerpoint","application/vnd.ms-excel"};

        //String[] mimeTypes = {"application/pdf"};

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
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));

        }
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 15 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                TextView HASH = (TextView) findViewById(R.id.hash);
                String hash = "";
                try {
                    ContentResolver cr = this.getContentResolver();
                    InputStream inputStream = cr.openInputStream(uri);
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    byte[] bytesBuffer = new byte[1024];
                    int bytesRead = -1;

                    while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                        digest.update(bytesBuffer, 0, bytesRead);
                    }

                    byte[] hashedBytes = digest.digest();
                    hash = bytesToHex(hashedBytes);
                } catch (Exception e) {

                }
                HASH.setText(hash);
            }
        }
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            requestPermissions();
        }
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            Cursor returnCursor =
                    getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            final String name = returnCursor.getString(nameIndex);
            final String user = "pi";
            final String password = "hgyghj123";
            final String host = "192.168.22.1";
            final int port = 22;
            Thread connectionThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        JSch jsch = new JSch();
                        Session session = jsch.getSession(user, host, port);
                        session.setPassword(password);
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.connect();
                        ContentResolver cr = getApplicationContext().getContentResolver();
                        //String type = cr.getType(uri);
                        InputStream inputStream = cr.openInputStream(uri);
                        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                        sftpChannel.connect();
                        sftpChannel.put(inputStream, ("/home/pi/Desktop/"+name));
                        sftpChannel.disconnect();
                        Channel channel = session.openChannel("exec");
                        String command = "cd Desktop;lpr "+name+";rm "+name;
                        ((ChannelExec) channel).setCommand(command);
                        channel.connect();
                        channel.disconnect();
                        session.disconnect();

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            connectionThread.start();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
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
}