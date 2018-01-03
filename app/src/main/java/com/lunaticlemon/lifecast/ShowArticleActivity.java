package com.lunaticlemon.lifecast;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.lunaticlemon.lifecast.member.LoginActivity;

import java.util.Calendar;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

import static com.lunaticlemon.lifecast.ShowArticleActivity.section.ECONOMY;
import static com.lunaticlemon.lifecast.ShowArticleActivity.section.SOCIETY;
import static com.lunaticlemon.lifecast.ShowArticleActivity.section.SPORT;

public class ShowArticleActivity extends AppCompatActivity implements YahooWeatherInfoListener {

    DatePickerDialog datePickerDialog;
    YahooWeather mYahooWeather = YahooWeather.getInstance(5000, true);

    TextView textView_date, textView_weather;
    TextView textView_politic, textView_economy, textView_society, textView_sport;
    TextView textView_entertainment, textView_world, textView_culture, textView_science;

    enum section {POLITIC, ECONOMY, SOCIETY, SPORT, ENTERTAINMENT, WORLD, CULTURE, SCIENCE};
    section cur_selected_section;

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
                textView_date.setText(year + " / " + monthOfYear + " / " + dayOfMonth);
            }

        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        // 선택된 section 굵게 표시
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
        textView_date.setText(Calendar.getInstance().get(Calendar.YEAR) + " / " + Calendar.getInstance().get(Calendar.MONTH) + " / " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        this.searchByGPS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.actionbar, menu);

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

    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(false);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(getApplicationContext(), this);
    }

    private void setTextViewHighlight(section selected_section)
    {
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

        cur_selected_section = selected_section;
    }

    public void onClickSearchDate(View v)
    {
        datePickerDialog.show();
    }

    public void onClickRefreshWeather(View v)
    {
        searchByGPS();
    }

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

}
