package com.example.iotmonitor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    LineChart tempChart;

    ArrayList<Entry> tempEntries = new ArrayList<>();
    ArrayList<Entry> airEntries = new ArrayList<>();
    ArrayList<String> timeLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        tempChart = findViewById(R.id.tempChart);
        setupChart();

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://iot-environmental-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("history");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                tempEntries.clear();
                airEntries.clear();
                timeLabels.clear();

                int index = 0;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Object tempValue = data.child("temperature").getValue();
                    Object airValue = data.child("air_quality").getValue();
                    Object timeValue = data.child("timestamp").getValue();

                    if (tempValue == null || airValue == null || timeValue == null) {
                        continue;
                    }

                    float temperature = Float.parseFloat(tempValue.toString());
                    float aqi = Float.parseFloat(airValue.toString());
                    String time = timeValue.toString();

                    tempEntries.add(new Entry(index, temperature));
                    airEntries.add(new Entry(index, aqi));
                    timeLabels.add(time);

                    index++;
                }

                updateChart();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void setupChart() {

        tempChart.getDescription().setEnabled(false);
        tempChart.setTouchEnabled(true);
        tempChart.setDragEnabled(true);
        tempChart.setScaleEnabled(true);
        tempChart.setPinchZoom(true);
        tempChart.setExtraOffsets(10, 10, 10, 20);

        // LEFT AXIS (Temperature)
        YAxis leftAxis = tempChart.getAxisLeft();
        leftAxis.setTextSize(11f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(50f);
        leftAxis.setLabelCount(6, true);

        // RIGHT AXIS (AQI)
        YAxis rightAxis = tempChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setTextSize(11f);
        rightAxis.setGranularity(10f);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(200f);
        rightAxis.setLabelCount(5, true);


        XAxis xAxis = tempChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(9f);
        xAxis.setLabelRotationAngle(-35f);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < timeLabels.size()) {
                    return timeLabels.get(index);
                }
                return "";
            }
        });

        tempChart.getLegend().setTextSize(12f);
    }

    private void updateChart() {
        if (tempEntries.isEmpty() || airEntries.isEmpty()) {
            tempChart.clear();
            return;
        }

        LineDataSet tempSet = new LineDataSet(tempEntries, "Температура, °C");
        tempSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        tempSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        tempSet.setLineWidth(3f);
        tempSet.setCircleRadius(4f);
        tempSet.setDrawValues(false);
        tempSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        tempSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet airSet = new LineDataSet(airEntries, "AQI");
        airSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        airSet.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));
        airSet.setLineWidth(2.5f);
        airSet.setCircleRadius(4f);
        airSet.setDrawValues(false);
        airSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        airSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData lineData = new LineData(tempSet, airSet);
        tempChart.setData(lineData);

        tempChart.animateX(600);
        tempChart.invalidate();
    }
}