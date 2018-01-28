package com.lunaticlemon.lifecast.show_article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.lunaticlemon.lifecast.R;

import java.util.ArrayList;

/**
 * Created by lemon on 2018-01-06.
 */

public class News_Adapter extends BaseAdapter implements Filterable {
    private ArrayList<News> news_arr_list = new ArrayList<News>();
    private ArrayList<News> filtered_news_arr_list = news_arr_list; // keyword 혹은 title로 검색된 결과 list
    Filter news_filter;
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
        return filtered_news_arr_list.size();
    }

    @Override
    public News getItem(int position) {
        return filtered_news_arr_list.get(position);
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

        News listViewItem = filtered_news_arr_list.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        holder.textView_title.setText(listViewItem.getTitle());
        holder.textView_newspaper.setText(listViewItem.getNewspaper());
        holder.textView_date.setText(listViewItem.getDate());

        return convertView;
    }

    public void init()
    {
        news_arr_list.clear();
        filtered_news_arr_list = news_arr_list;
    }

    public void addItem(int _id, String _keyword, String _url, String _title, String _date, String _newspaper, int _view, String _section)
    {
        News item = new News(_id, _keyword, _url, _title, _date, _newspaper, _view, _section);

        news_arr_list.add(item);
    }

    public void deleteItem(int _id)
    {
        for(News item : news_arr_list)
        {
            if(item.getId() == _id)
            {
                news_arr_list.remove(item);
                break;
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (news_filter == null)
        {
            news_filter = new NewsFilter();
        }
        return news_filter ;
    }

    private class NewsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = news_arr_list;
                results.count = news_arr_list.size() ;
            } else {
                ArrayList<News> itemList = new ArrayList<News>() ;

                for (News item : news_arr_list) {
                    if (item.getTitle().toUpperCase().contains(constraint.toString().toUpperCase()))
                    {
                        itemList.add(item) ;
                    }
                    else if(item.getKeyword1() != null) {
                        if (item.getKeyword1().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            itemList.add(item);
                        } else if (item.getKeyword2().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            itemList.add(item);
                        } else if (item.getKeyword3().toUpperCase().contains(constraint.toString().toUpperCase())) {
                            itemList.add(item);
                        }
                    }
                }

                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            // update listview by filtered data list.
            filtered_news_arr_list = (ArrayList<News>) results.values ;

            // notify
            if (results.count > 0) {
                notifyDataSetChanged() ;
            } else {
                notifyDataSetInvalidated() ;
            }
        }
    }
}
