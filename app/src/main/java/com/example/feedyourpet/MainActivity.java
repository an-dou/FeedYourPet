package com.example.feedyourpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingDeque;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SSID = "esp8266";
    private static final String PASSWORD = "12345678";
    private static final int MESSAGE_RECEICE = 0;
//    private HeartBeatThread heartBeatThread;

    private Button buttonConnect;
    private Button buttonOut;
    private Button buttonAdd;
    private TextView tViewConnect;
//    private TextView tViewNowListNull;
    private RecyclerView recyclerView;
    private SlideRecyclerView slideRecyclerView;

    private Data data;
    private List<Alarm> alarms;
    private AlarmAdapter alarmAdapter;

    //??????ESP8266???IP????????????
    private final String IP="192.168.4.1";
    private final int Port=8080;
    private WifiManager wifiManager;
    private WifiInfo connectionInfo;

    private Socket mSocket;
    private PrintStream out;
    private BufferedReader br;
    private String receiceString;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_RECEICE:
                    tViewConnect.setText(receiceString);
                    Log.d(TAG, "handleMessage: "+receiceString);
                    makeToastLong(receiceString);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        buttonConnect= findViewById(R.id.button0);
        buttonOut= findViewById(R.id.button1);
        buttonAdd= findViewById(R.id.button2);
        tViewConnect=findViewById(R.id.title_layout_connect);
        recyclerView=findViewById(R.id.recyclerView);
        slideRecyclerView=findViewById(R.id.slideRecyclerView);

        data=(Data) getApplicationContext();
        alarms=data.getAlarms();

        //?????????????????????
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);//?????????????????????
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(RecyclerView.VERTICAL);//?????????????????????
        recyclerView.setLayoutManager(linearLayoutManager1);
        slideRecyclerView.setLayoutManager(linearLayoutManager);

        //??????????????????
        recyclerView.setAdapter(new AlarmNowAdapter(getApplicationContext()));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_inset));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());// ?????? item ???????????????????????????
        //????????????????????????
        slideRecyclerView.addItemDecoration(itemDecoration);
        alarmAdapter=new AlarmAdapter(this);
        slideRecyclerView.setAdapter(alarmAdapter);
        slideRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

//        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new CallBack());
//        itemTouchHelper.attachToRecyclerView(recyclerView);


        //app???esp8266??????
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSocket == null || !mSocket.isConnected()) {
                    new ConnectThread(IP,Port).start();
                }
                if (mSocket != null && mSocket.isConnected()) {
                    try {
                        mSocket.close();
                        mSocket=null; //??????mSocket
                        buttonConnect.setText("??????");
                        tViewConnect.setText("?????????");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //???????????????????????????
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                alarms=data.getAlarms();
                if(alarms.size()<data.getMaxAlarmNum()){
                    //dialog
                    CustomDialog dialog = new CustomDialog(MainActivity.this);
                    dialog.show();
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
//                            alarms=data.getAlarms();
//                            alarmAdapter=new AlarmAdapter(getApplicationContext());
//                            slideRecyclerView.setAdapter(alarmAdapter);
                            alarmAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    makeToastLong("??????????????????");
                }
            }
        });
        //app????????????????????????????????????
        buttonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager == null && !wifiManager.isWifiEnabled()){
                    makeToastShort("?????????WLAN");
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS),1);
                }
                else{
                    new SendAlarms().start();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                new SendAlarms().start();
                break;
        }
    }

    private class SendAlarms extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                if(mSocket != null && mSocket.isConnected()){
                    if(data.getState_on_count()>data.getMaxAlarmNowNum()){
                        uiThreadToastLong("????????????"+data.getMaxAlarmNowNum()+"???????????????");
                    }
                    else if(data.getState_on_count()==0 || alarms.size()==0){
                        outData("A:");
                        uiThreadToastLong("???????????????????????????");
                        data.clearAlarmsNow();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                data.fillAlarmsNow();
                                recyclerView.setAdapter(new AlarmNowAdapter(getApplicationContext()));
                            }
                        });
                    }
                    else if(out!=null&&alarms.size()>0){
                        String str;
                        for (int a = 0; a < alarms.size(); a++) {
                            if(a==0){
                                outData("A:");
                                data.clearAlarmsNow();
                            }
                            if(alarms.get(a).getState()){
                                data.addAlarmsNow(alarms.get(a));
                                str=alarms.get(a).getHour()*60+alarms.get(a).getMinute()
                                        + ":" + alarms.get(a).getWeight() + ":";
                                outData(str);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: "+data.getAlarmsNow().size());
                                data.fillAlarmsNow();
                                recyclerView.setAdapter(new AlarmNowAdapter(getApplicationContext()));
                                makeToastLong("?????????");
                            }
                        });
                    }
                }
                else{
                    uiThreadToastLong("???????????????");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void wifiConnect() {
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            //Android 10+(SDK 29)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                NetworkSpecifier specifier =
                        new WifiNetworkSpecifier.Builder()
                                .setSsidPattern(new PatternMatcher(SSID, PatternMatcher.PATTERN_PREFIX))
                                .setWpa2Passphrase(PASSWORD)
                                .build();

                NetworkRequest request =
                        new NetworkRequest.Builder()
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                .setNetworkSpecifier(specifier)
                                .build();

                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        // do success processing here..
                    }

                    @Override
                    public void onUnavailable() {
                        // do failure processing here..
                        makeToastLong("??????????????????????????????WLAN");
                    }
                };
                connectivityManager.requestNetwork(request, networkCallback);
                // Release the request when done.
                // connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        }



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
                        buttonConnect.setText("??????");
                        tViewConnect.setText("?????????");
                    }
                });
