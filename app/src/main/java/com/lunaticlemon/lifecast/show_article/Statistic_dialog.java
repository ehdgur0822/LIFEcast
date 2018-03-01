package com.lunaticlemon.lifecast.show_article;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lemon on 2018-01-19.
 */

public class Statistic_dialog extends Dialog {
    private Context con;

    String TAG = "Statistic_dialog";

    // http request queue
    RequestQueue volley_queue;

    // 사용자가 선택한 분야, 날짜
    private ShowArticleActivity.section selected_section;
    private int selected_year, selected_month, selected_day;

    //사용자의 id, 나이, 성별, 도시 정보
    private String user_id, user_gender, user_birthday, user_city;
    private int user_age;

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

    public Statistic_dialog(@NonNull Context context, ShowArticleActivity.section _section, int _year, int _month, int _day, String _id, String _gender, String _birthday, String _city) {
        super(context);

        con = context;
        selected_section = _section;
        selected_year = _year;
        selected_month = _month;
        selected_day = _day;

        user_id = _id;
        user_gender = _gender;
        user_birthday = _birthday;
        user_city = _city;

        // 나이 구하기
        Calendar current = Calendar.getInstance();
        int currentYear  = current.get(Calendar.YEAR);
        int user_birthYear = Integer.parseInt(user_birthday.substring(0,4));
        user_age = currentYear - user_birthYear + 1;

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

        volley_queue = Volley.newRequestQueue(con);

        spinner_gender = (Spinner) findViewById(R.id.spinner_gender);
        spinner_age = (Spinner) findViewById(R.id.spinner_age);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);

        ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_gender);
        ArrayAdapter<String> adapter_age = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_age);
        ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(con, android.R.layout.simple_spinner_dropdown_item, arr_city);

        // spinner의 default는 사용자의 데이터로 맞춤
        spinner_gender.setAdapter(adapter_gender);
        switch(user_gender)
        {
            case "male":
                spinner_gender.setSelection(0);
                break;
            case "female":
                spinner_gender.setSelection(1);
                break;
        }

        spinner_age.setAdapter(adapter_age);
        if(user_age >= 10 && user_age < 20)
        {
            spinner_age.setSelection(0);
        }
        else if(user_age >= 20 && user_age < 30)
        {
            spinner_age.setSelection(1);
        }
        else if(user_age >= 30 && user_age < 40)
        {
            spinner_age.setSelection(2);
        }
        else if(user_age >= 40 && user_age < 50)
        {
            spinner_age.setSelection(3);
        }
        else if(user_age >= 50 && user_age < 60)
        {
            spinner_age.setSelection(4);
        }
        else if(user_age >= 60)
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
            if(user_city.equals(arr_city[i])) {
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
                switch(selected_section)
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
                intent.putExtra("user_id", user_id);
                intent.putExtra("gender", user_gender);
                intent.putExtra("birthday", user_birthday);
                intent.putExtra("city", user_city);
                con.startActivity(intent);

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
    public void getNews(final ShowArticleActivity.section selected_section, final int selected_year, final int selected_month, final int selected_day,
                        final String _gender, final String _age, final String _city){

        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 날짜
        String url = "http://115.71.236.22/get_statistic.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("false")) {
                            try {
                                news_adapter.init();

                                JSONObject jsonObj = new JSONObject(response);
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
                                listview_news.smoothScrollToPosition(0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                news_adapter.notifyDataSetChanged();
                            }
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
                String section, gender, age;

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

                switch(_gender)
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
                    default:
                        gender = user_gender;
                        break;
                }

                switch(_age)
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
                    default:
                        age = Integer.toString(user_age);
                        break;
                }

                Map<String, String>  params = new HashMap<String, String>();
                params.put("section", section);
                params.put("date", date);
                params.put("gender", gender);
                params.put("age", age);
                params.put("city", _city);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);

    }

}