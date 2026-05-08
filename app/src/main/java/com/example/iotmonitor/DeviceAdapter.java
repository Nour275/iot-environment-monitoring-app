package com.example.iotmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    ArrayList<Device> list;

    public DeviceAdapter(ArrayList<Device> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceId, location, temp, aqi, status;

        public ViewHolder(View v) {
            super(v);
            deviceId = v.findViewById(R.id.deviceId);
            location = v.findViewById(R.id.location);
            temp = v.findViewById(R.id.temp);
            aqi = v.findViewById(R.id.aqi);
            status = v.findViewById(R.id.status);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device d = list.get(position);

        holder.deviceId.setText("Device: " + safe(d.device_id));
        holder.location.setText("Location: " + safe(d.location));
        holder.temp.setText("🌡 " + d.temperature + " °C");
        holder.aqi.setText("🌫 AQI: " + d.air_quality);
        holder.status.setText("Status: " + safe(d.status));

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            if (d.location == null || d.latitude == 0 || d.longitude == 0) {
                Toast.makeText(context, "Location data is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

            prefs.edit()
                    .putString("city", d.location)
                    .putFloat("lat", (float) d.latitude)
                    .putFloat("lon", (float) d.longitude)
                    .apply();

            Toast.makeText(context, "Selected: " + d.location, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra("lat", d.latitude);
            intent.putExtra("lon", d.longitude);
            intent.putExtra("location", d.location);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String safe(String value) {
        return value != null ? value : "--";
    }
}