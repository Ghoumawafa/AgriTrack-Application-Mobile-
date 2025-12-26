package com.example.agritrack.Models;

public class DashboardModule {
    private String icon;
    private String title;
    private String description;
    private String color;
    private Class<?> activityClass;

    public DashboardModule(String icon, String title, String description,
                           String color, Class<?> activityClass) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.color = color;
        this.activityClass = activityClass;
    }
    public String getIcon() { return icon; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public Class<?> getActivityClass() { return activityClass; }
}