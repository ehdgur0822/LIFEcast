package com.lunaticlemon.lifecast.show_article;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
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


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapFragment mapFragment;

    String[] arr_city = {
            "서울","부산","대구","인천","광주","대전","울산","경기",
            "강원","충북","충남","전북","전남","경북","경남","제주"
    };

    String city;
    int selected_year, selected_month, selected_day;

    ListView listview_marker;
    Marker_Adapter marker_adapter;

    ClusterManager<MarkerItem> clusterManager;
    ArrayList<Local_News> local_newsList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        city = getIntent().getExtras().getString("city");
        selected_year = getIntent().getExtras().getInt("selected_year");
        selected_month = getIntent().getExtras().getInt("selected_month");
        selected_day = getIntent().getExtras().getInt("selected_day");

        getNews(city, selected_year, selected_month, selected_day);

        FragmentManager fragmentManager = getFragmentManager();
        mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        listview_marker = (ListView) findViewById(R.id.listViewMarker);
        listview_marker.setVisibility(View.GONE);
        marker_adapter = new Marker_Adapter();

        listview_marker.setAdapter(marker_adapter);
        listview_marker.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Local_News news = (Local_News)listview_marker.getItemAtPosition(position);

                // web view에 해당 url 보여줌
                Intent intent = new Intent(MapActivity.this, WebViewActivity.class);
                intent.putExtra("url","http://" + news.getUrl());
                startActivity(intent);

                //addView(cur_selected_section, news.getUrl(), gender, birthday, city);
                //redirectUsingCustomTab(news.getUrl());
            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
/*
float   HUE_AZURE
float   HUE_BLUE
float   HUE_CYAN
float   HUE_GREEN
float   HUE_MAGENTA
float   HUE_ORANGE
float   HUE_RED
float   HUE_ROSE
float   HUE_VIOLET
float   HUE_YELLOW
 */
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.56, 126.97), 10));

        clusterManager = new ClusterManager<>( this, googleMap );

        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnInfoWindowClickListener(clusterManager);
        googleMap.setOnCameraIdleListener(clusterManager);

        for(Local_News item : local_newsList)
        {
            clusterManager.addItem(new MarkerItem(item.getTitle(), item.getKeyword1(), new LatLng(item.getLatitude(), item.getLongitude()), item.getCity()));
        }

        clusterManager.setAlgorithm(new GridBasedAlgorithm<MarkerItem>());
        clusterManager.setOnClusterClickListener(
                new ClusterManager.OnClusterClickListener<MarkerItem>() {
                    @Override public boolean onClusterClick(Cluster<MarkerItem> cluster) {

                        // if true, do not move camera

                        marker_adapter.init();
                        for(MarkerItem item : cluster.getItems())
                        {
                            for(Local_News news : local_newsList)
                            {
                                if(item.getTitle().equals(news.getTitle()))
                                {
                                    marker_adapter.addItem(news.getId(), "null", news.getUrl(), news.getTitle(), news.getDate(), news.getCity(), news.getLatitude(), news.getLongitude(), news.getView());
                                }
                            }
                        }
                        marker_adapter.notifyDataSetChanged();
                        listview_marker.setVisibility(View.VISIBLE);

                        return false;
                    }
                });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng latLng)
            {
                marker_adapter.init();
                listview_marker.setVisibility(View.GONE);
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker_adapter.init();

                String marker_title = marker.getTitle();
                String url = null;

                for(Local_News item : local_newsList)
                {
                    if(item.getTitle().equals(marker_title))
                    {
                        url = item.getUrl();
                        break;
                    }
                }

                if(url != null) {
                    Intent intent = new Intent(MapActivity.this, WebViewActivity.class);
                    intent.putExtra("url", "http://" + url);
                    startActivity(intent);
                }
            }
        });
    }

    public void getNews(String city, int selected_year, int selected_month, int selected_day){

        // 서버와 http protocol을 이용하여 해당 지역의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 도시
        // 2nd parameter : 사용자가 선택한 날짜
        class GetNewsData extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String city = (String)params[0];
                String date = (String)params[1];

                String serverURL = "http://115.71.236.22/get_local_news.php";
                String postParameters = "city=" + city + "&date=" + date;


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
                Log.d("mapactivity",result);

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
                        String city = news.getString("city");
                        double latitude = news.getDouble("latitude");
                        double longitude = news.getDouble("longitude");
                        int view = news.getInt("view");

                        local_newsList.add(new Local_News(id, keyword, url, title, date, city, latitude, longitude, view));
                    }

                    mapFragment.getMapAsync(MapActivity.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        String date = selected_year + "-" + selected_month + "-" + selected_day;

        GetNewsData GetNewsData_Task = new GetNewsData();
        GetNewsData_Task.execute(city, date);
    }
}
