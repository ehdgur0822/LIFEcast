package com.lunaticlemon.lifecast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.lunaticlemon.lifecast.member.LoginActivity;

// 'LIFEcast' logo를 보여주는 activity
// 5초 후 자동으로 다음 Activity인 LoginActivity로 넘어감
public class LogoActivity extends AppCompatActivity {

    Handler logo_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        logo_handler = new Handler();
        logo_handler.postDelayed(new LogoHandler(), 5000);
    }

    private class LogoHandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(), LoginActivity.class));
            LogoActivity.this.finish();
        }
    }
}
