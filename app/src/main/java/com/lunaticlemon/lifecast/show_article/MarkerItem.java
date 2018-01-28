package com.lunaticlemon.lifecast.show_article;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by lemon on 2018-01-26.
 */

public class MarkerItem implements ClusterItem {
    final String title;
    final String snippet;
    final LatLng latLng;
    final String city;

    public MarkerItem(String title, String snippet, LatLng latLng, String city) {
        this.title = title;
        this.snippet = snippet;
        this.latLng = latLng;
        this.city = city;
    }
    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getCity() { return city; }
}

