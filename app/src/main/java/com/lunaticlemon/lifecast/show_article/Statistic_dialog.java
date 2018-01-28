package com.lunaticlemon.lifecast.show_article;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

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
import java.util.Calendar;

/**
 * Created by lemon on 2018-01-19.
 */

public class Statistic_dialog extends Dialog {
    private Context con;

    // 사용자가 선택한 분야, 날짜 및 사용자의 나이, 성별, 도시 정보
    private ShowArticleActivity.section selected_section;
    private int selected_year, selected_month, selected_day, selected_age;
    private String  selected_gender, selected_birthday, selected_city;

    private Spinner spinner_gender, spinner_age, spinner_city;


    // spinner data
    private String[] arr_gender = {"남성", "여성", "선택안함"};
    private String[] arr_age =  {"10대", "20대", "30대", "40대", "50대", "60대 이상", "선택안함"};
    private String[] arr_city = {
            "서울","부산","대구","인천","광주","대전","울산","경기",
            "강원","충북","충남","전북","전남","경북","경남","제주", "선택안함"
    };

    ListView listview_news;
    News_Adapter news_adapter;

    public Statistic_dialog(@NonNull Context context, ShowArticleActivity.section _section, int _year, int _month, int _day, String gender, String birthday, String city) {
        super(context);

        con = context;
        selected_section = _section;
        selected_year = _year;
        selected_month = _month;
        selected_day = _day;
        selected_gender = gender;
        selected_birthday = birthday;
        selected_city = city;

        // 나이 구하기
        Calendar current = Calendar.getInstance();
        int currentYear  = current.get(Calendar.YEAR);
        int user_birthYear = Integer.parseInt(birthday.substring(0,4));
        selected_age = currentYear - user_birthYear + 1;

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

        setContentView(R.layout.statistic_dialog);

        spinner_gender = (Spinner) findViewById(R.id.spinner_gender);
        spinner_age = (Spinner) findViewById(R.id.spinner_age);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);

        ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_gender);
        ArrayAdapter<String> adapter_age = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_age);
        ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_city);

        spinner_gender.setAdapter(adapter_gender);
        switch(selected_gender)
        {
            case "male":
                spinner_gender.setSelection(0);
                break;
            case "female":
                spinner_gender.setSelection(1);
                break;
        }

        spinner_age.setAdapter(adapter_age);
        if(selected_age >= 10 && selected_age < 20)
        {
            spinner_age.setSelection(0);
        }
        else if(selected_age >= 20 && selected_age < 30)
        {
            spinner_age.setSelection(1);
        }
        else if(selected_age >= 30 && selected_age < 40)
        {
            spinner_age.setSelection(2);
        }
        else if(selected_age >= 40 && selected_age < 50)
        {
            spinner_age.setSelection(3);
        }
        else if(selected_age >= 50 && selected_age < 60)
        {
            spinner_age.setSelection(4);
        }
        else if(selected_age >= 60)
        {
            spinner_age.setSelection(5);
        }
        else
        {
            spinner_age.setSelection(6);
        }

        spinner_city.setAdapter(adapter_city);
        for(int i = 0;i<16;i++)
        {
            if(selected_city.equals(arr_city[i])) {
                spinner_city.setSelection(i);
                break;
            }
        }

        spinner_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getNews(selected_section, selected_year, selected_month, selected_day, spinner_gender.getSelectedItem().toString(), spinner_age.getSelectedItem().toString(), spinner_city.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_age.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getNews(selected_section, selected_year, selected_month, selected_day, spinner_gender.getSelectedItem().toString(), spinner_age.getSelectedItem().toString(), spinner_city.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getNews(selected_section, selected_year, selected_month, selected_day, spinner_gender.getSelectedItem().toString(), spinner_age.getSelectedItem().toString(), spinner_city.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        listview_news = (ListView) findViewById(R.id.listViewNews);
        news_adapter = new News_Adapter();

        listview_news.setAdapter(news_adapter);
        listview_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                News news = (News)listview_news.getItemAtPosition(position);

                // web view에 해당 url 보여줌
                Intent intent = new Intent(con, WebViewActivity.class);
                intent.putExtra("url","http://" + news.getUrl());
                con.startActivity(intent);


                addView(selected_section, news.getUrl(), selected_gender, selected_birthday, selected_city);
                //redirectUsingCustomTab(news.getUrl());
            }
        });


        getNews(selected_section, selected_year, selected_month, selected_day, spinner_gender.getSelectedItem().toString(), spinner_age.getSelectedItem().toString(), spinner_city.getSelectedItem().toString());
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

    // 서버에서 해당 분야, 날짜에 사용자가 선택한 성별, 나이, 도시의 많이 본 뉴스를 가져옴
    public void getNews(ShowArticleActivity.section selected_section, int selected_year, int selected_month, int selected_day, String gender, String age, String city){

        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        class GetNews extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String section = (String)params[0];
                String date = (String) params[1];
                String selected_gender = (String) params[2];
                String selected_age = (String) params[3];
                String selected_city = (String) params[4];

                String serverURL = "http://115.71.236.22/get_statistic.php";
                String postParameters = "section=" + section + "&date=" + date + "&gender=" + selected_gender + "&age=" + selected_age + "&city=" + selected_city;


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
                    news_adapter.init();

                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray news_arr = jsonObj.getJSONArray("result");

                    for (int i = 0; i < news_arr.length(); i++) {
                        JSONObject news = news_arr.getJSONObject(i);
                        int id = news.getInt("id");
                        String keyword = news.getString("keyword");
                        String url = news.getString("url");
                        String title = news.getString("title");
                        String date = news.getString("date");
                        String newspaper = news.getString("newspaper");
                        int view = news.getInt("view");


                        news_adapter.addItem(id, keyword, url, title, date, newspaper, view, "null");
                    }

                    news_adapter.notifyDataSetChanged();
                    listview_news.smoothScrollToPosition( 0 );
                } catch (JSONException e) {
                    e.printStackTrace();
                    news_adapter.notifyDataSetChanged();
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

        switch(gender)
        {
            case "남성":
                gender = "male";
                break;
            case "여성":
                gender = "female";
                break;
            case "선택안함":
                gender = "none";
                break;
        }

        switch(age)
        {
            case "10대":
                age = "10";
                break;
            case "20대":
                age = "20";
                break;
            case "30대":
                age = "30";
                break;
            case "40대":
                age = "40";
                break;
            case "50대":
                age = "50";
                break;
            case "60대 이상":
                age = "60";
                break;
            case "선택안함":
                age = "none";
                break;

        }

        if(city.equals("선택안함"))
            city = "none";


        GetNews GetNews_Task = new GetNews();
        GetNews_Task.execute(section, date, gender, age, city);
    }

    public void addView(ShowArticleActivity.section selected_section, String _url, String _gender, String _birthday, String _city){

        // 서버와 http protocol을 이용하여 사용자가 선택한 뉴스의 조회수 증가
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 기사의 url
        // 3rd parameter : 사용자의 성별
        // 4th parameter : 사용자의 생일
        // 5th parameter : 사용자의 도시
        class AddViewData extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {

                String section = (String)params[0];
                String _url = (String)params[1];
                String gender = (String)params[2];
                String birthday = (String)params[3];
                String city = (String)params[4];

                String serverURL = "http://115.71.236.22/add_view.php";
                String postParameters = "section=" + section + "&url=" + _url +"&gender=" + gender + "&birthday=" + birthday + "&city=" + city;

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
                    String line = null;

                    while((line = bufferedReader.readLine()) != null){
                        sb.append(line);
                    }

                    bufferedReader.close();
                    return sb.toString();


                } catch (Exception e) {
                    return new String("add view Error: " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String result){
            }
        }

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

        AddViewData AddView_Task = new AddViewData();
        AddView_Task.execute(section, _url, _gender, _birthday, _city);
    }
}