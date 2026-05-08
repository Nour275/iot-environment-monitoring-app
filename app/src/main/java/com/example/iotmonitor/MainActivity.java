package com.example.iotmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView tempText, humidityText, airText, timeText, locationText;
    DatabaseReference ref;

    Double latestTemperature = null;
    Double latestHumidity = null;
    Integer latestAqi = null;
    Double latestPm25 = null;
    Double latestPm10 = null;

    Handler handler = new Handler();
    Runnable refreshRunnable;

    boolean warningShown = false;
    long lastHistorySaveTime = 0;

    String locationName;
    double latitude;
    double longitude;

    private static final String DATABASE_URL =
            "https://iot-environmental-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSettings();

        tempText = findViewById(R.id.tempText);
        humidityText = findViewById(R.id.humidityText);
        airText = findViewById(R.id.airText);
        timeText = findViewById(R.id.timeText);
        locationText = findViewById(R.id.locationText);

        if (locationText != null) {
            locationText.setText("Location: " + locationName);
        }

        animateCards();

        ref = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("sensor_data");

        setupBottomNavigation();
        listenToFirebase();

        fetchWeatherData();
        fetchAirQualityData();
        updateAllSavedDevices();

        startAutoRefresh();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        locationName = prefs.getString("city", "Moscow, Russia");
        latitude = prefs.getFloat("lat", 55.7558f);
        longitude = prefs.getFloat("lon", 37.6173f);
    }

    private void animateCards() {
        tempText.setAlpha(0f);
        humidityText.setAlpha(0f);
        airText.setAlpha(0f);

        tempText.animate().alpha(1f).setDuration(700);
        humidityText.animate().alpha(1f).setDuration(900);
        airText.animate().alpha(1f).setDuration(1100);
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_data) {
                startActivity(new Intent(this, DataActivity.class));
                return true;
            } else if (id == R.id.nav_graph) {
                startActivity(new Intent(this, GraphActivity.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadSettings();
                fetchWeatherData();
                fetchAirQualityData();
                updateAllSavedDevices();

                handler.postDelayed(this, 5 * 60 * 1000);
            }
        };

        handler.postDelayed(refreshRunnable, 5 * 60 * 1000);
    }

    private void fetchWeatherData() {
        String url =
                "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&current=temperature_2m,relative_humidity_2m";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");

                        double temperature = current.getDouble("temperature_2m");
                        double humidity = current.getDouble("relative_humidity_2m");

                        latestTemperature = temperature;
                        latestHumidity = humidity;

                        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        ref.child("temperature").setValue(temperature);
                        ref.child("humidity").setValue(humidity);
                        ref.child("timestamp").setValue(timestamp);
                        ref.child("source").setValue("Open-Meteo API");
                        ref.child("location").setValue(locationName);
                        ref.child("latitude").setValue(latitude);
                        ref.child("longitude").setValue(longitude);
                        ref.child("device_id").setValue("virtual_device_main");
                        ref.child("device_type").setValue("virtual_iot_node");
                        ref.child("status").setValue("active");

                        saveHistoryIfReady();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }

    private void fetchAirQualityData() {
        String url =
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&current=us_aqi,pm2_5,pm10";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");

                        int aqi = current.getInt("us_aqi");
                        double pm25 = current.getDouble("pm2_5");
                        double pm10 = current.getDouble("pm10");

                        latestAqi = aqi;
                        latestPm25 = pm25;
                        latestPm10 = pm10;

                        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        ref.child("air_quality").setValue(aqi);
                        ref.child("pm2_5").setValue(pm25);
                        ref.child("pm10").setValue(pm10);
                        ref.child("timestamp").setValue(timestamp);
                        ref.child("location").setValue(locationName);
                        ref.child("latitude").setValue(latitude);
                        ref.child("longitude").setValue(longitude);
                        ref.child("device_id").setValue("virtual_device_main");
                        ref.child("device_type").setValue("virtual_iot_node");
                        ref.child("status").setValue("active");

                        saveHistoryIfReady();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }

    private void updateAllSavedDevices() {

        DatabaseReference devicesRef = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("devices");

        devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot data : snapshot.getChildren()) {

                    Device d = data.getValue(Device.class);

                    if (d == null) continue;
                    if (d.device_id == null) continue;
                    if (d.location == null) continue;
                    if (d.latitude == 0 || d.longitude == 0) continue;

                    fetchDeviceData(d.device_id, d.location, d.latitude, d.longitude);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void fetchDeviceData(String deviceId, String location, double lat, double lon) {

        String weatherUrl =
                "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon +
                        "&current=temperature_2m,relative_humidity_2m";

        String airUrl =
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=" + lat +
                        "&longitude=" + lon +
                        "&current=us_aqi,pm2_5,pm10";

        DatabaseReference deviceRef = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("devices")
                .child(deviceId);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                weatherUrl,
                null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");

                        double temp = current.getDouble("temperature_2m");
                        double hum = current.getDouble("relative_humidity_2m");
                        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        deviceRef.child("temperature").setValue(temp);
                        deviceRef.child("humidity").setValue(hum);
                        deviceRef.child("timestamp").setValue(timestamp);
                        deviceRef.child("location").setValue(location);
                        deviceRef.child("latitude").setValue(lat);
                        deviceRef.child("longitude").setValue(lon);
                        deviceRef.child("device_id").setValue(deviceId);
                        deviceRef.child("device_type").setValue("saved_virtual_iot_node");
                        deviceRef.child("status").setValue("active");
                        deviceRef.child("source").setValue("Open-Meteo API");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        JsonObjectRequest airRequest = new JsonObjectRequest(
                Request.Method.GET,
                airUrl,
                null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current");

                        int aqi = current.getInt("us_aqi");
                        double pm25 = current.getDouble("pm2_5");
                        double pm10 = current.getDouble("pm10");
                        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                        deviceRef.child("air_quality").setValue(aqi);
                        deviceRef.child("pm2_5").setValue(pm25);
                        deviceRef.child("pm10").setValue(pm10);
                        deviceRef.child("timestamp").setValue(timestamp);
                        deviceRef.child("source").setValue("Open-Meteo API");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );

        queue.add(weatherRequest);
        queue.add(airRequest);
    }

    private void saveHistoryIfReady() {
        if (latestTemperature == null || latestHumidity == null || latestAqi == null
                || latestPm25 == null || latestPm10 == null) {
            return;
        }

        long now = System.currentTimeMillis();

        if (now - lastHistorySaveTime < 60 * 1000) {
            return;
        }

        lastHistorySaveTime = now;

        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        HashMap<String, Object> record = new HashMap<>();
        record.put("temperature", latestTemperature);
        record.put("humidity", latestHumidity);
        record.put("air_quality", latestAqi);
        record.put("pm2_5", latestPm25);
        record.put("pm10", latestPm10);
        record.put("timestamp", timestamp);
        record.put("source", "Open-Meteo API");
        record.put("location", locationName);
        record.put("latitude", latitude);
        record.put("longitude", longitude);
        record.put("device_id", "virtual_device_main");
        record.put("device_type", "virtual_iot_node");
        record.put("status", "active");

        FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("history")
                .push()
                .setValue(record);
    }

    private void listenToFirebase() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String temp = valueOrDash(snapshot, "temperature");
                String humidity = valueOrDash(snapshot, "humidity");
                String air = valueOrDash(snapshot, "air_quality");
                String time = valueOrDash(snapshot, "timestamp");
                String location = valueOrDash(snapshot, "location");

                tempText.setText("🌡 Temperature\n" + temp + " °C");
                humidityText.setText("💧 Humidity\n" + humidity + " %");

                if (timeText != null) {
                    timeText.setText("Last update: " + time);
                }

                if (locationText != null && !location.equals("--")) {
                    locationText.setText("Location: " + location);
                }

                try {
                    int aqi = Integer.parseInt(air);

                    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                    int threshold = prefs.getInt("aqi_threshold", 100);

                    if (aqi <= 50) {
                        airText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        airText.setText("🌫 Air Quality\n" + air + "  ● Good");
                        warningShown = false;
                    } else if (aqi <= threshold) {
                        airText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                        airText.setText("🌫 Air Quality\n" + air + "  ● Moderate");
                        warningShown = false;
                    } else {
                        airText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        airText.setText("🌫 Air Quality\n" + air + "  ● Warning!");

                        if (!warningShown) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Warning: AQI is above your selected threshold (" + threshold + ")",
                                    Toast.LENGTH_LONG
                            ).show();
                            warningShown = true;
                        }
                    }

                } catch (Exception e) {
                    airText.setTextColor(getResources().getColor(android.R.color.black));
                    airText.setText("🌫 Air Quality\n--");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tempText.setText("Error: " + error.getMessage());
            }
        });
    }

    private String valueOrDash(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value != null ? value.toString() : "--";
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSettings();

        if (locationText != null) {
            locationText.setText("Location: " + locationName);
        }

        if (ref != null) {
            fetchWeatherData();
            fetchAirQualityData();
            updateAllSavedDevices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
}