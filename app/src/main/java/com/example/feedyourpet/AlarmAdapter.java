package com.example.feedyourpet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder>{
    private static final String TAG = "AlarmAdapter";
    private Context context;
    private List<Alarm> alarms;

    private Data data;

    public AlarmAdapter(Context context) {
        this.context = context;

        this.data=(Data) context.getApplicationContext();
        this.alarms = data.getAlarms();

    }
    @NonNull
    @Override
    public AlarmAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.alarm_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AlarmAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Alarm alarm = alarms.get(position);
        if(position<alarms.size()-2){
            holder.time.setText((alarm.getHour() < 10 ? "0" + alarm.getHour() : alarm.getHour())
                    + ":" + (alarm.getMinute() < 10 ? "0" + alarm.getMinute() : alarm.getMinute()));
            holder.weight.setText(alarm.getWeight()+"");
            if (alarm.getState()){
                alarmOn(holder,alarm);
            }
            else if (!alarm.getState()){
                alarmOff(holder,alarm);
            }
            holder.constrain.setVisibility(View.VISIBLE);
        }
        else {
            holder.constrain.setVisibility(View.GONE);
            holder.imageBack.setClickable(false);
            holder.imageSwitch.setClickable(false);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog dialog = new CustomDialog(view.getContext(),position);
                dialog.show();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        notifyDataSetChanged();
                    }
                });
            }
        });

        holder.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alarm.getState()){
                    alarmOff(holder,alarm);
                    data.changeAlarmState(position);
                }
                else if (!alarm.getState()){
                    alarmOn(holder,alarm);
                    data.changeAlarmState(position);
                }
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(holder.getAdapterPosition());

            }
        });

    }

    private void alarmOn(@NonNull AlarmAdapter.ViewHolder holder, @NonNull Alarm alarm){
        holder.imageBack.setImageResource(R.mipmap.alarm_on);
        holder.time.setTextColor(context.getResources().getColor(R.color.black));
        holder.weight.setTextColor(context.getResources().getColor(R.color.gray_737373));
        holder.unit.setTextColor(context.getResources().getColor(R.color.gray_737373));
    }
    private void alarmOff(@NonNull AlarmAdapter.ViewHolder holder, @NonNull Alarm alarm){
        holder.imageBack.setImageResource(R.mipmap.alarm_off);
        holder.time.setTextColor(context.getResources().getColor(R.color.gray_c5c5c5));
        holder.weight.setTextColor(context.getResources().getColor(R.color.gray_c5c5c5));
        holder.unit.setTextColor(context.getResources().getColor(R.color.gray_c5c5c5));
    }

    public void remove(int adapterPosition) {
        data.removeAlarm(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View constrain;
        TextView time,weight,unit;
        ImageView imageBack,imageSwitch;
        Button buttonDelete;
        public ViewHolder(View itemView) {
            super(itemView);
            time=itemView.findViewById(R.id.time_item);
            weight=itemView.findViewById(R.id.weight_item);
            unit=itemView.findViewById(R.id.text_weight_unit);
            imageBack=itemView.findViewById(R.id.image_alarm_back);
            imageSwitch=itemView.findViewById(R.id.image_alarm_switch);
            buttonDelete=itemView.findViewById(R.id.button_delete_alarm);

            constrain=itemView.findViewById(R.id.alarm_item_all);
        }
    }
}
