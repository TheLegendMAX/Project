package com.example.thelegeendmax.wordchooser;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Bundle extras = getIntent().getExtras();
        uri = Uri.parse(extras.getString("KEY"));
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        final String name = returnCursor.getString(nameIndex);
        TextView filename = (TextView) findViewById(R.id.FileName);
        filename.setText("File Name: "+name);
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver,filter);
        Button print = (Button) findViewById(R.id.print);
        print.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                final String user = "pi";
                final String host = "192.168.22.1";
                final int port = 22;
                final Thread connectionThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            JSch jsch = new JSch();
                            Session session = jsch.getSession(user, host, port);
                            InputStream privateKeyByteStream = getResources()
                                    .openRawResource(
                                            getResources().getIdentifier("id_rsa", "raw", getPackageName()));
                            byte[] prvkey= readFully(privateKeyByteStream);
                            jsch.addIdentity("Identity",prvkey,null,null);
                            session.setConfig("StrictHostKeyChecking", "no");
                            session.connect();
                            ContentResolver cr = getApplicationContext().getContentResolver();
                            InputStream inputStream = cr.openInputStream(uri);
                            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                            /*
                            Main3Activity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(Main3Activity.this, "sending "+name+" to the print server",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            */
                            TextView status = (TextView) findViewById(R.id.status);
                            status.setText("Status: Transfering "+name+" to print server ...");
                            sftpChannel.connect();
                            sftpChannel.put(inputStream, ("/home/pi/Desktop/"+name));
                            sftpChannel.disconnect();
                            status.setText("Status: Transfer is completed");
                            Channel channel = session.openChannel("shell");
                            OutputStream inputstream_for_the_channel = channel.getOutputStream();
                            PrintStream commander = new PrintStream(inputstream_for_the_channel, true);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            channel.setOutputStream(baos);
                            channel.connect();
                            commander.println("cd Desktop");
                            commander.println("lp "+name);
                            commander.println("echo $?");
                            Thread.sleep(1000);
                            String ret = new String(baos.toByteArray());
                            commander.println("rm "+name);
                            commander.close();
                            String[] ab = ret.split("\r\n");
                            if(Integer.parseInt(ab[12])!=0)
                                status.setText("Status: Printing failed, please make sure that the printer is on and connected to the device!");
                            else{
                                status.setText("Status: Printing ..");
                                Thread.sleep(1500);
                                unregisterReceiver(receiver);
                                Intent i = new Intent(Main3Activity.this, MainActivity.class);
                                startActivity(i);
                                Main3Activity.this.finish();  //Kill the activity from which you will go to next activity
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

    }
    private String unornamatedSsid(String ssid) {
        ssid = ssid.replaceFirst("^\"", "");
        return ssid.replaceFirst("\"$", "");
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
    private Uri uri;
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
                        Intent i = new Intent(Main3Activity.this, Main2Activity.class);
                        startActivity(i);
                        finish();  //Kill the activity from which you will go to next activity
                    }
                }
                catch(Exception e){
                    //finish();
                }
            }
            else{
                Intent i = new Intent(Main3Activity.this, Main2Activity.class);
                startActivity(i);
                finish();  //Kill the activity from which you will go to next activity
                unregisterReceiver(receiver);
            }
        }
    };
}
