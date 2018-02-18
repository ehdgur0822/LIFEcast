package com.lunaticlemon.lifecast.option_menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.News;
import com.lunaticlemon.lifecast.show_article.News_Adapter;
import com.lunaticlemon.lifecast.show_article.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewsBucketActivity extends AppCompatActivity {

    String TAG = "NewsBucket";

    // http request queue
    RequestQueue volley_queue;

    ListView listview_news;
    News_Adapter news_adapter;


    // 데이터베이스의 member table의 number
    int user_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_bucket);

        volley_queue = Volley.newRequestQueue(this);

        user_number = getIntent().getExtras().getInt("number");

        listview_news = (ListView) findViewById(R.id.listViewNews);
        news_adapter = new News_Adapter();

        listview_news.setAdapter(news_adapter);
        listview_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                News news = (News)listview_news.getItemAtPosition(position);

                // web view에 해당 url 보여줌
                Intent intent = new Intent(NewsBucketActivity.this, WebViewActivity.class);
                intent.putExtra("url","http://" + news.getUrl());
                startActivity(intent);

                //addView(cur_selected_section, news.getUrl(), gender, birthday, city);
                //redirectUsingCustomTab(news.getUrl());
            }
        });

        listview_news.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
                final News news = (News)listview_news.getItemAtPosition(position);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewsBucketActivity.this);

                // set dialog message
                alertDialogBuilder
                        .setMessage("해당 기사를 보관함에서 삭제하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("예",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                deleteBucket(news.getSection(), user_number, news.getId());
                                news_adapter.deleteItem(news.getId());
                                news_adapter.notifyDataSetChanged();
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

        // listview 내용 설정
        getNews(Integer.toString(user_number));
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // http request가 남아있을 시 모두 취소
        if(volley_queue != null)
            volley_queue.cancelAll(TAG);
    }

    // 데이터베이스에서 사용자가 보관함에 담은 news를 가져와 listview에 넣어주는 함수
    public void getNews(final String user_number){

        // 서버와 http protocol을 이용하여 사용자 보관함의 뉴스를 가져옴
        // 1st parameter : 사용자의 number
        String url = "http://115.71.236.22/get_bucketnews.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response.toString());
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
                                String section = news.getString("section");

                                news_adapter.addItem(id, keyword, url, title, date, newspaper, view, section);
                            }

                            news_adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        getNews(user_number);
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("user_number", user_number);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }

    public void deleteBucket(final String selected_section, final int user_number, final int news_id){

        // 서버와 http protocol을 이용하여 사용자가 선택한 뉴스를 사용자의 보관함에 넣음
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자의 number
        // 3rd parameter : 뉴스의 id
        // 4th parameter : 추가 시 add / 삭제 시 delete
        // response : (success : 삭제 성공 / fail : 삭제 실패)

        String url = "http://115.71.236.22/add_bucket.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("success"))
                        {
                            Toast.makeText(NewsBucketActivity.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                        }
                        else if(response.equals("fail"))
                        {
                            Toast.makeText(NewsBucketActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("section", selected_section);
                params.put("user_number", Integer.toString(user_number));
                params.put("news_id", Integer.toString(news_id));
                params.put("action", "delete");
                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}
