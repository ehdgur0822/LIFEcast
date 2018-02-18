package com.lunaticlemon.lifecast.show_article;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapFragment mapFragment;

    String TAG = "Map";

    // http request queue
    RequestQueue volley_queue;

    // 사용자 정보
    String section, id, gender, birthday, city;

    int selected_year, selected_month, selected_day;

    ListView listview_marker;
    Marker_Adapter marker_adapter;

    ClusterManager<MarkerItem> clusterManager;
    ArrayList<Local_News> local_newsList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        volley_queue = Volley.newRequestQueue(this);

        section = "local";
        id = getIntent().getExtras().getString("id");
        gender = getIntent().getExtras().getString("gender");
        birthday = getIntent().getExtras().getString("birthday");
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
                intent.putExtra("url", news.getUrl());
                startActivity(intent);

                //addView(cur_selected_section, news.getUrl(), gender, birthday, city);
                //redirectUsingCustomTab(news.getUrl());
            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        // 서울로 맵 이동
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
                int news_id = -1;

                for(Local_News item : local_newsList)
                {
                    if(item.getTitle().equals(marker_title))
                    {
                        url = item.getUrl();
                        news_id = item.getId();
                        break;
                    }
                }

                if(url != null && news_id != -1) {
                    Intent intent = new Intent(MapActivity.this, WebViewActivity.class);
                    intent.putExtra("section", section);
                    intent.putExtra("url", url);
                    intent.putExtra("news_id", news_id);
                    intent.putExtra("user_id", id);
                    intent.putExtra("gender", gender);
                    intent.putExtra("birthday", birthday);
                    intent.putExtra("city", city);
                    startActivity(intent);
                }
            }
        });
    }

    // 서버와 http protocol을 이용하여 해당 지역의 뉴스를 가져옴
    public void getNews(final String city, final int selected_year, final int selected_month, final int selected_day){

        // 서버와 http protocol을 이용하여 해당 지역의 뉴스를 가져옴
        // 1st parameter : 사용자가 선택한 도시
        // 2nd parameter : 사용자가 선택한 날짜
        String url = "http://115.71.236.22/get_local_news.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response);
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

                Map<String, String>  params = new HashMap<String, String>();
                params.put("city", city);
                params.put("date", date);
                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}
