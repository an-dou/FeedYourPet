package com.example.feedyourpet;

public class Alarm {
    int hour;
    int minute;
    int weight; //g
    boolean state; //0-关  1-开

    Alarm(int h,int m,int w,boolean s){
        hour=h;
        minute=m;
        weight=w;
        state=s;
    }


}

