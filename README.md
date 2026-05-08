# IoT Environment Monitoring Mobile Application

Android mobile IoT application for environmental monitoring using Firebase Realtime Database and Open-Meteo API.

## Project Description

This project was developed as part of a graduation thesis focused on the design and implementation of a mobile IoT-based environmental monitoring system. The application collects environmental data such as:
- Temperature
- Humidity
- Air Quality Index (AQI)
- PM2.5
- PM10
The system uses virtual IoT nodes connected to cloud infrastructure and displays real-time environmental information through a mobile Android application.
## Main Features
- Real-time environmental monitoring
- Firebase cloud synchronization
- Virtual IoT devices
- AQI warning system
- Historical data visualization
- Temperature and AQI charts
- Location-based monitoring
- Interactive map display
- User settings and AQI threshold configuration

## Technologies Used

| Component | Technology |
|---|---|
| Mobile Platform | Android |
| Programming Language | Java |
| IDE | Android Studio |
| Cloud Database | Firebase Realtime Database |
| External API | Open-Meteo API |
| HTTP Requests | Volley |
| Charts | MPAndroidChart |
| Map Integration | OpenStreetMap + WebView |


## System Architecture

The system consists of four main layers:

1. Device Layer  
2. Communication Layer  
3. Cloud Layer  
4. Mobile Application Layer  

Environmental data is obtained from Open-Meteo API, stored in Firebase Realtime Database, and visualized in the Android application.

## Application Screens

The application contains:

- Main Dashboard
- Virtual IoT Devices Screen
- Historical Charts Screen
- Map Screen
- Settings Screen

## Firebase Structure

The database includes the following main nodes:

- sensor_data
- history
- devices

## Project Purpose

The main purpose of this project is to demonstrate the feasibility of implementing a mobile IoT environmental monitoring system using cloud technologies and virtual IoT nodes without requiring physical sensors.
The architecture can later be extended using real IoT hardware such as ESP32 and environmental sensors.



## Author

Developed as a graduation thesis project in the field of Internet of Things (IoT) and mobile application development.
