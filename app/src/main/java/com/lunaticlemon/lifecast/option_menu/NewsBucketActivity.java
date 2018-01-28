package com.lunaticlemon.lifecast.option_menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.News;
import com.lunaticlemon.lifecast.show_article.News_Adapter;
import com.lunaticlemon.lifecast.show_article.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsBucketActivity extends AppCompatActivity {

    ListView listview_news;
    News_Adapter news_adapter;

    int user_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_bucket);

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
                                addBucket(news.getSection(), user_number, news.getId());
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

    // 서버에서 news를 가져와 listview에 넣어주는 함수
    public void getNews(String user_number){

        // 서버와 http protocol을 이용하여 사용자 보관함의 뉴스를 가져옴
        // 1st parameter : 사용자의 number

        class GetNewsData extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String user_number = (String)params[0];

                String serverURL = "http://115.71.236.22/get_bucketnews.php";
                String postParameters = "user_number=" + user_number;
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
        }

        GetNewsData GetNewsData_Task = new GetNewsData();
        GetNewsData_Task.execute(user_number);
    }

    public void addBucket(String selected_section, int user_number, int news_id){

        // 서버와 http protocol을 이용하여 사용자가 선택한 뉴스를 사용자의 보관함에 넣음
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자의 number
        // 3rd parameter : 뉴스의 id
        class AddBucketData extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {

                String section = (String)params[0];
                String user_number = (String)params[1];
                String news_id = (String)params[2];

                String serverURL = "http://115.71.236.22/add_bucket.php";
                String postParameters = "section=" + section + "&user_number=" + user_number +"&news_id=" + news_id + "&action=" + "delete";
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
                    return new String("add bucket Error: " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String result){
                Toast.makeText(NewsBucketActivity.this, "삭제 성공", Toast.LENGTH_SHORT).show();
            }
        }

        AddBucketData AddBucket_Task = new AddBucketData();
        AddBucket_Task.execute(selected_section, Integer.toString(user_number), Integer.toString(news_id));
    }
}
