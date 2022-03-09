package com.example.feedyourpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    final String IP="192.168.4.1";
    final int Port=8080;

    Socket mSocket;
    ConnectThread mConnectThread;
    PrintStream out;

    Button buttonConnect;
    Button buttonOut;
    Button buttonAdd;
    TextView tViewConnect;
    SlideRecyclerView recyclerView;
//    RecyclerView recyclerView;

    Data data;
    List<Alarm> alarms;
    AlarmAdapter alarmAdapter;

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


        Data data=(Data) getApplicationContext();
        alarms=data.getAlarms();

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

        alarmAdapter=new AlarmAdapter(this,alarms);
//        slideRecyclerView.setAdapter(alarmAdapter);

        recyclerView.setAdapter(alarmAdapter);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

//        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new CallBack());
//        itemTouchHelper.attachToRecyclerView(recyclerView);

        if(alarms.size()>0)
            Toast.makeText(MainActivity.this,alarms.get(0).getHour()+"",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(MainActivity.this,"aaaa",Toast.LENGTH_LONG).show();


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
//                alarms=data.getAlarms();
                if(alarms.size()<10){
                    //dialog
                    CustomDialog dialog = new CustomDialog(MainActivity.this);
                    dialog.show();
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: "+alarms.get(alarms.size()-1).getHour()+ "时"
                +alarms.get(alarms.size()-1).getMinute()+ "分");


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