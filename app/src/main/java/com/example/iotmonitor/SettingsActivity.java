package com.example.iotmonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    EditText aqiInput, cityInput, latInput, lonInput;
    Button saveBtn;

    private static final String DATABASE_URL =
            "https://iot-environmental-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        aqiInput = findViewById(R.id.aqiInput);
        cityInput = findViewById(R.id.cityInput);
        latInput = findViewById(R.id.latInput);
        lonInput = findViewById(R.id.lonInput);
        saveBtn = findViewById(R.id.saveBtn);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        aqiInput.setText(String.valueOf(prefs.getInt("aqi_threshold", 100)));
        cityInput.setText(prefs.getString("city", "Moscow, Russia"));
        latInput.setText(String.valueOf(prefs.getFloat("lat", 55.7558f)));
        lonInput.setText(String.valueOf(prefs.getFloat("lon", 37.6173f)));

        saveBtn.setOnClickListener(v -> {
            String aqiText = aqiInput.getText().toString();
            String cityText = cityInput.getText().toString();
            String latText = latInput.getText().toString();
            String lonText = lonInput.getText().toString();

            if (aqiText.isEmpty() || cityText.isEmpty() || latText.isEmpty() || lonText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int aqi = Integer.parseInt(aqiText);
                float lat = Float.parseFloat(latText);
                float lon = Float.parseFloat(lonText);

                prefs.edit()
                        .putInt("aqi_threshold", aqi)
                        .putString("city", cityText)
                        .putFloat("lat", lat)
                        .putFloat("lon", lon)
                        .apply();

                saveLocationAsDevice(cityText, lat, lon);

                Toast.makeText(this, "Локация сохранена как IoT-узел", Toast.LENGTH_SHORT).show();
                finish();

            } catch (Exception e) {
                Toast.makeText(this, "Ошибка ввода данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocationAsDevice(String city, float lat, float lon) {
        String deviceId = city
                .replace(",", "")
                .replace(" ", "_")
                .toLowerCase();

        HashMap<String, Object> device = new HashMap<>();
        device.put("device_id", deviceId);
        device.put("location", city);
        device.put("latitude", lat);
        device.put("longitude", lon);
        device.put("device_type", "saved_virtual_iot_node");
        device.put("status", "saved");

        FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("devices")
                .child(deviceId)
                .updateChildren(device);
    }
}