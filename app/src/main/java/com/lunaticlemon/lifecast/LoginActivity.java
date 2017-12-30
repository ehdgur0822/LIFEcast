package com.lunaticlemon.lifecast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    // sign up button 클릭 시 호출
    public void onClick_btn_signup(View v)
    {
        startActivity(new Intent(getApplication(), SignUpActivity.class));
    }

    // log in button  클릭 시 호출
    public void onClick_btn_login(View v)
    {}
}
