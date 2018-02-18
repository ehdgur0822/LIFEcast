package com.lunaticlemon.lifecast.show_article;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;

import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    String TAG = "WebView";

    // http request queue
    RequestQueue volley_queue;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        volley_queue = Volley.newRequestQueue(this);

        // 사용자가 선택한 기사의 url
        String section = getIntent().getExtras().getString("section");
        String url = getIntent().getExtras().getString("url");
        int news_id = getIntent().getExtras().getInt("news_id");
        String user_id = getIntent().getExtras().getString("user_id");
        String gender = getIntent().getExtras().getString("gender");
        String birthday = getIntent().getExtras().getString("birthday");
        String city = getIntent().getExtras().getString("city");

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.loadUrl("http://" + url);

        if(!section.equals("local"))
            addView(section, url, news_id, user_id, gender, birthday, city);
    }

    public void addView(final String _section, final String _url, final int _news_id, final String _user_id, final String _gender, final String _birthday, final String _city){

        // 서버와 http protocol을 이용하여 사용자가 선택한 뉴스의 조회수 증가
        // 1st parameter : 사용자가 선택한 분야
        // 2nd parameter : 사용자가 선택한 기사의 url
        // 3rd parameter : 사용자가 선택한 기사의 id
        // 4th parameter : 사용자의 id
        // 5th parameter : 사용자의 성별
        // 6th parameter : 사용자의 생일
        // 7th parameter : 사용자의 도시
        String url = "http://115.71.236.22/add_view.php";
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
                Map<String, String> params = new HashMap<String, String>();
                params.put("section", _section);
                params.put("url", _url);
                params.put("news_id", Integer.toString(_news_id));
                params.put("user_id", _user_id);
                params.put("gender", _gender);
                params.put("birthday", _birthday);
                params.put("city", _city);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}
