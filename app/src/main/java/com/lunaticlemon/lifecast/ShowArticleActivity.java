package com.lunaticlemon.lifecast;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

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
    section cur_selected_section = section.POLITIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_article);

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

    // weatherInfo object contains all information returned by Yahoo Weather API
    // if `weatherInfo` is null, you can get the error from `errorType`
    @Override
    public void gotWeatherInfo(final WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if(weatherInfo != null)
        {
            // 현재 gps 위치의 날씨와 온도를 표시
            textView_weather.setText(weatherInfo.getCurrentText() + "\n" + weatherInfo.getCurrentTemp() + "\u00b0" + "C" );
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



}
