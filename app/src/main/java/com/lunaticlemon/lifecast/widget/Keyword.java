package com.lunaticlemon.lifecast.widget;

/**
 * Created by lemon on 2018-03-14.
 */

// widget keyword listview에 쓰일 keyword list
public class Keyword {
    private String keyword; // keyword
    private int count;  // keyword나온 횟수

    public Keyword(String _keyword, int _count)
    {
        this.keyword = _keyword;
        this.count = _count;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
