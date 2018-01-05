package com.lunaticlemon.lifecast.show_article;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.member.LoginActivity;

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

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

public class ShowArticleActivity extends AppCompatActivity implements YahooWeatherInfoListener {

    DatePickerDialog datePickerDialog;
    YahooWeather mYahooWeather = YahooWeather.getInstance(5000, true);

    TextView textView_date, textView_weather;
    TextView textView_politic, textView_economy, textView_society, textView_sport;
    TextView textView_entertainment, textView_world, textView_culture, textView_science;

    ListView listview_news;
    News_Adapter news_adapter;

    enum section {POLITIC, ECONOMY, SOCIETY, SPORT, ENTERTAINMENT, WORLD, CULTURE, SCIENCE};
    section cur_selected_section;   // 사용자가 현재 선택한 분야
    int selected_year, selected_month, selected_day;   // 사용자가 현재 선택한 날짜

    String nickname, gender, birthday, city, created, preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);

        nickname = getIntent().getExtras().getString("nickname");
        gender = getIntent().getExtras().getString("gender");
        birthday = getIntent().getExtras().getString("birthday");
        city = getIntent().getExtras().getString("city");
        created = getIntent().getExtras().getString("created");
        preference = getIntent().getExtras().getString("preference");

        // 사용자의 선호 분야를 가장먼저 보여주기 위해 section 초기화
        switch(preference)
        {
            case "politic":
                cur_selected_section = section.POLITIC;
                break;
            case "economy":
                cur_selected_section = section.ECONOMY;
                break;
            case "society":
                cur_selected_section = section.SOCIETY;
                break;
            case "sport":
                cur_selected_section = section.SPORT;
                break;
            case "entertainment":
                cur_selected_section = section.ENTERTAINMENT;
                break;
            case "world":
                cur_selected_section = section.WORLD;
                break;
            case "culture":
                cur_selected_section = section.CULTURE;
                break;
            case "science":
                cur_selected_section = section.SCIENCE;
                break;
            default:
            cur_selected_section = section.POLITIC;
                break;
        }

        textView_date = (TextView)findViewById(R.id.textView_date);
        textView_weather = (TextView)findViewById(R.id.textView_weather);

        textView_politic = (TextView)findViewById(R.id.textView_politic);
        textView_politic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.POLITIC);
            }
        });

        textView_economy = (TextView)findViewById(R.id.textView_economy);
        textView_economy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.ECONOMY);
            }
        });

        textView_society = (TextView)findViewById(R.id.textView_society);
        textView_society.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.SOCIETY);
            }
        });

        textView_sport = (TextView)findViewById(R.id.textView_sport);
        textView_sport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.SPORT);
            }
        });

        textView_entertainment = (TextView)findViewById(R.id.textView_entertainment);
        textView_entertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.ENTERTAINMENT);
            }
        });

        textView_world = (TextView)findViewById(R.id.textView_world);
        textView_world.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.WORLD);
            }
        });

        textView_culture = (TextView)findViewById(R.id.textView_culture);
        textView_culture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.CULTURE);
            }
        });

        textView_science = (TextView)findViewById(R.id.textView_science);
        textView_science.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(section.SCIENCE);

            }
        });


        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 사용자가 선택한 날짜 표시
                textView_date.setText(year + " / " + monthOfYear+1 + " / " + dayOfMonth);

                // 사용자가 선택한 날짜 변경
                selected_year = year;
                selected_month = monthOfYear+1;
                selected_day = dayOfMonth;

                // 해당 날짜의 뉴스 가져옴
                getNews(cur_selected_section, selected_year, selected_month, selected_day);
            }

        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        // 사용자의 선호분야 굵게 표시
        switch(cur_selected_section)
        {
            case POLITIC:
                textView_politic.setTextColor(0xFF000000);
                textView_politic.setTypeface(textView_politic.getTypeface(), Typeface.BOLD);
                break;
            case ECONOMY:
                textView_economy.setTextColor(0xFF000000);
                textView_economy.setTypeface(textView_economy.getTypeface(), Typeface.BOLD);
                break;
            case SOCIETY:
                textView_society.setTextColor(0xFF000000);
                textView_society.setTypeface(textView_society.getTypeface(), Typeface.BOLD);
                break;
            case SPORT:
                textView_sport.setTextColor(0xFF000000);
                textView_sport.setTypeface(textView_sport.getTypeface(), Typeface.BOLD);
                break;
            case ENTERTAINMENT:
                textView_entertainment.setTextColor(0xFF000000);
                textView_entertainment.setTypeface(textView_entertainment.getTypeface(), Typeface.BOLD);
                break;
            case WORLD:
                textView_world.setTextColor(0xFF000000);
                textView_world.setTypeface(textView_world.getTypeface(), Typeface.BOLD);
                break;
            case CULTURE:
                textView_culture.setTextColor(0xFF000000);
                textView_culture.setTypeface(textView_culture.getTypeface(), Typeface.BOLD);
                break;
            case SCIENCE:
                textView_science.setTextColor(0xFF000000);
                textView_science.setTypeface(textView_science.getTypeface(), Typeface.BOLD);
                break;
        }

        // 오늘 날짜와 현재 날씨로 초기화
        textView_date.setText(Calendar.getInstance().get(Calendar.YEAR) + " / " + Calendar.getInstance().get(Calendar.MONTH)+1 + " / " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        this.searchByGPS();

        listview_news = (ListView) findViewById(R.id.listViewNews);
        news_adapter = new News_Adapter();

        listview_news.setAdapter(news_adapter);
        listview_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                News news = (News)listview_news.getItemAtPosition(position);

                Intent intent = new Intent(ShowArticleActivity.this, WebViewActivity.class);
                intent.putExtra("url","http://" + news.getUrl());
                startActivity(intent);
                //redirectUsingCustomTab(news.getUrl());
            }
        });


        getNews(cur_selected_section, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)+1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.actionbar, menu);

        // action bar에 검색창 생성
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case R.id.profile_btn:
                // TODO
                return true;
            case R.id.setting_btn:
                // TODO
                return true;
            case R.id.logout_btn:
                setResult(LoginActivity.result_logout);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("예",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton)
            {
                setResult(LoginActivity.result_finish);
                finish();
            }
        });

        builder.setNegativeButton("아니오",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton)
            {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // weatherInfo object contains all information returned by Yahoo Weather API
    // if `weatherInfo` is null, you can get the error from `errorType`
    @Override
    public void gotWeatherInfo(final WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if(weatherInfo != null)
        {
            // 현재 gps 위치의 날씨와 온도를 표시
            textView_weather.setText(changeWeatherCodeToKorean(weatherInfo.getCurrentCode()) + "\n" + weatherInfo.getCurrentTemp() + "\u00b0" + "C" );

        }
        else
        {
            textView_weather.setText("GPS off");
        }
    }

    // 날씨정보 불러오는 함수
    // https://developer.yahoo.com/weather/documentation.html
    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(false);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(getApplicationContext(), this);
    }

    private void setTextViewHighlight(section selected_section)
    {
        // 사용자가 전에 선택했었던 section 표시 변경
        switch(cur_selected_section)
        {
            case POLITIC:
                textView_politic.setTextColor(0x66000000);
                textView_politic.setTypeface(textView_politic.getTypeface(), Typeface.NORMAL);
                break;
            case ECONOMY:
                textView_economy.setTextColor(0x66000000);
                textView_economy.setTypeface(textView_economy.getTypeface(), Typeface.NORMAL);
                break;
            case SOCIETY:
                textView_society.setTextColor(0x66000000);
                textView_society.setTypeface(textView_society.getTypeface(), Typeface.NORMAL);
                break;
            case SPORT:
                textView_sport.setTextColor(0x66000000);
                textView_sport.setTypeface(textView_sport.getTypeface(), Typeface.NORMAL);
                break;
            case ENTERTAINMENT:
                textView_entertainment.setTextColor(0x66000000);
                textView_entertainment.setTypeface(textView_entertainment.getTypeface(), Typeface.NORMAL);
                break;
            case WORLD:
                textView_world.setTextColor(0x66000000);
                textView_world.setTypeface(textView_world.getTypeface(), Typeface.NORMAL);
                break;
            case CULTURE:
                textView_culture.setTextColor(0x66000000);
                textView_culture.setTypeface(textView_culture.getTypeface(), Typeface.NORMAL);
                break;
            case SCIENCE:
                textView_science.setTextColor(0x66000000);
                textView_science.setTypeface(textView_science.getTypeface(), Typeface.NORMAL);
                break;
        }

        // 사용자가 선택한 section 표시 변경
        switch(selected_section)
        {
            case POLITIC:
                textView_politic.setTextColor(0xFF000000);
                textView_politic.setTypeface(textView_politic.getTypeface(), Typeface.BOLD);
                break;
            case ECONOMY:
                textView_economy.setTextColor(0xFF000000);
                textView_economy.setTypeface(textView_economy.getTypeface(), Typeface.BOLD);
                break;
            case SOCIETY:
                textView_society.setTextColor(0xFF000000);
                textView_society.setTypeface(textView_society.getTypeface(), Typeface.BOLD);
                break;
            case SPORT:
                textView_sport.setTextColor(0xFF000000);
                textView_sport.setTypeface(textView_sport.getTypeface(), Typeface.BOLD);
                break;
            case ENTERTAINMENT:
                textView_entertainment.setTextColor(0xFF000000);
                textView_entertainment.setTypeface(textView_entertainment.getTypeface(), Typeface.BOLD);
                break;
            case WORLD:
                textView_world.setTextColor(0xFF000000);
                textView_world.setTypeface(textView_world.getTypeface(), Typeface.BOLD);
                break;
            case CULTURE:
                textView_culture.setTextColor(0xFF000000);
                textView_culture.setTypeface(textView_culture.getTypeface(), Typeface.BOLD);
                break;
            case SCIENCE:
                textView_science.setTextColor(0xFF000000);
                textView_science.setTypeface(textView_science.getTypeface(), Typeface.BOLD);
                break;
        }

        // 사용자가 선택한 분야 변경
        cur_selected_section = selected_section;

        // 사용자가 선택한 분야의 뉴스 가져옴
        getNews(cur_selected_section, selected_year, selected_month, selected_day);
    }

    // 사용자가 선택한 뉴스 url 열어줌
    private void redirectUsingCustomTab(String url)
    {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse("http://" + url));
    }

    // 달력 클릭 시
    public void onClickSearchDate(View v)
    {
        datePickerDialog.show();
    }

    // 새로고침 클릭 시
    public void onClickRefreshWeather(View v)
    {
        searchByGPS();
    }

    // 날짜 코드를 날짜로 변경해주는 함수
    public String changeWeatherCodeToKorean(int code)
    {
        // detail code information
        // https://developer.yahoo.com/weather/documentation.html#response
        switch(code)
        {
            case 0:
                return "토네이도";
            case 1:
                return "열대폭풍";
            case 2:
                return "허리케인";
            case 3:
                return "번개, 비";
            case 4:
                return "번개, 비";
            case 5:
                return "눈, 비";
            case 6:
                return "진눈깨비";
            case 7:
                return "진눈깨비";
            case 8:
                return "이슬비";
            case 9:
                return "이슬비";
            case 10:
                return "폭우";
            case 11:
                return "소나기";
            case 12:
                return "소나기";
            case 13:
                return "소나기성 눈";
            case 14:
                return "소나기성 눈";
            case 15:
                return "폭설";
            case 16:
                return "눈";
            case 17:
                return "우박";
            case 18:
                return "진눈깨비";
            case 19:
                return "먼지";
            case 20:
                return "안개";
            case 21:
                return "안개";
            case 22:
                return "안개";
            case 23:
                return "거센 바람";
            case 24:
                return "바람";
            case 25:
                return "추움";
            case 26:
                return "흐림";
            case 27:
                return "흐림";
            case 28:
                return "흐림";
            case 29:
                return "흐림";
            case 30:
                return "흐림";
            case 31:
                return "맑음";
            case 32:
                return "화창함";
            case 33:
                return "갬";
            case 34:
                return "갬";
            case 35:
                return "진눈깨비";
            case 36:
                return "더위";
            case 37:
                return "뇌우";
            case 38:
                return "뇌우";
            case 39:
                return "뇌우";
            case 40:
                return "뇌우";
            case 41:
                return "폭설";
            case 42:
                return "소나기성 눈";
            case 43:
                return "폭설";
            case 44:
                return "흐림";
            case 45:
                return "뇌우";
            case 46:
                return "눈";
            case 47:
                return "뇌우";
            default:
                return "맑음";
        }
    }

    // 서버에서 news를 가져와 listview에 넣어주는 함수
    public void getNews(section selected_section, int selected_year, int selected_month, int selected_day){

        // 서버와 http protocol을 이용하여 사용자가 선택한 분야/날짜의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        class GetNewsData extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {

                String section = (String)params[0];
                String date = (String) params[1];

                String serverURL = "http://115.71.236.22/get_news.php";
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
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray news_arr = jsonObj.getJSONArray("result");

                    Log.d("showactivity",Integer.toString(news_arr.length()));

                    // 그 전 날짜의 데이터를 지워줌
                    news_adapter.init();

                    for(int i=0;i<news_arr.length();i++){
                        JSONObject news = news_arr.getJSONObject(i);
                        String keyword = news.getString("keyword");
                        String url = news.getString("url");
                        String title = news.getString("title");
                        String date = news.getString("date");
                        String newspaper = news.getString("newspaper");
                        int view = news.getInt("view");


                        news_adapter.addItem(keyword, url, title, date, newspaper, view);
                    }

                    news_adapter.notifyDataSetChanged();

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
            case ENTERTAINMENT:
                section = "entertainment";
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

        Log.d("showactivity",date.toString());
        GetNewsData GetNewsData_Task = new GetNewsData();
        GetNewsData_Task.execute(section, date);
    }
}
