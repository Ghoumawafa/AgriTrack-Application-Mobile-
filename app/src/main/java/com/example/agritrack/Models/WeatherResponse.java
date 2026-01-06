package com.example.agritrack.Models;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public String name; // city name

    public static class Main {
        public double temp;
        public int humidity;
        public double pressure;
    }

    public static class Weather {
        public String main;
        public String description;
        public String icon;
    }

    public static class Wind {
        public double speed;
    }
}

