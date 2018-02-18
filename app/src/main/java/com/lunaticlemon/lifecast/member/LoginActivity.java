package com.lunaticlemon.lifecast.member;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.ShowArticleActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class LoginActivity extends AppCompatActivity {

    public static int request_login = 1001;
    public static int result_logout = 2001, result_finish = 2002, result_withdraw = 2003;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String TAG = "Login";

    // http request queue
    RequestQueue volley_queue;

    EditText editText_id, editText_pw;
    CheckBox autoLogin;
    boolean autoLoginChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 빌드 버전 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {
                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        volley_queue = Volley.newRequestQueue(this);

        pref = getSharedPreferences("lifecast",MODE_PRIVATE);
        editor = pref.edit();

        if(pref.getBoolean("autoLogin",false))  // 자동로그인
        {
            String id = pref.getString("id","default"); // default : should never happened
            String pw = pref.getString("pw","default");

            login(id, pw);
        }

        editText_id = (EditText) findViewById(R.id.editText_id);
        editText_pw = (EditText) findViewById(R.id.editText_pw);
        autoLogin = (CheckBox) findViewById(R.id.checkBox_autoLogin);

        autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                if(isChecked)
                {
                    autoLoginChecked = true;
                }
                else
                {
                    autoLoginChecked = false;
                    editor.remove("id");
                    editor.remove("pw");
                    editor.commit();
                }
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // http request가 남아있을 시 모두 취소
        if(volley_queue != null)
            volley_queue.cancelAll(TAG);
    }

    // sign up button 클릭 시 호출
    public void onClick_btn_signup(View v)
    {
        startActivity(new Intent(getApplication(), SignUpActivity.class));
    }

    // log in button  클릭 시 호출
    public void onClick_btn_login(View v)
    {
        String id, pw;

        id = editText_id.getText().toString();
        pw = editText_pw.getText().toString();

        if(id == null)
        {
            Toast.makeText(this, "ID를 입력하세요", Toast.LENGTH_SHORT).show();
        }
        else if(pw == null)
        {
            Toast.makeText(this, "password를 입력하세요", Toast.LENGTH_SHORT).show();
        }
        else    // id, pw 모두 입력한 경우
        {
            login(id, pw);
        }

    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == request_login) {
            if (resultCode == result_finish)    // exit
            {
                finish();
            }
            else if (resultCode == result_logout)    // log out
            {
                editor.remove("id");
                editor.remove("pw");
                editor.putBoolean("autoLogin", false);
                editor.commit();

                autoLogin.setChecked(false);
                editText_pw.setText("");
                editText_id.setText("");
            }
            else if (resultCode == result_withdraw)    // log out
            {
                editor.remove("id");
                editor.remove("pw");
                editor.putBoolean("autoLogin", false);
                editor.commit();

                autoLogin.setChecked(false);
                editText_pw.setText("");
                editText_id.setText("");
            }
        }
    }

    // 서버와 http protocol을 이용하여 정보를 보내 사용자가 입력한 정보와 database에 저장된 정보 일치여부 확인
    // parameter : (id, pw)
    // response : (false : 로그인 정보 불일치 , true : 로그인 정보 일치)
    public void login(final String id, final String pw)
    {
        String url = "http://115.71.236.22/login.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("false"))
                        {
                            Toast.makeText(LoginActivity.this, "ID 또는 password가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String number = null, nickname = null, gender = null, birthday = null, city = null, created = null, preference = null;
                            StringTokenizer st = new StringTokenizer(response, "/");

                            if(st.hasMoreTokens())
                                number = st.nextToken();
                            if(st.hasMoreTokens())
                                nickname = st.nextToken();
                            if(st.hasMoreTokens())
                                gender = st.nextToken();
                            if(st.hasMoreTokens())
                                birthday = st.nextToken();
                            if(st.hasMoreTokens())
                                city = st.nextToken();
                            if(st.hasMoreTokens())
                                created = st.nextToken();
                            if(st.hasMoreTokens())
                                preference = st.nextToken();

                            // autoLogin 체크되있을 경우 sharedpreference에 id, pw, check여부 저장
                            if(autoLoginChecked == true) {
                                editor.putString("id", id);
                                editor.putString("pw", pw);
                                editor.putBoolean("autoLogin", true);
                                editor.commit();
                            }

                            // 로그인 성공
                            if(number != null && nickname != null && gender != null && birthday != null && city != null && created != null && preference != null) {
                                Intent intent = new Intent(LoginActivity.this, ShowArticleActivity.class);
                                intent.putExtra("number", Integer.parseInt(number));
                                intent.putExtra("id", id);
                                intent.putExtra("nickname", nickname);
                                intent.putExtra("gender", gender);
                                intent.putExtra("birthday", birthday);
                                intent.putExtra("city", city);
                                intent.putExtra("created", created);
                                intent.putExtra("preference", preference);
                                startActivityForResult(intent, request_login);
                            }
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "다시 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("id", id);
                params.put("pw", pw);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }


    //퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 모든 퍼미션이 허가되었는지 확인
        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean accessfinelocationPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    boolean accesscoarselocationPermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    boolean accesscameraPermissionAccepted = grantResults[2]
                            == PackageManager.PERMISSION_GRANTED;
                    if (!accessfinelocationPermissionAccepted || !accesscoarselocationPermissionAccepted || !accesscameraPermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    }
                }
                break;
        }
    }

    // 퍼미션 요청하는 다이얼로그
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}
