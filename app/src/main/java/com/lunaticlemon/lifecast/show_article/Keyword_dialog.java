package com.lunaticlemon.lifecast.show_article;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lunaticlemon.lifecast.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lemon on 2018-01-18.
 */

public class Keyword_dialog extends Dialog {
    private Context con;
    private ShowArticleActivity.section selected_section;
    private int selected_year, selected_month, selected_day;

    String TAG = "Keyword_dialog";

    // http request queue
    RequestQueue volley_queue;

    PieChart pieChart;
    TextView textView_title;

    public Keyword_dialog(@NonNull Context context, ShowArticleActivity.section _section, int _year, int _month, int _day) {
        super(context);

        con = context;
        selected_section = _section;
        selected_year = _year;
        selected_month = _month;
        selected_day = _day;

        this.setCanceledOnTouchOutside(true); // 다이얼로그 바깥영역 터치시, 다이알로그 닫기
        this.setCancelable(true); // 백키로 다이얼로그 닫기

        // 다이얼로그 외부 화면 흐리게 하기
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.keyword_dialog);

        volley_queue = Volley.newRequestQueue(con);

        pieChart = (PieChart) findViewById(R.id.piechart);
        pieChart.setUsePercentValues(true);

        textView_title = (TextView) findViewById(R.id.textView_title);

        switch(selected_section)
        {
            case POLITIC:
                textView_title.setText("오늘의 정치 키워드");
                break;
            case ECONOMY:
                textView_title.setText("오늘의 경제 키워드");
                break;
            case SOCIETY:
                textView_title.setText("오늘의 사회 키워드");
                break;
            case SPORT:
                textView_title.setText("오늘의 스포츠 키워드");
                break;
            case WORLD:
                textView_title.setText("오늘의 세계 키워드");
                break;
            case CULTURE:
                textView_title.setText("오늘의 연예/문화 키워드");
                break;
            case SCIENCE:
                textView_title.setText("오늘의 IT/과학 키워드");
                break;
        }

        getKeywords(selected_section, selected_year, selected_month, selected_day);

    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss()
    {
        super.dismiss();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    // 서버에서 해당 분야, 날짜에 많이 나온 Keyword를 가져오는 함수
    public void getKeywords(final ShowArticleActivity.section selected_section, final int selected_year, final int selected_month, final int selected_day){

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
                            ArrayList<Entry> yvalues = new ArrayList<Entry>();
                            ArrayList<String> xvalues = new ArrayList<String>();

                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray news_arr = jsonObj.getJSONArray("result");

                            // pieChart data 생성
                            for (int i = 0; i < news_arr.length(); i++) {
                                JSONObject news = news_arr.getJSONObject(i);
                                xvalues.add(news.getString("keyword"));
                                yvalues.add(new Entry(news.getInt("count"), i));
                            }

                            PieDataSet dataSet = new PieDataSet(yvalues, "Keyword Ranking");
                            dataSet.setValueTextColor(Color.WHITE);
                            PieData data = new PieData(xvalues, dataSet);

                            // In Percentage term
                            data.setValueFormatter(new PercentFormatter());

                            pieChart.setData(data);

                            pieChart.setDrawHoleEnabled(true);
                            pieChart.setTransparentCircleRadius(10f);
                            pieChart.setHoleRadius(15f);
                            pieChart.getLegend().setEnabled(false);
                            ArrayList<Integer> colors = new ArrayList<Integer>();

                            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                                colors.add(c);
                            for (int c : ColorTemplate.COLORFUL_COLORS)
                                colors.add(c);

                            colors.add(ColorTemplate.getHoloBlue());
                            dataSet.setColors(colors);

                            data.setValueTextSize(10f);
                            data.setValueTextColor(Color.DKGRAY);
                            //pieChart.setOnChartValueSelectedListener(this);

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
