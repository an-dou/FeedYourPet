package com.example.feedyourpet;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Data extends Application {
    final private int maxAlarmNum=10;

    public static final String ALARM_SAVED = "alarm_saved";
    public static final String HOUR_SAVED = "hour_saved";
    public static final String MINUTE_SAVED = "minute_saved";
    public static final String WEIGHT_SAVED = "weight_saved";
    public static final String STATE_SAVED = "state_saved";

    private List<Alarm> alarms=new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        getSavedAlarm();


    }

    public int getMaxAlarmNum() {
        return maxAlarmNum;
    }

    public void addAlarms(Alarm alarm){
        alarms.add(alarm);

        AlarmSort();
        saveAlarm();
    }

    public List<Alarm> getAlarms(){
        return alarms;
    }

    public void removeAlarm(int i){
        alarms.remove(i);
        saveAlarm();
    }

    public void changeAlarmState(int i){
        alarms.get(i).setState(!alarms.get(i).getState());
        saveAlarm();
    }
    public void changeAlarm(int i,Alarm alarm){
        alarms.remove(i);
        addAlarms(alarm);
    }

    private void AlarmSort(){
        int a=alarms.size()-1;
        int temp=a;
        for(int b=0;b<alarms.size()-1;b++){
            if((alarms.get(b).getHour()*60+alarms.get(b).getMinute())
                    > (alarms.get(a).getHour()*60+alarms.get(a).getMinute())){
                temp=b;
                break;
            }
            else if((alarms.get(b).getHour()*60+alarms.get(b).getMinute())
                    == (alarms.get(a).getHour()*60+alarms.get(a).getMinute())){
                if((alarms.get(b).getWeight()) == (alarms.get(a).getWeight())) {
                    alarms.remove(a);
                    alarms.get(b).setState(true);
                    break;
                }
                else if((alarms.get(b).getWeight()) > (alarms.get(a).getWeight())) {
                    temp = b;
                    break;
                }
            }
        }
        if(temp!=a){
            alarms.add(temp,alarms.get(a));
            alarms.remove(alarms.size()-1);
        }
    }
    private void getSavedAlarm() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences(ALARM_SAVED, MODE_PRIVATE);
        String hourString = sp.getString(HOUR_SAVED, "");
        String[] hourArrays = hourString.split(",");
        if (hourArrays[0].equals("")) {
            return;
        }
        String minuteString = sp.getString(MINUTE_SAVED, "");
        String weightString = sp.getString(WEIGHT_SAVED, "");
        String stateString = sp.getString(STATE_SAVED, "");
        String[] minuteArrays = minuteString.split(",");
        String[] weightArrays = weightString.split(",");
        String[] stateArrays = stateString.split(",");
        alarms = new ArrayList<Alarm>();
        for (int a = 0; a < hourArrays.length; a++) {
            alarms.add(new Alarm(Integer.parseInt(hourArrays[a]),
                    Integer.parseInt(minuteArrays[a]),
                    Integer.parseInt(weightArrays[a]),
                    Boolean.parseBoolean(stateArrays[a])));
        }

    }

    private void saveAlarm(){
        SharedPreferences sp=getApplicationContext().getSharedPreferences(ALARM_SAVED,MODE_PRIVATE);
        if (alarms.size() > 0) {
            StringBuilder sb0 = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            StringBuilder sb3 = new StringBuilder();
            for (int a = 0; a < alarms.size(); a++) {
                sb0.append(alarms.get(a).getHour() + ",");
                sb1.append(alarms.get(a).getMinute() + ",");
                sb2.append(alarms.get(a).getWeight() + ",");
                sb3.append(alarms.get(a).getState() + ",");
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(HOUR_SAVED, sb0.toString());
            editor.putString(MINUTE_SAVED, sb1.toString());
            editor.putString(WEIGHT_SAVED, sb2.toString());
            editor.putString(STATE_SAVED, sb3.toString());
            editor.commit();
        } else {
            return;
        }
    }

}

