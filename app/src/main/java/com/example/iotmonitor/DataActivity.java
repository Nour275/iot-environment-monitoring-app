package com.example.iotmonitor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class DataActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Device> deviceList;
    DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        recyclerView = findViewById(R.id.devicesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deviceList = new ArrayList<>();
        adapter = new DeviceAdapter(deviceList);
        recyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://iot-environmental-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("devices");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                deviceList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Device d = data.getValue(Device.class);
                    deviceList.add(d);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}