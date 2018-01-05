package com.lunaticlemon.lifecast.show_article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lunaticlemon.lifecast.R;

import java.util.ArrayList;

/**
 * Created by lemon on 2018-01-06.
 */

public class News_Adapter extends BaseAdapter {
    private ArrayList<News> news_arr_list = new ArrayList<News>();
    Context context;

    public News_Adapter() {
    }

    private class ViewHolder{
        public TextView textView_title;
        public TextView textView_newspaper;
        public TextView textView_date;
    }

    @Override
    public int getCount() {
        return news_arr_list.size();
    }

    @Override
    public News getItem(int position) {
        return news_arr_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        context = parent.getContext();

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_news, parent, false);

            holder.textView_title = (TextView) convertView.findViewById(R.id.textView_title);
            holder.textView_newspaper = (TextView) convertView.findViewById(R.id.textView_newspaper);
            holder.textView_date = (TextView) convertView.findViewById(R.id.textView_date);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        News listViewItem = news_arr_list.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        holder.textView_title.setText(listViewItem.getTitle());
        holder.textView_newspaper.setText(listViewItem.getNewspaper());
        holder.textView_date.setText(listViewItem.getDate());

        return convertView;
    }

    public void init()
    {
        news_arr_list.clear();
    }

    public void addItem(String _keyword, String _url, String _title, String _date, String _newspaper, int _view)
    {
        News item = new News(_keyword, _url, _title, _date, _newspaper, _view);

        news_arr_list.add(item);
    }
}
