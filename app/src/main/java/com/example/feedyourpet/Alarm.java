package com.example.feedyourpet;

public class Alarm {
    private int hour;
    private int minute;
    private int weight; //g
    private boolean state; //0-关  1-开

    Alarm(int h,int m,int w,boolean s){
        hour=h;
        minute=m;
        weight=w;
        state=s;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}

