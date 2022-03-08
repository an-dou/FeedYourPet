package com.example.feedyourpet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    final String IP="192.168.4.1";
    final int Port=8080;

    Socket mSocket;
    ConnectThread mConnectThread;
    PrintStream out;

    Button buttonConnect;
    Button buttonOut;
    Button buttonAdd;
    TextView tViewConnect;

    Data data;
    List<Alarm> alarms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonConnect= findViewById(R.id.button0);
//        editIP= findViewById(R.id.edit_text0);;
//        editPort= findViewById(R.id.edit_text1);
        buttonOut= findViewById(R.id.button1);
        buttonAdd= findViewById(R.id.button2);
        tViewConnect=findViewById(R.id.text1);

//
        Data data=(Data) getApplicationContext();

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSocket == null || !mSocket.isConnected()) {
//                    String ip = editIP.getText().toString();
//                    int port = Integer.valueOf(editPort.getText().toString());
                    mConnectThread = new ConnectThread(IP,Port);
                    mConnectThread.start();
                }
                if (mSocket != null && mSocket.isConnected()) {
                    try {
                        mSocket.close();
                        mSocket=null; //清空mSocket
                        buttonConnect.setText("连接");
                        tViewConnect.setText("未连接");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        buttonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarms=data.getAlarms();
                if(alarms.size()<data.getMaxAlarmNum()){
                    //dialog
                    CustomDialog dialog = new CustomDialog(MainActivity.this);
                    dialog.show();
                }
                else {
                    Toast.makeText(MainActivity.this,"定时器已满！",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class ConnectThread extends Thread {
        private String ip;
        private int port;

        public ConnectThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                mSocket = new Socket(ip, port);
                out = new PrintStream(mSocket.getOutputStream());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonConnect.setText("断开");
                        tViewConnect.setText("已连接");
                    }
                });
//                new HeartBeatThread().start();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast mToast = Toast.makeText(MainActivity.this, "",Toast.LENGTH_LONG);
                        mToast.setText("连接失败，请检查WiFi连接");
                        mToast.show();
                    }
                });
            }
        }
    }

/*
    private class HeartBeatThread extends Thread{
        @Override
        public void run(){
            while (true){
                try {
                    Thread.sleep(3000);
                    if (!mSocket.isConnected()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonConnect.setText("连接");
                                Toast.makeText(MainActivity.this,"连接已关闭",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }*/


    


}