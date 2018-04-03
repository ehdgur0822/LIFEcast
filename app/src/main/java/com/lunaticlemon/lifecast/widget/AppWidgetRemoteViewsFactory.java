package com.lunaticlemon.lifecast.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.ShowArticleActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lemon on 2018-03-14.
 */

// widget listview adapter
public class AppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static String TAG = "AppWidgetAdapter";

    private Context mContext;

    // keyword list (listview data)
    private ArrayList<Keyword> keyword_list = null;

    // http request queue
    private static RequestQueue volley_queue = null;

    // 현재 listview에 보여지는 news의 section
    private ShowArticleActivity.section cur_section = null;

    public AppWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;

        volley_queue = Volley.newRequestQueue(applicationContext);
        keyword_list = new ArrayList<>();

        // TODO
        // 오늘 날짜로 변경 필요 (크롤링 시)
        getKeywords(AppWidget.cur_section, 2018, 1, 19);
    }

    @Override
    public void onCreate() {
        Log.d("WIDGETFACTORY", "oncreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.d("WIDGETFACTORY", "ondata changed");
        if(keyword_list != null && this.cur_section != AppWidget.cur_section)
        {
            // 사용자가 section 변경 시 listview data 변경
            keyword_list.clear();
            getKeywords(AppWidget.cur_section, 2018, 1, 19);
        }
    }

    @Override
    public void onDestroy() {
        if (keyword_list != null) {
            keyword_list.clear();
        }
    }

    @Override
    public int getCount() {
        return keyword_list.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || keyword_list == null) {
            return null;
        }

        // listview에 data표시
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(), R.layout.listview_keyword);
        remoteView.setTextViewText(R.id.textView_keyword, keyword_list.get(position).getKeyword());
        remoteView.setTextViewText(R.id.textView_count, keyword_list.get(position).getCount() + "회");

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // 서버에서 해당 분야, 날짜에 많이 나온 Keyword를 가져오는 함수
    public void getKeywords(final ShowArticleActivity.section selected_section, final int selected_year, final int selected_month, final int selected_day){

        cur_section = selected_section;

        Log.d("WIDGETFACTORY", "getkeyword");
        // 서버와 http protocol을 이용하여 사용자가 선택한 분야/날짜의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        // response : (keyword , count) 로 이루어진 json array
        String url = "http://115.71.236.22/get_keyword.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray news_arr = jsonObj.getJSONArray("result");

                            // pieChart data 생성
                            for (int i = 0; i < news_arr.length(); i++) {
                                JSONObject news = news_arr.getJSONObject(i);
                                keyword_list.add(new Keyword(news.getString("keyword"), news.getInt("count")));
                            }

                            // refresh all your widgets
                            AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
                            ComponentName cn = new ComponentName(mContext, AppWidget.class);
                            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.listViewKeyword);

                            Log.d("WIDGETFACTORY", "keyword" +  Integer.toString(keyword_list.size()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        getKeywords(selected_section, selected_year, selected_month, selected_day);
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                String date = selected_year + "-" + selected_month + "-" + selected_day;
                String section;
                switch(selected_section)
                {
                    case POLITIC:
                        section = "politic";
                        break;
                    case ECONOMY:
                        section = "economy";
                        break;
                    case SOCIETY:
                        section = "society";
                        break;
                    case SPORT:
                        section = "sport";
                        break;
                    case WORLD:
                        section = "world";
                        break;
                    case CULTURE:
                        section = "culture";
                        break;
                    case SCIENCE:
                        section = "science";
                        break;
                    default:
                        section = "politic";
                        break;
                }

                Map<String, String>  params = new HashMap<String, String>();
                params.put("section", section);
                params.put("date", date);
                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}

