package com.lunaticlemon.lifecast.show_article;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import android.widget.TextView;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lemon on 2018-01-18.
 */

public class Keyword_dialog extends Dialog {
    private Context con;
    private ShowArticleActivity.section selected_section;
    private int selected_year, selected_month, selected_day;

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
    public void getKeywords(ShowArticleActivity.section selected_section, int selected_year, int selected_month, int selected_day){

        // 서버와 http protocol을 이용하여 사용자가 선택한 분야/날짜의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        class GetKeywords extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String section = (String)params[0];
                String date = (String) params[1];

                String serverURL = "http://115.71.236.22/get_keyword.php";
                String postParameters = "section=" + section + "&date=" + date;


                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    //httpURLConnection.setRequestProperty("content-type", "application/json");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(postParameters.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    }
                    else{
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result){
                try {
                    ArrayList<Entry> yvalues = new ArrayList<Entry>();
                    ArrayList<String> xvalues = new ArrayList<String>();

                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray news_arr = jsonObj.getJSONArray("result");

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
                    // Default value

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
        }

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

        GetKeywords GetKeywords_Task = new GetKeywords();
        GetKeywords_Task.execute(section, date);
    }
}
