package com.lunaticlemon.lifecast.show_article;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chabbal.slidingdotsplash.OnItemClickListener;
import com.chabbal.slidingdotsplash.SlidingSplashView;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.camera.CameraActivity;
import com.lunaticlemon.lifecast.minigame.MiniGameActivity;
import com.lunaticlemon.lifecast.camera.PictureActivity;
import com.lunaticlemon.lifecast.option_menu.NewsBucketActivity;
import com.lunaticlemon.lifecast.option_menu.ProfileActivity;
import com.lunaticlemon.lifecast.paint.PaintActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    public static int request_showarticle = 3001, request_speach = 3002;
    public static int result_nickchange = 4001, result_pwchange = 4002;
    public enum section {POLITIC, ECONOMY, SOCIETY, SPORT, WORLD, CULTURE, SCIENCE}

    String TAG = "ShowArticle";

    // http request queue
    RequestQueue volley_queue;

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
    int user_number;
    String id, nickname, gender, birthday, city, created, preference;

    Keyword_dialog keyword_dialog;
    Statistic_dialog statistic_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);

        volley_queue = Volley.newRequestQueue(this);

        user_number = getIntent().getExtras().getInt("number");
        id = getIntent().getExtras().getString("id");
        nickname = getIntent().getExtras().getString("nickname");
        gender = getIntent().getExtras().getString("gender");
        birthday = getIntent().getExtras().getString("birthday");
        city = getIntent().getExtras().getString("city");
        created = getIntent().getExtras().getString("created");
        preference = getIntent().getExtras().getString("preference");

        // 사용자 접속 사실 nosql에 저장
        save_log("in", id);

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
                textView_date.setText(year + " / " + Integer.toString(monthOfYear+1) + " / " + dayOfMonth);

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
        textView_date.setText(Calendar.getInstance().get(Calendar.YEAR) + " / " + Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1) + " / " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
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
                        statistic_dialog = new Statistic_dialog(ShowArticleActivity.this, cur_selected_section, selected_year, selected_month, selected_day, id, gender, birthday, city);
                        statistic_dialog.show();
                        break;
                    case 2: // 지역 뉴스 보여주기
                        Intent intent = new Intent(ShowArticleActivity.this, MapActivity.class);
                        intent.putExtra("user_id", id);
                        intent.putExtra("gender", gender);
                        intent.putExtra("birthday", birthday);
                        intent.putExtra("city",city);
                        intent.putExtra("selected_year",selected_year);
                        intent.putExtra("selected_month",selected_month);
                        intent.putExtra("selected_day",selected_day);
                        startActivity(intent);
                        break;
                }
            }
        });



        listview_news = (ListView) findViewById(R.id.listViewNews);
        news_adapter = new News_Adapter();

        listview_news.setAdapter(news_adapter);
        listview_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long _id) {
                News news = (News)listview_news.getItemAtPosition(position);

                // web view에 해당 url 보여줌
                Intent intent = new Intent(ShowArticleActivity.this, WebViewActivity.class);
                switch(cur_selected_section)
                {
                    case POLITIC:
                        intent.putExtra("section", "politic");
                        break;
                    case ECONOMY:
                        intent.putExtra("section", "economy");
                        break;
                    case SOCIETY:
                        intent.putExtra("section", "society");
                        break;
                    case SPORT:
                        intent.putExtra("section", "sport");
                        break;
                    case WORLD:
                        intent.putExtra("section", "world");
                        break;
                    case CULTURE:
                        intent.putExtra("section", "culture");
                        break;
                    case SCIENCE:
                        intent.putExtra("section", "science");
                        break;
                    default:
                        intent.putExtra("section", "politic");
                        break;
                }
                intent.putExtra("url", news.getUrl());
                intent.putExtra("news_id", news.getId());
                intent.putExtra("user_id", id);
                intent.putExtra("gender", gender);
                intent.putExtra("birthday", birthday);
                intent.putExtra("city", city);
                startActivity(intent);

                //redirectUsingCustomTab(news.getUrl());
            }
        });
        listview_news.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                final News news = (News)listview_news.getItemAtPosition(position);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowArticleActivity.this);

                // set dialog message
                alertDialogBuilder
                        .setMessage("해당 기사를 보관함에 추가하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("예",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                addBucket(cur_selected_section, user_number, news.getId());
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("아니요",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
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
        MenuItem searchMenuItem = menu.findItem(R.id.text_search);
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
            case R.id.vocal_search:
                promptSpeechInput();
                return true;
            case R.id.profile_btn:
                Intent intent_profile = new Intent(ShowArticleActivity.this, ProfileActivity.class);
                intent_profile.putExtra("id",id);
                intent_profile.putExtra("nickname",nickname);
                intent_profile.putExtra("gender",gender);
                intent_profile.putExtra("birthday",birthday);
                intent_profile.putExtra("city",city);
                intent_profile.putExtra("created",created);
                startActivityForResult(intent_profile,request_showarticle);
                return true;
            case R.id.bucket_btn:
                Intent intent_bucket = new Intent(ShowArticleActivity.this, NewsBucketActivity.class);
                intent_bucket.putExtra("number",user_number);
                startActivity(intent_bucket);
                return true;
            case R.id.logout_btn:
                setResult(result_logout);
                finish();
                return true;
            case R.id.recognize_btn:
                Intent intent_recognize= new Intent(ShowArticleActivity.this, CameraActivity.class);
                startActivity(intent_recognize);
                return true;
            case R.id.text_btn:
                Intent intent_text= new Intent(ShowArticleActivity.this, PaintActivity.class);
                startActivity(intent_text);
                return true;
            case R.id.camera_btn:
                Intent intent_camera= new Intent(ShowArticleActivity.this, PictureActivity.class);
                startActivity(intent_camera);
                return true;
            case R.id.minigame_btn:
                Intent intent_minigame= new Intent(ShowArticleActivity.this, MiniGameActivity.class);
                startActivity(intent_minigame);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // 사용자 접속 사실 nosql에 저장
        save_log("out", id);
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
            if (resultCode == result_withdraw)
            {
                // ProfileActivity에서 회원 탈퇴 시
                setResult(result_withdraw);
                finish();
            }
            else if (resultCode == result_nickchange)
            {
                // ProfileActivity에서 nickname 변경 시
                nickname = data.getExtras().getString("nick");
            }
            else if (resultCode == result_pwchange)
            {
                // ProfileActivity에서 pw 변경 시
                Toast.makeText(ShowArticleActivity.this, "새 비밀번호로 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                setResult(result_logout);
                finish();
            }
        }
        else if(requestCode == request_speach) {
            if (data != null) {
                // 음성 검색 결과를 제목 또는 키워드에 포함하는 기사 검색
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                listview_news.setFilterText(result.get(0)) ;
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

    // google speech api
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "검색어를 말해주세요.");
        try {
            startActivityForResult(intent, request_speach);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "검색을 지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
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

    /*
    // 사용자가 선택한 뉴스 url 열어줌
    private void redirectUsingCustomTab(String url)
    {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse("http://" + url));
    }*/

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
    public void getNews(final section selected_section, final int selected_year, final int selected_month, final int selected_day){

        // 서버와 http protocol을 이용하여 사용자가 선택한 분야/날짜의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        String url = "http://115.71.236.22/get_news.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // end of listview page
                            if(response.equals("false"))
                            {
                                is_page_end = true;
                            }
                            else {
                                is_page_end = false;
                                JSONObject jsonObj = new JSONObject(response);
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


                                    news_adapter.addItem(id, keyword, url, title, date, newspaper, view, "null");
                                }

                                news_adapter.notifyDataSetChanged();
                                if (listview_page == 1)
                                    listview_news.smoothScrollToPosition( 0 );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                String page = Integer.toString(listview_page);

                Map<String, String>  params = new HashMap<String, String>();
                params.put("section", section);
                params.put("date", date);
                params.put("page", page);
                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }

    public void addBucket(final section selected_section, final int user_number, final int news_id){

        // 서버와 http protocol을 이용하여 사용자가 선택한 뉴스를 사용자의 보관함에 넣음
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자의 number
        // 3rd parameter : 뉴스의 id
        // 4th parameter : 보관함에 담을 시 insert / 보관함에서 삭제 시 delete
        // response : (success : 담기 성공 / fail : 담기 실패)
        String url = "http://115.71.236.22/add_bucket.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("success"))
                        {
                            Toast.makeText(ShowArticleActivity.this, "보관함 담기 성공", Toast.LENGTH_SHORT).show();
                        }
                        else if(response.equals("fail"))
                        {
                            Toast.makeText(ShowArticleActivity.this, "보관함 담기 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ShowArticleActivity.this, "보관함 담기 실패", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
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
                params.put("user_number", Integer.toString(user_number));
                params.put("news_id", Integer.toString(news_id));
                params.put("action", "insert");

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }

    public void save_log(final String action, final String id){

        // 서버와 http protocol을 이용하여 사용자의 접속 기록을 log로 남김
        // 1st parameter : in (접속 시) or out (종료 시)
        // 2nd parameter : 사용자의 id
        String url = "http://115.71.236.22/save_log.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("action", action);
                params.put("id", id);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}
