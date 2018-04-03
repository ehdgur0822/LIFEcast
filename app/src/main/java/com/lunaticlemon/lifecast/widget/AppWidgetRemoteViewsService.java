package com.lunaticlemon.lifecast.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by lemon on 2018-03-14.
 */

// RemoteViewsFactory를 widget에서 호출
public class AppWidgetRemoteViewsService  extends RemoteViewsService {
    private static final String TAG = "WidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory: " + "Service called");
        return new AppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

