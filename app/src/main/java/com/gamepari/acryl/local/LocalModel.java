package com.gamepari.acryl.local;

/**
 * Created by gamepari on 12/21/14.
 */
public class LocalModel {

    int tagId;
    double lat;
    double lng;
    String title;

    public LocalModel(double lat, double lng, String title, int tagId) {

        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.tagId = tagId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getTitle() {
        return title;
    }

    public int getTagId() {
        return tagId;
    }
}
