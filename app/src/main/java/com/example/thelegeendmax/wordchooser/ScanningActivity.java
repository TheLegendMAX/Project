package com.example.thelegeendmax.wordchooser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ScanningActivity extends AppCompatActivity {

    final WiFiReceiver wifiRec = new WiFiReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiRec.WifiReceiverBroadcastReceiver,filter);
        final Button save = (Button) findViewById(R.id.save);
        save.setVisibility(View.INVISIBLE);
        final EditText name = (EditText) findViewById(R.id.name);
        name.setVisibility(View.INVISIBLE);
        final Button scan = (Button) findViewById(R.id.scann);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = "pi";
                final String host = "192.168.22.1";
                final int port = 22;
                final Thread connectionThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            TextView status = (TextView) findViewById(R.id.status);
                            status.setText("Status: Checking the scanner");
                            JSch jsch = new JSch();
                            Session session = jsch.getSession(user, host, port);
                            InputStream privateKeyByteStream = getResources()
                                    .openRawResource(
                                            getResources().getIdentifier("id_rsa", "raw", getPackageName()));
                            byte[] prvkey= readFully(privateKeyByteStream);
                            jsch.addIdentity("Identity",prvkey,null,null);
                            session.setConfig("kex","diffie-hellman-group1-sha1");
                            session.setConfig("cipher.s2c","aes256-ctr");
                            session.setConfig("StrictHostKeyChecking", "no");
                            session.connect();
                            Channel channel = session.openChannel("shell");
                            OutputStream inputstream_for_the_channel = channel.getOutputStream();
                            PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            channel.setOutputStream(baos);
                            channel.connect();
                            commander.println("cd Desktop");
                            commander.println("scanimage --resolution 300 --mode Color --format tiff > document.tiff");
                            commander.println("echo $?");
                            Thread.sleep(30000);
                            String ret = new String(baos.toByteArray());
                            //commander.close();
                            String[] ab = ret.split("\r\n");
                            int a = -1;
                            try{
                                a = Integer.parseInt(ab[19]);

                            }
                            catch(Exception e){

                            }
                            try{
                                a = Integer.parseInt(ab[20]);
                            }
                            catch (Exception e){

                            }
                            if(a!=0){
                                status.setText("Status: Scanning failed, please make sure that the scanner is on and connected to the device!");
                                Thread.sleep(1500);
                                unregisterReceiver(wifiRec.WifiReceiverBroadcastReceiver);
                                Intent i = new Intent(ScanningActivity.this, CheckingWifiActivity.class);
                                startActivity(i);
                                ScanningActivity.this.finish();  //Kill the activity from which you will go to next activity
                            }
                            else if(a==0){
                                status.setText("Status: Waiting for the scanning to finish");
                                commander.println("convert document.tiff document-big.pdf");
                                commander.println("gs -q -dNOPAUSE -dBATCH -dSAFER        \\\n" +
                                        "   -sDEVICE=pdfwrite                   \\\n" +
                                        "   -dCompatibilityLevel=1.3            \\\n" +
                                        "   -dPDFSETTINGS=/screen               \\\n" +
                                        "   -dEmbedAllFonts=true                \\\n" +
                                        "   -dSubsetFonts=true                  \\\n" +
                                        "   -dColorImageDownsampleType=/Bicubic \\\n" +
                                        "   -dColorImageResolution=300          \\\n" +
                                        "   -dGrayImageDownsampleType=/Bicubic  \\\n" +
                                        "   -dGrayImageResolution=300           \\\n" +
                                        "   -dMonoImageDownsampleType=/Bicubic  \\\n" +
                                        "   -dMonoImageResolution=300           \\\n" +
                                        "   -sOutputFile=document.pdf           \\\n" +
                                        "   document-big.pdf");
                                Thread.sleep(30000);
                                status.setText("Status: Scanning done!, Enter a name and press save to save it.");
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        scan.setVisibility(View.INVISIBLE);
                                        save.setVisibility(View.VISIBLE);
                                        name.setVisibility(View.VISIBLE);

                                    }
                                });
                                commander.close();
                            }
                            channel.disconnect();
                            session.disconnect();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                });
                connectionThread.start();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().equals("")){
                    ScanningActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ScanningActivity.this, "You must enter a name of the file",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    final String user = "pi";
                    final String host = "192.168.22.1";
                    final int port = 22;
                    final Thread connectionThread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                TextView status = (TextView) findViewById(R.id.status);
                                JSch jsch = new JSch();
                                Session session = jsch.getSession(user, host, port);
                                InputStream privateKeyByteStream = getResources()
                                        .openRawResource(
                                                getResources().getIdentifier("id_rsa", "raw", getPackageName()));
                                byte[] prvkey= readFully(privateKeyByteStream);
                                jsch.addIdentity("Identity",prvkey,null,null);
                                session.setConfig("kex","diffie-hellman-group1-sha1");
                                session.setConfig("cipher.s2c","aes256-ctr");
                                session.setConfig("StrictHostKeyChecking", "no");
                                session.connect();
                                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                                sftpChannel.connect();
                                InputStream is = sftpChannel.get("/home/pi/Desktop/document.pdf");
                                createExternalStoragePrivateFile(name.getText().toString(),"pdf",is);
                                status.setText("File Saved!");
                                sftpChannel.disconnect();
                                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                                ((ChannelExec) channel).setCommand("cd Desktop;rm document-big.pdf;rm document.pdf;rm document.tiff");
                                channel.connect();
                                channel.disconnect();
                                session.disconnect();
                                Thread.sleep(1000);
                                unregisterReceiver(wifiRec.WifiReceiverBroadcastReceiver);
                                Intent i = new Intent(ScanningActivity.this, PrintOrScan.class);
                                startActivity(i);
                                ScanningActivity.this.finish();  //Kill the activity from which you will go to next activity
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    });
                    connectionThread.start();
                }
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

    public void createExternalStoragePrivateFile
            (String name, String extension, InputStream is) {
        // Create a path where we will place our private file on external
        // storage.
        File file = new File
                (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name +"."+extension);

        try {
            OutputStream os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        }


        public static byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

}
