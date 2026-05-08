package com.example.iotmonitor;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class MapActivity extends AppCompatActivity {

    WebView mapView;

    private static final String DATABASE_URL =
            "https://iot-environmental-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);

        WebSettings settings = mapView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; IoTMonitorApp) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
        );

        mapView.setWebViewClient(new WebViewClient());

        loadDevicesMap();
    }

    private void loadDevicesMap() {
        DatabaseReference ref = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("devices");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                StringBuilder markers = new StringBuilder();

                android.content.SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

                double centerLat = prefs.getFloat("lat", 33.8938f);
                double centerLon = prefs.getFloat("lon", 35.5018f);

                boolean firstDevice = false;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Device d = data.getValue(Device.class);

                    if (d == null) continue;
                    if (d.location == null) continue;
                    if (d.latitude == 0 || d.longitude == 0) continue;

                    /*if (firstDevice) {
                        centerLat = d.latitude;
                        centerLon = d.longitude;
                        firstDevice = false;
                    }*/

                    String safeLocation = d.location.replace("'", "\\'");
                    String safeStatus = d.status != null ? d.status.replace("'", "\\'") : "--";
                    String color = getAqiColor(d.air_quality);

                    markers.append("L.circleMarker([")
                            .append(d.latitude)
                            .append(",")
                            .append(d.longitude)
                            .append("], {")
                            .append("radius: 10,")
                            .append("color: '").append(color).append("',")
                            .append("fillColor: '").append(color).append("',")
                            .append("fillOpacity: 0.85")
                            .append("})")
                            .append(".addTo(map)")
                            .append(".bindPopup('")
                            .append("<b>").append(safeLocation).append("</b>")
                            .append("<br>Temperature: ").append(d.temperature).append(" °C")
                            .append("<br>AQI: ").append(d.air_quality)
                            .append("<br>Status: ").append(safeStatus)
                            .append("');");
                }

                String html = buildMapHtml(markers.toString(), centerLat, centerLon);

                mapView.loadDataWithBaseURL(
                        "https://leafletjs.com/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                );
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private String getAqiColor(double aqi) {
        if (aqi <= 50) {
            return "green";
        } else if (aqi <= 100) {
            return "orange";
        } else {
            return "red";
        }
    }

    private String buildMapHtml(String markers, double centerLat, double centerLon) {

        String embedUrl = "https://www.openstreetmap.org/export/embed.html?bbox="
                + (centerLon - 5) + "%2C" + (centerLat - 5) + "%2C"
                + (centerLon + 5) + "%2C" + (centerLat + 5)
                + "&layer=mapnik&marker=" + centerLat + "%2C" + centerLon;

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; }" +
                "iframe { width:100%; height:100vh; border:0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<iframe src='" + embedUrl + "'></iframe>" +
                "</body>" +
                "</html>";
    }
}