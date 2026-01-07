package com.example.agritrack.Models;

public class ExchangeRateResponse {
    public boolean success;
    public long timestamp;
    public String base;
    public String date;
    public Rates rates;

    public static class Rates {
        public double TND;
    }
}

