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
 * Created by lemon on 2018-01-26.
 */

public class Marker_Adapter extends BaseAdapter {
    private ArrayList<Local_News> localNews_arr_list = new ArrayList<Local_News>();
    Context context;

    public Marker_Adapter() {
    }

    private class ViewHolder{
        public TextView textView_title;
    }

    @Override
    public int getCount() {
        return localNews_arr_list.size();
    }

    @Override
    public Local_News getItem(int position) {
        return localNews_arr_list.get(position);
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
            convertView = inflater.inflate(R.layout.listview_marker, parent, false);

            holder.textView_title = (TextView) convertView.findViewById(R.id.textView_title);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Local_News listViewItem = localNews_arr_list.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        holder.textView_title.setText(listViewItem.getTitle());

        return convertView;
    }

    public void init()
    {
        localNews_arr_list.clear();
    }

    public void addItem(int _id, String _keyword, String _url, String _title, String _date, String _city, double _latitude, double _longitude, int _view)
    {
        Local_News item = new Local_News(_id, _keyword, _url, _title, _date, _city, _latitude, _longitude, _view);

        localNews_arr_list.add(item);
    }
}
