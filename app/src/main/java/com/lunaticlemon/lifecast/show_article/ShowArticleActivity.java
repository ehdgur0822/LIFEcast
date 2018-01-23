package com.lunaticlemon.lifecast.show_article;

import android.app.DatePickerDialog;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chabbal.slidingdotsplash.OnItemClickListener;
import com.chabbal.slidingdotsplash.SlidingSplashView;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.option_menu.ProfileActivity;

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

import static com.lunaticlemon.lifecast.member.LoginActivity.result_finish;
import static com.lunaticlemon.lifecast.member.LoginActivity.result_logout;
import static com.lunaticlemon.lifecast.member.LoginActivity.result_withdraw;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.CULTURE;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.ECONOMY;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.POLITIC;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SCIENCE;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SOCIETY;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SPORT;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.WORLD;

public class ShowArticleActivity extends AppCompatActivity implements YahooWeatherInfoListener {

    public static int request_showarticle = 3001;
    public static int result_nickchange = 4001, result_pwchange = 4002;
    public enum section {POLITIC, ECONOMY, SOCIETY, SPORT, WORLD, CULTURE, SCIENCE};

    DatePickerDialog datePickerDialog;
    YahooWeather mYahooWeather = YahooWeather.getInstance(5000, true);

    TextView textView_date, textView_weather;
    TextView textView_politic, textView_economy, textView_society, textView_sport;
    TextView textView_world, textView_culture, textView_science;

    SlidingSplashView slidingView;

    ListView listview_news;
    News_Adapter news_adapter;

    private int listview_page = 1;
    private boolean is_page_end = false;
    boolean lastitemVisibleFlag = false;


    section cur_selected_section;   // 사용자가 현재 선택한 분야
    int selected_year, selected_month, selected_day;   // 사용자가 현재 선택한 날짜

    // 사용자 정보
    int number;
    String id, nickname, gender, birthday, city, created, preference;

    Keyword_dialog keyword_dialog;
    Statistic_dialog statistic_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);

        number = getIntent().getExtras().getInt("number");
        id = getIntent().getExtras().getString("id");
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
                cur_selected_section = POLITIC;
                break;
            case "economy":
                cur_selected_section = ECONOMY;
                break;
            case "society":
                cur_selected_section = SOCIETY;
                break;
            case "sport":
                cur_selected_section = SPORT;
                break;
            case "world":
                cur_selected_section = WORLD;
                break;
            case "culture":
                cur_selected_section = CULTURE;
                break;
            case "science":
                cur_selected_section = SCIENCE;
                break;
            default:
                cur_selected_section = POLITIC;
                break;
        }

        // 선택 날짜를 오늘로 초기화
        selected_year = Calendar.getInstance().get(Calendar.YEAR);
        selected_month = Calendar.getInstance().get(Calendar.MONTH)+1;
        selected_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);


        textView_date = (TextView)findViewById(R.id.textView_date);
        textView_weather = (TextView)findViewById(R.id.textView_weather);

        textView_politic = (TextView)findViewById(R.id.textView_politic);
        textView_politic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(POLITIC);
            }
        });

        textView_economy = (TextView)findViewById(R.id.textView_economy);
        textView_economy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(ECONOMY);
            }
        });

        textView_society = (TextView)findViewById(R.id.textView_society);
        textView_society.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(SOCIETY);
            }
        });

        textView_sport = (TextView)findViewById(R.id.textView_sport);
        textView_sport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(SPORT);
            }
        });

        textView_world = (TextView)findViewById(R.id.textView_world);
        textView_world.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(WORLD);
            }
        });

        textView_culture = (TextView)findViewById(R.id.textView_culture);
        textView_culture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(CULTURE);
            }
        });

        textView_science = (TextView)findViewById(R.id.textView_science);
        textView_science.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTextViewHighlight(SCIENCE);

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



        slidingView = (SlidingSplashView) findViewById(R.id.splash_img);
        slidingView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onPagerItemClick(View view, int position) {
                //Toast.makeText(ShowArticleActivity.this, Integer.toString(position), Toast.LENGTH_SHORT).show();
                switch(position)
                {
                    case 0: // 사용자가 선택한 날짜, 분야의 키워드 차트 보여주기
                        keyword_dialog = new Keyword_dialog(ShowArticleActivity.this, cur_selected_section, selected_year, selected_month, selected_day);
                        keyword_dialog.show();
                        break;
                    case 1: // 사용자가 선택한 성별, 나이, 도시에서 인기있는 뉴스 보여주기
                        statistic_dialog = new Statistic_dialog(ShowArticleActivity.this, cur_selected_section, selected_year, selected_month, selected_day, gender, birthday, city);
                        statistic_dialog.show();
                        break;
                    case 2: // 지역 뉴스 보여주기
                        startActivity(new Intent(getApplication(), MapActivity.class));
                        break;
                    case 3:
                        break;
                }
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
                Intent intent = new Intent(ShowArticleActivity.this, WebViewActivity.class);
                intent.putExtra("url","http://" + news.getUrl());
                startActivity(intent);

                addView(cur_selected_section, news.getUrl(), gender, birthday, city);
                //redirectUsingCustomTab(news.getUrl());
            }
        });

        listview_news.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastitemVisibleFlag && !is_page_end) {
                    listview_page++;
                    getNews(cur_selected_section, selected_year, selected_month, selected_day);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, final int totalItemCount) {
                if(!is_page_end)
                    lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });

        // listview 내용 설정
        getNews(cur_selected_section, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)+1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.actionbar, menu);

        // action bar에 검색창 생성
