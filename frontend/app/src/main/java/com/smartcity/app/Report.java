package com.smartcity.app;

import java.io.Serializable;

public class Report implements Serializable {
    public final long id;
    public final String title;
    public final String description;
    public final String category;
    public final String status;
    public final double latitude;
    public final double longitude;
    public final String imageUrl;

    public Report(long id, String title, String description, String category, String status,
                  double latitude, double longitude, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }
}
