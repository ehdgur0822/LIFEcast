package com.lunaticlemon.lifecast.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.Concurrency_Rate;
import com.lunaticlemon.lifecast.show_article.ShowArticleActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.lunaticlemon.lifecast.R.id.textView_country;
import static com.lunaticlemon.lifecast.R.id.textView_rate;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.CULTURE;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.ECONOMY;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.POLITIC;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SCIENCE;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SOCIETY;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.SPORT;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.section.WORLD;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    private static final String TAG = "WIDGET";
    private static final String OnClickTag1 = "CURRENCY_PREV";
    private static final String OnClickTag2 = "CURRENCY_NEXT";
    private static final String OnClickTag3 = "CURRENCY_REFRESH";
    private static final String OnClickTag4 = "SECTION_PREV";
    private static final String OnClickTag5 = "SECTION_NEXT";
    private static final String OnClickTag6 = "SECTION_REFRESH";

    // xml parse 시 namespace
    private static final String ns = null;

    // http request queue
    private static RequestQueue volley_queue = null;

    // 환율 정보
    private static ArrayList<Concurrency_Rate> concurrency_rates = null;
    private static int concurrency_index = 0;
    private static double kor_concurrency = 1.0;

    // listview에 보여줄 section
    static ShowArticleActivity.section cur_section = POLITIC;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(TAG, "update");

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

            Intent intent = new Intent(context, AppWidgetRemoteViewsService.class);
            remoteViews.setRemoteAdapter(R.id.listViewKeyword, intent);

            // 각 버튼에 listener 달기
            remoteViews.setOnClickPendingIntent(R.id.imageButton_currency_prev, getPendingSelfIntent(context, OnClickTag1));
            remoteViews.setOnClickPendingIntent(R.id.imageButton_currency_next, getPendingSelfIntent(context, OnClickTag2));
            remoteViews.setOnClickPendingIntent(R.id.imageButton_currency_refresh, getPendingSelfIntent(context, OnClickTag3));
            remoteViews.setOnClickPendingIntent(R.id.imageButton_section_prev, getPendingSelfIntent(context, OnClickTag4));
            remoteViews.setOnClickPendingIntent(R.id.imageButton_section_next, getPendingSelfIntent(context, OnClickTag5));
            remoteViews.setOnClickPendingIntent(R.id.imageButton_section_refresh, getPendingSelfIntent(context, OnClickTag6));

            set_section(context);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }


    // onclicklistener를 위한 intent
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // widget service 요청 보냄
    public void sendRefreshBroadcast(Context context) {
        // http request가 남아있을 시 모두 취소
        if(volley_queue == null)
            volley_queue = Volley.newRequestQueue(context);

        // 환율정보 없을시 받아옴
        if(concurrency_rates == null) {
            concurrency_rates = new ArrayList<>();
            get_cuncurrency_rate(context);
        }

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, AppWidget.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(TAG, "receive");

        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            // refresh all your widgets
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, AppWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.listViewKeyword);
        }


        // onclick 요청 처리
        if (OnClickTag1.equals(intent.getAction())) {
            // 환율 prev click
            if(concurrency_rates != null && concurrency_rates.size() != 0)
            {
                if(concurrency_index == 0)
                    concurrency_index = concurrency_rates.size()-1;
                else
                    concurrency_index--;

                concurrency_index %= concurrency_rates.size();

                // 선택된 index가 한국일 경우 skip
                if(concurrency_rates.get(concurrency_index).getCountry().equals("KRW"))
                    concurrency_index--;

                concurrency_index %= concurrency_rates.size();
            }

            Log.d(TAG, OnClickTag1);
        } else if (OnClickTag2.equals(intent.getAction())) {
            // 환율 next click
            if(concurrency_rates != null && concurrency_rates.size() != 0) {
                concurrency_index++;

                concurrency_index %= concurrency_rates.size();

                // 선택된 index가 한국일 경우 skip
                if(concurrency_rates.get(concurrency_index).getCountry().equals("KRW"))
                    concurrency_index++;

                concurrency_index %= concurrency_rates.size();
            }

            Log.d(TAG, OnClickTag2);
        } else if (OnClickTag3.equals(intent.getAction())) {
            sendRefreshBroadcast(context);
        } else if (OnClickTag4.equals(intent.getAction())) {
            // 선택된 section의 전 section keyword 보여주기
            switch(cur_section)
            {
                case POLITIC:
                    cur_section = SCIENCE;
                    break;
                case ECONOMY:
                    cur_section = POLITIC;
                    break;
                case SOCIETY:
                    cur_section = ECONOMY;
                    break;
                case SPORT:
                    cur_section = SOCIETY;
                    break;
                case WORLD:
                    cur_section = SPORT;
                    break;
                case CULTURE:
                    cur_section = WORLD;
                    break;
                case SCIENCE:
                    cur_section = CULTURE;
                    break;
                default:
                    cur_section = POLITIC;
                    break;
            }
            sendRefreshBroadcast(context);
            set_section(context);
        } else if (OnClickTag5.equals(intent.getAction())) {
            // 선택된 section의 다음 section keyword 보여주기
            switch(cur_section)
            {
                case POLITIC:
                    cur_section = ECONOMY;
                    break;
                case ECONOMY:
                    cur_section = SOCIETY;
                    break;
                case SOCIETY:
                    cur_section = SPORT;
                    break;
                case SPORT:
                    cur_section = WORLD;
                    break;
                case WORLD:
                    cur_section = POLITIC;
                    break;
                case CULTURE:
                    cur_section = SCIENCE;
                    break;
                case SCIENCE:
                    cur_section = POLITIC;
                    break;
                default:
                    cur_section = POLITIC;
                    break;
            }
            sendRefreshBroadcast(context);
            set_section(context);
        } else if (OnClickTag6.equals(intent.getAction())) {
            sendRefreshBroadcast(context);
            Toast.makeText(context, "click6", Toast.LENGTH_SHORT);
        }

        if(concurrency_rates != null && concurrency_rates.size() != 0)
        {
            set_concurrency(context);
            set_section(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
        Log.d(TAG, "enable");

        volley_queue = Volley.newRequestQueue(context);
        concurrency_rates = new ArrayList<>();

        get_cuncurrency_rate(context);
        set_section(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
        Log.d(TAG, "disable");

        if(concurrency_rates != null)
            concurrency_rates.clear();

        // http request가 남아있을 시 모두 취소
        if(volley_queue != null)
            volley_queue.cancelAll(TAG);
    }

    public void set_section(Context context)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        Intent intent = new Intent(context, AppWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.listViewKeyword, intent);

        // 각 버튼에 listener 달기
        views.setOnClickPendingIntent(R.id.imageButton_currency_prev, getPendingSelfIntent(context, OnClickTag1));
        views.setOnClickPendingIntent(R.id.imageButton_currency_next, getPendingSelfIntent(context, OnClickTag2));
        views.setOnClickPendingIntent(R.id.imageButton_currency_refresh, getPendingSelfIntent(context, OnClickTag3));
        views.setOnClickPendingIntent(R.id.imageButton_section_prev, getPendingSelfIntent(context, OnClickTag4));
        views.setOnClickPendingIntent(R.id.imageButton_section_next, getPendingSelfIntent(context, OnClickTag5));
        views.setOnClickPendingIntent(R.id.imageButton_section_refresh, getPendingSelfIntent(context, OnClickTag6));

        // 현재 선택된 section widget에 보여주기
        switch(cur_section)
        {
            case POLITIC:
                views.setTextViewText(R.id.textView_section, "정치");
                break;
            case ECONOMY:
                views.setTextViewText(R.id.textView_section, "경제");
                break;
            case SOCIETY:
                views.setTextViewText(R.id.textView_section, "사회");
                break;
            case SPORT:
                views.setTextViewText(R.id.textView_section, "스포츠");
                break;
            case WORLD:
                views.setTextViewText(R.id.textView_section, "국제");
                break;
            case CULTURE:
                views.setTextViewText(R.id.textView_section, "문화");
                break;
            case SCIENCE:
                views.setTextViewText(R.id.textView_section, "과학");
                break;
            default:
                views.setTextViewText(R.id.textView_section, "정치");
                break;
        }

        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, AppWidget.class),views);
    }

    public void set_concurrency(Context context)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        Intent intent = new Intent(context, AppWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.listViewKeyword, intent);

        // 각 버튼에 listener 달기
        views.setOnClickPendingIntent(R.id.imageButton_currency_prev, getPendingSelfIntent(context, OnClickTag1));
        views.setOnClickPendingIntent(R.id.imageButton_currency_next, getPendingSelfIntent(context, OnClickTag2));
        views.setOnClickPendingIntent(R.id.imageButton_currency_refresh, getPendingSelfIntent(context, OnClickTag3));
        views.setOnClickPendingIntent(R.id.imageButton_section_prev, getPendingSelfIntent(context, OnClickTag4));
        views.setOnClickPendingIntent(R.id.imageButton_section_next, getPendingSelfIntent(context, OnClickTag5));
        views.setOnClickPendingIntent(R.id.imageButton_section_refresh, getPendingSelfIntent(context, OnClickTag6));

        double rate = Double.parseDouble(String.format("%.2f", kor_concurrency / concurrency_rates.get(concurrency_index).getRate()));

        // 현재 선택된 나라의 환율정보 보여주기
        switch(concurrency_rates.get(concurrency_index).getCountry())
        {
            case "USD":
                views.setTextViewText(textView_country, "미국 " + concurrency_rates.get(concurrency_index).getCountry());
                break;
            case "JPY":
                views.setTextViewText(textView_country, "일본 " + concurrency_rates.get(concurrency_index).getCountry() + " 100");
                rate = Double.parseDouble(String.format("%.2f", rate * 100));
                break;
            case "CAD":
                views.setTextViewText(textView_country, "캐나다 " + concurrency_rates.get(concurrency_index).getCountry());
                break;
            case "HKD":
                views.setTextViewText(textView_country, "홍콩 " + concurrency_rates.get(concurrency_index).getCountry());
                break;
            case "AUD":
                views.setTextViewText(textView_country, "호주 " + concurrency_rates.get(concurrency_index).getCountry());
                break;
        }

        views.setTextViewText(textView_rate, String.valueOf(rate));
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, AppWidget.class),views);
    }

    public void get_cuncurrency_rate(final Context context){

        // European Central Bank에서 환율정보를 가져와서 concurrency_rates list에 저장
        // document : http://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html#dev
        String url = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                            InputStream in_s = new ByteArrayInputStream(response.getBytes("UTF-8"));
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(in_s, null);
                            parser.nextTag();

                            // parse 시작
                            parser.require(XmlPullParser.START_TAG, ns, "gesmes:Envelope");
                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.getEventType() != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                String name = parser.getName();
                                if (name.equals("Cube")) {
                                    if(parser.getAttributeValue(null, "currency") != null && parser.getAttributeValue(null, "rate") != null) {
                                        readAttribute(parser);
                                    }
                                }
                                else {
                                    skip(parser);
                                }
                            }

                            set_concurrency(context);
                            Log.e(TAG, "rates" + concurrency_rates.size());

                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
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
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }

    // xml에서 currency 나라 이름 / rate 환율 정보 가져와서 concurrency_rates list에 저장
    public void readAttribute(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Cube");
        String tag = parser.getName();
        String country = parser.getAttributeValue(null, "currency");

        if (tag.equals("Cube")) {
            if(country.equals("USD") || country.equals("JPY") || country.equals("CAD") || country.equals("HKD") || country.equals("AUD")) {
                concurrency_rates.add(new Concurrency_Rate(parser.getAttributeValue(null, "currency"), Double.valueOf(parser.getAttributeValue(null, "rate"))));
            }
            else if(country.equals("KRW"))
            {
                concurrency_rates.add(new Concurrency_Rate(parser.getAttributeValue(null, "currency"), Double.valueOf(parser.getAttributeValue(null, "rate"))));
                kor_concurrency = Double.valueOf(parser.getAttributeValue(null, "rate"));
            }
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, ns, "Cube");
    }

    // xml에서 필요없는 정보일 경우 다음 tag로 넘겨줌
    public void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}