package com.example.feedyourpet;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Data extends Application {
    final private int maxAlarmNum=15;
    final private int maxAlarmNowNum=6;

    private static final String ALARM_SAVED = "alarm_saved";
    private static final String HOUR_SAVED = "hour_saved";
    private static final String MINUTE_SAVED = "minute_saved";
    private static final String WEIGHT_SAVED = "weight_saved";
    private static final String STATE_SAVED = "state_saved";

    private static final String ALARMNOW_SAVED = "alarm_now_saved";
    private static final String HOURNOW_SAVED = "hour_now_saved";
    private static final String MINUTENOW_SAVED = "minute_now_saved";
    private static final String WEIGHTNOW_SAVED = "weight_now_saved";

    private List<Alarm> alarms;
    private List<Alarm> alarmsNow;
    private int state_on_count=0;


    @Override
    public void onCreate() {
        super.onCreate();

        getSavedAlarm();
        getSavedAlarmNow();

        fillAlarms();
        fillAlarmsNow();

    }

    public int getState_on_count() {
        return state_on_count;
    }

    public void setState_on_count(boolean i) {
        if (i)
            state_on_count++;
        else
            state_on_count--;
    }

    public int getMaxAlarmNum() {
        return maxAlarmNum;
    }

    public int getMaxAlarmNowNum() {
        return maxAlarmNowNum;
    }

    public void addAlarmsNow(Alarm alarm){
        alarmsNow.add(alarm);
        saveAlarmNow();
    }

    public void clearAlarmsNow(){
        alarmsNow.clear();
        saveAlarmNow();
    }

    public void fillAlarmsNow(){
        while(alarmsNow.size()<maxAlarmNowNum){
            alarmsNow.add(new Alarm(0,0,0,false));
        }
    }

    public List<Alarm> getAlarmsNow(){
        return alarmsNow;
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
        setState_on_count(alarms.get(i).getState());
        saveAlarm();
    }
    public void changeAlarm(int i,Alarm alarm){
        alarms.remove(i);
        addAlarms(alarm);
    }

    public void fillAlarms(){
        if(alarms.size() < 2 || alarms.get(alarms.size() - 1).getWeight() != 0){
            alarms.add(new Alarm(23,58,0,false));
            alarms.add(new Alarm(23,59,0,false));
        }
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
                    setState_on_count(true);
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

    private void getSavedAlarmNow(){
        alarmsNow = new ArrayList<>();
        SharedPreferences sp = getApplicationContext().getSharedPreferences(ALARMNOW_SAVED, MODE_PRIVATE);
        String hourString = sp.getString(HOURNOW_SAVED, "");
        String[] hourArrays = hourString.split(",");
        if (hourArrays[0].equals("")) {
            return;
        }
        String minuteString = sp.getString(MINUTENOW_SAVED, "");
        String weightString = sp.getString(WEIGHTNOW_SAVED, "");
        String[] minuteArrays = minuteString.split(",");
        String[] weightArrays = weightString.split(",");
        for (int a = 0; a < hourArrays.length; a++) {
            alarmsNow.add(new Alarm(Integer.parseInt(hourArrays[a]),
                    Integer.parseInt(minuteArrays[a]),
                    Integer.parseInt(weightArrays[a]), true));
        }
    }

    private void saveAlarmNow(){
        SharedPreferences sp=getApplicationContext().getSharedPreferences(ALARMNOW_SAVED,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (alarmsNow.size() > 0) {
            StringBuilder sb0 = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            for (int a = 0; a < alarmsNow.size(); a++) {
                sb0.append(alarmsNow.get(a).getHour() + ",");
                sb1.append(alarmsNow.get(a).getMinute() + ",");
                sb2.append(alarmsNow.get(a).getWeight() + ",");
            }
            editor.putString(HOURNOW_SAVED, sb0.toString());
            editor.putString(MINUTENOW_SAVED, sb1.toString());
            editor.putString(WEIGHTNOW_SAVED, sb2.toString());
        } else {
            editor.clear();
        }
        editor.commit();
    }

    private void getSavedAlarm() {
        alarms = new ArrayList<>();
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
        for (int a = 0; a < hourArrays.length; a++) {
            alarms.add(new Alarm(Integer.parseInt(hourArrays[a]),
                    Integer.parseInt(minuteArrays[a]),
                    Integer.parseInt(weightArrays[a]),
                    Boolean.parseBoolean(stateArrays[a])));
            if(Boolean.parseBoolean(stateArrays[a])){
                setState_on_count(true);
                Log.d("TAG", "getSavedAlarm: "+getState_on_count());
            }
        }
    }

    private void saveAlarm(){
        SharedPreferences sp=getApplicationContext().getSharedPreferences(ALARM_SAVED,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
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
            editor.putString(HOUR_SAVED, sb0.toString());
            editor.putString(MINUTE_SAVED, sb1.toString());
            editor.putString(WEIGHT_SAVED, sb2.toString());
            editor.putString(STATE_SAVED, sb3.toString());
        } else {
            editor.clear();
        }
        editor.commit();
    }

}

