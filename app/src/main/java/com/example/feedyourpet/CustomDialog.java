package com.example.feedyourpet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class CustomDialog extends Dialog implements View.OnClickListener {
    Context mContext;

    PickerView pickerHour;
    PickerView pickerMinute;
    PickerView pickerWeight;

    Button butCancel;
    Button butFinish;

    Data data;

    public CustomDialog(Context context) {
        super(context, R.style.dialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_layout, null);
        this.setContentView(view);

        data=(Data) getContext().getApplicationContext();

        butCancel=findViewById(R.id.button_cancel);
        butFinish=findViewById(R.id.button_finish);
        butCancel.setOnClickListener(this);
        butFinish.setOnClickListener(this);

        pickerHour = findViewById(R.id.pickerView0);
        pickerMinute = findViewById(R.id.pickerView1);
        pickerWeight = findViewById(R.id.pickerView2);
        List<String> hourList = new ArrayList<>();
        List<String> minuteList = new ArrayList<>();
        List<String> weightList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourList.add(i < 10 ? "0" + i : i + "");
        }
        for (int i = 0; i < 60; ) {
            minuteList.add(i < 10 ? "0" + i : i + "");
            i+=10;
        }
        for (int i = 50; i <= 500; ) {
            weightList.add(i + "");
            i+=50;
        }
        pickerHour.setData(hourList);
        pickerMinute.setData(minuteList);
        pickerWeight.setData(weightList);
        /*pickerHour.setOnSelectListener(new PickerView.onSelectListener() {

            @Override
            public void onSelect(String text)
            {
                Toast.makeText(getContext(), "选择了 " + text + " 时",
                        Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_cancel:
                this.dismiss();
                break;
            case R.id.button_finish:
                data.addAlarms(new Alarm(Integer.parseInt(pickerHour.getCurrentSelected()),
                        Integer.parseInt(pickerMinute.getCurrentSelected()),
                        Integer.parseInt(pickerWeight.getCurrentSelected()), true));
                this.dismiss();
                break;
            default:
                break;
        }
    }

}
