package com.lunaticlemon.lifecast.show_article;

import java.util.StringTokenizer;

/**
 * Created by lemon on 2018-01-24.
 */

public class Local_News {
    private int id;
    private String keyword1, keyword2, keyword3;
    private String url;
    private String title;
    private String date;
    private String city;
    private double latitude;
    private double longitude;
    private int view;

    public Local_News(int _id, String _keyword, String _url, String _title, String _date, String _city, double _latitude, double _longitude, int _view) {
        if(_keyword.equals("null"))
        {
            keyword1 = null;
            keyword2 = null;
            keyword3 = null;
        }
        else
        {
            StringTokenizer st = new StringTokenizer(_keyword, "/");
            keyword1 = st.nextToken();
            keyword2 = st.nextToken();
            keyword3 = st.nextToken();
        }
        id = _id;
        url = _url;
        title = _title;
        date = _date;
        city = _city;
        latitude = _latitude;
        longitude = _longitude;
        view = _view;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyword1() {
        return keyword1;
    }

    public void setKeyword1(String keyword1) {
        this.keyword1 = keyword1;
    }

    public String getKeyword2() {
        return keyword2;
    }

    public void setKeyword2(String keyword2) {
        this.keyword2 = keyword2;
    }

    public String getKeyword3() {
        return keyword3;
    }

    public void setKeyword3(String keyword3) {
        this.keyword3 = keyword3;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }
}