//        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
//        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));


        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("검색어 입력");
            searchView.setMaxWidth(2129960); // https://stackoverflow.com/questions/18063103/searchview-in-optionsmenu-not-full-width
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (s.length() > 0) {
                        listview_news.setFilterText(s) ;
                    } else {
                        listview_news.clearTextFilter() ;
                    }
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case R.id.profile_btn:
                Intent intent = new Intent(ShowArticleActivity.this, ProfileActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("nickname",nickname);
                intent.putExtra("gender",gender);
                intent.putExtra("birthday",birthday);
                intent.putExtra("city",city);
                intent.putExtra("created",created);
                startActivityForResult(intent,request_showarticle);
                return true;
            case R.id.setting_btn:
                // TODO
                // 보관함 추가?
                return true;
            case R.id.logout_btn:
                setResult(result_logout);
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
                setResult(result_finish);
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

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == request_showarticle) {
            if (resultCode == result_withdraw)    // exit
            {
                setResult(result_withdraw);
                finish();
            }
            else if (resultCode == result_nickchange)
            {
                nickname = data.getExtras().getString("nick");
            }
            else if (resultCode == result_pwchange)
            {
                Toast.makeText(ShowArticleActivity.this, "새 비밀번호로 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                setResult(result_logout);
                finish();
            }
        }
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
            // gps 사용 꺼져있을 경우
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

        // listview page 초기화
        listview_page = 1;

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
                String page = Integer.toString(listview_page);

                String serverURL = "http://115.71.236.22/get_news.php";
                String postParameters = "section=" + section + "&date=" + date +"&page=" + page;


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
                    // end of listview page
                    if(result.equals("false"))
                    {
                        is_page_end = true;
                    }
                    else {
                        is_page_end = false;
                        JSONObject jsonObj = new JSONObject(result);
                        JSONArray news_arr = jsonObj.getJSONArray("result");

                        Log.d("showactivity", Integer.toString(news_arr.length()));

                        // 그 전 날짜의 데이터를 지워줌
                        if (listview_page == 1)
                            news_adapter.init();

                        for (int i = 0; i < news_arr.length(); i++) {
                            JSONObject news = news_arr.getJSONObject(i);
                            int id = news.getInt("id");
                            String keyword = news.getString("keyword");
                            String url = news.getString("url");
                            String title = news.getString("title");
                            String date = news.getString("date");
                            String newspaper = news.getString("newspaper");
                            int view = news.getInt("view");


                            news_adapter.addItem(id, keyword, url, title, date, newspaper, view);
                        }

                        news_adapter.notifyDataSetChanged();
                        if (listview_page == 1)
                            listview_news.smoothScrollToPosition( 0 );
                    }
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

        Log.d("showactivity",date.toString());
        GetNewsData GetNewsData_Task = new GetNewsData();
        GetNewsData_Task.execute(section, date);
    }

    public void addView(section selected_section, String _url, String _gender, String _birthday, String _city){

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
