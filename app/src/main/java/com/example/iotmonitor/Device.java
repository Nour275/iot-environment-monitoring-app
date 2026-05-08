package com.example.iotmonitor;

public class Device {

    public String device_id;
    public String location;
    public String status;
    public String device_type;
    public double temperature;
    public double humidity;
    public double air_quality;
    public double latitude;
    public double longitude;

    public Device() {
        // Required for Firebase
    }
}