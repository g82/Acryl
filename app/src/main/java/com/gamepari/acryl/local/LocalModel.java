package com.gamepari.acryl.local;

import net.daum.android.map.coord.MapCoordLatLng;

/**
 * Created by gamepari on 12/21/14.
 */
public class LocalModel {

    public LocalModel(double lat, double lng, String title) {

        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }

    double lat;
    double lng;
    String title;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getTitle() {
        return title;
    }
}