//                new HeartBeatThread().start();

                //app???????????????????????????
                new GetTCPstring().start();

                //app????????????????????????????????????????????????
                Calendar cal=Calendar.getInstance();
                outData(cal.get(Calendar.SECOND)+":");
                outData(cal.get(Calendar.MINUTE)+":");
                outData((Calendar.AM==cal.get(Calendar.AM_PM)?cal.get(Calendar.HOUR):cal.get(Calendar.HOUR)+12)+":");
                outData(cal.get(Calendar.DATE)+":");
                outData((cal.get(Calendar.MONTH)+1)+":");
                outData((cal.get(Calendar.DAY_OF_WEEK)-1)+":");
                outData(cal.get(Calendar.YEAR)+":");


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                uiThreadToastLong("????????????????????????WiFi??????");
            }
        }
    }
    private void outData(String str) throws InterruptedException {
        if(out!=null){
            out.print(str);
            out.flush();
            Log.d(TAG, "outData: " +str);
            Thread.sleep(500);
        }
    }

    private class GetTCPstring extends Thread{

        @Override
        public void run(){
            while (true){
                try{
                    char[] receice = new char[4];
                    br=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (true){
                        if(br.ready()){
                            br.read(receice);
                            receiceString=String.valueOf(receice,0,4);
//                            handler.sendMessage(handler.obtainMessage());
                            Message message=handler.obtainMessage();
                            message.what=MESSAGE_RECEICE;
                            handler.sendMessage(message);
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                    makeToastLong("??????????????????");
                }
            }
        }
    }

    private class HeartBeatThread extends Thread{
        public volatile boolean exit = false;
        @Override
        public void run(){
            while (!exit){
                try {
                    Thread.sleep(3000);
                    if (!mSocket.isConnected()){
                        exit=true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonConnect.setText("??????");
                                makeToastLong("???????????????");
                            }
                        });
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    void makeToastLong(String str){
        Toast mToast = Toast.makeText(MainActivity.this, "",Toast.LENGTH_LONG);
        mToast.setText(str);
        mToast.show();
    }
    void makeToastShort(String str){
        Toast mToast = Toast.makeText(MainActivity.this, "",Toast.LENGTH_SHORT);
        mToast.setText(str);
        mToast.show();
    }
    void uiThreadToastLong(String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeToastLong(str);
            }
        });
    }
}