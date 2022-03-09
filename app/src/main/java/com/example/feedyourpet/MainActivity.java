package com.example.feedyourpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //连接ESP8266的IP和端口号
    private final String IP="192.168.4.1";
    private final int Port=8080;

    private Socket mSocket;
    private ConnectThread mConnectThread;
    private PrintStream out;
//    private DataInputStream in;
    private BufferedReader br;
    private Handler handler;
    private char[] receice;
    private String receiceString;

    private Button buttonConnect;
    private Button buttonOut;
    private Button buttonAdd;
    private TextView tViewConnect;
    private SlideRecyclerView recyclerView;

    private Data data;
    private List<Alarm> alarms;
    private AlarmAdapter alarmAdapter;

    class CallBack extends ItemTouchHelper.Callback{
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0,ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            /**
             * call max distance start onSwiped call
             */
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


            if (actionState==ItemTouchHelper.ACTION_STATE_SWIPE){
                /**
                 * get {@link TextView#getWidth()}
                 */
                ViewGroup viewGroup= (ViewGroup) viewHolder.itemView;
                TextView textView = (TextView) viewGroup.getChildAt(1);
                ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
                if (Math.abs(dX)<=layoutParams.width){
                    /**
                     * move {@link RecyclerView.ViewHolder} distance
                     */
                    viewHolder.itemView.scrollTo((int) -dX,0);
                    /**
                     * callAction or register click bind view
                     */
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
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
        recyclerView=findViewById(R.id.recyclerView);


        data=(Data) getApplicationContext();
        alarms=data.getAlarms();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
//                        tv_content.setText("WiFi模块发送的：" + msg.obj);
                        Log.d(TAG, "handleMessage: "+msg.obj);
//                        Toast.makeText(MainActivity.this, "接收到信息: "+msg.obj, Toast.LENGTH_LONG).show();
                }
            }
        };

//        alarms=new ArrayList<>();
        /*if(alarms.size()<data.getMaxAlarmNum()) {
            Alarm alarm = new Alarm(11, 30, 300, true);
            Alarm alarm1 = new Alarm(12, 40, 50, false);
            Alarm alarm2 = new Alarm(13, 10, 100, true);
            alarms.add(alarm);
            alarms.add(alarm1);
            alarms.add(alarm2);
        }*/

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);//设置布局管理器
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);//设置为垂直布局，这也是默认的
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_inset));
        recyclerView.addItemDecoration(itemDecoration);

        // 设置 item 增加和删除时的动画
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

        alarmAdapter=new AlarmAdapter(this);
//        slideRecyclerView.setAdapter(alarmAdapter);

        recyclerView.setAdapter(alarmAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

//        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new CallBack());
//        itemTouchHelper.attachToRecyclerView(recyclerView);

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
                if(out!=null&&alarms.size()>0){
                    StringBuilder sb0 = new StringBuilder();
                    StringBuilder sb1 = new StringBuilder();
                    int counter=0;
                    for (int a = 0; a < alarms.size(); a++) {
                        if(alarms.get(a).getState()){
                            sb0.append(alarms.get(a).getHour()*60+alarms.get(a).getMinute() + ",");
                            sb1.append(alarms.get(a).getWeight() + ",");
                            counter++;
                        }
                    }
                    // Android 4.0 之后不能在主线程中请求HTTP请求
                    int finalCounter = counter;
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            if(out!=null){
                                if(finalCounter >0) {
                                    out.print(sb0.toString());
                                    out.print(sb1.toString());
                                    out.flush();
                                }
                                else {
                                    out.print(-1);
                                    out.flush();
                                }
                            }
                        }
                    }).start();
                }
                else if(alarms.size()==0){
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            out.print(-1);
                            out.flush();
                        }
                    }).start();

                }
            }
        });
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
                            alarmAdapter.notifyDataSetChanged();
                        }
                    });
                }
                /*if(alarms.size()<data.getMaxAlarmNum()){
                    //dialog
                    CustomDialog dialog = new CustomDialog(MainActivity.this);
                    dialog.show();
                }*/
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
                //new HeartBeatThread().start();

                //app发送数据：同步手机和单片机的时间
                Calendar cal=Calendar.getInstance();
                String time=cal.get(Calendar.YEAR)+","+(cal.get(Calendar.MONTH)+1)+","
                        +cal.get(Calendar.DATE)+","+(cal.get(Calendar.HOUR)+12)+","
                        +cal.get(Calendar.MINUTE)+","+cal.get(Calendar.SECOND)+",";
                out.print(time);
                out.flush();
                Log.d(TAG, "run: " +time);

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

    private void GetTCPstring(){
        new Thread(){
            public void run(){
                try{
                    receice=new char[10];
                    br=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (true){
                        if(br.ready()){
                            br.read(receice,0,10);
//                            receiceString=String.valueOf(receice);


                            Message message = new Message();
                            message.what = 1;
                            message.obj = new String(receice);
                            handler.sendMessage(message);
//                            handler.sendMessage(handler.obtainMessage());
                        }
                    }


                    //app接受数据：
                   /* while (true) {
                        Socket mSocket = server.accept();

                        receice = new byte[50];
                        in.read(receice);
                        in.close();

                    }*/


                }catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"接受数据失败",Toast.LENGTH_LONG).show();

                }
            }
        }.start();
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