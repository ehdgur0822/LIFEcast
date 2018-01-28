package com.lunaticlemon.lifecast.show_article;

import java.util.StringTokenizer;

/**
 * Created by lemon on 2018-01-06.
 */

public class News {
    private int id;
    private String keyword1, keyword2, keyword3;
    private String url;
    private String title;
    private String date;
    private String newspaper;
    private int view;
    private String section; // bucket news 삭제 시 분야 판별하는데 사용

    public News(int _id, String _keyword, String _url, String _title, String _date, String _newspaper, int _view, String _section) {
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
        newspaper = _newspaper;
        view = _view;
        section = _section;
    }

    public int getId() { return id; }

    public String getKeyword1() {
        return keyword1;
    }

    public String getKeyword2() {
        return keyword2;
    }

    public String getKeyword3() {
        return keyword3;
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

    public String getSection() { return section; }
}
