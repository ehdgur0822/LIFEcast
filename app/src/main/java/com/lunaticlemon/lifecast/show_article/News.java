package com.lunaticlemon.lifecast.show_article;

/**
 * Created by lemon on 2018-01-06.
 */

public class News {
    String keyword;
    String url;
    String title;
    String date;
    String newspaper;
    int view;

    public News(String _keyword, String _url, String _title, String _date, String _newspaper, int _view) {
        keyword = _keyword;
        url = _url;
        title = _title;
        date = _date;
        newspaper = _newspaper;
        view = _view;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getNewspaper() {
        return newspaper;
    }

    public int getView() {
        return view;
    }
}
