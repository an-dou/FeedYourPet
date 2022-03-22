package com.example.feedyourpet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlarmNowAdapter extends RecyclerView.Adapter<AlarmNowAdapter.ViewHolder>{

    private Context context;
    private List<Alarm> alarmsNow;
    private Data data;

    public AlarmNowAdapter(Context context){
        this.context = context;
        this.data=(Data) context.getApplicationContext();
        this.alarmsNow = data.getAlarmsNow();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.alarm_now_item,parent,false);
        AlarmNowAdapter.ViewHolder holder=new AlarmNowAdapter.ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alarm alarm = alarmsNow.get(position);
        holder.now_num.setText(position+1+".");
        if(alarm.getState()){
            holder.now_time.setText((alarm.getHour() < 10 ? "0" + alarm.getHour() : alarm.getHour())
                    + ":" + (alarm.getMinute() < 10 ? "0" + alarm.getMinute() : alarm.getMinute()));
            holder.now_weight.setText(alarm.getWeight()+"");
            holder.now_symbol.setVisibility(View.VISIBLE);
            holder.now_weight_unit.setVisibility(View.VISIBLE);
        }
        else {
            holder.now_time.setText("暂无");
            holder.now_symbol.setVisibility(View.INVISIBLE);
            holder.now_weight_unit.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return alarmsNow.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView now_num,now_time,now_weight,now_symbol,now_weight_unit;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            now_num=itemView.findViewById(R.id.now_num_item);
            now_time=itemView.findViewById(R.id.now_time_item);
            now_weight=itemView.findViewById(R.id.now_weight_item);
            now_symbol=itemView.findViewById(R.id.now_symbol);
            now_weight_unit=itemView.findViewById(R.id.now_weight_unit);
        }
    }
}
