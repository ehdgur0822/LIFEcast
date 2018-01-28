package com.lunaticlemon.lifecast.member;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.show_article.ShowArticleActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

public class LoginActivity extends AppCompatActivity {

    public static int request_login = 1001;
    public static int result_logout = 2001, result_finish = 2002, result_withdraw = 2003;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

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

        pref = getSharedPreferences("lifecast",MODE_PRIVATE);
        editor = pref.edit();

        if(pref.getBoolean("autoLogin",false))  // 자동로그인
        {
            String id = pref.getString("id","default"); // default : should never happened
            String pw = pref.getString("pw","default");

            Login login_task = new Login();
            login_task.execute(id, pw);
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

    // sign up button 클릭 시 호출
    public void onClick_btn_signup(View v)
    {
        startActivity(new Intent(getApplication(), SignUpActivity.class));
    }

    // log in button  클릭 시 호출
    public void onClick_btn_login(View v)
    {
        // TODO
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
        else    // id, pw 입력한 경우
        {
            Login login_task = new Login();
            login_task.execute(id, pw);
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
    class Login extends AsyncTask<String, Void, String> {

        String id, pw;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 정보가 일치할 경우 (nickname / gender / birthday / city / created / preference) 형태의 '/' 구분자 사용한 정보 받음
        // 정보가 일치하지 않을 경우 false
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("false"))
            {
                Toast.makeText(LoginActivity.this, "ID 또는 password가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                String number = null, nickname = null, gender = null, birthday = null, city = null, created = null, preference = null;
                StringTokenizer st = new StringTokenizer(result, "/");

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


        @Override
        protected String doInBackground(String... params) {

            id = (String)params[0];
            pw = (String)params[1];

            String serverURL = "http://115.71.236.22/login.php";
            String postParameters = "id=" + id + "&pw=" + pw;


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
                return new String("Insert Member Data Error: " + e.getMessage());
            }
        }
    }


    //퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CAMERA"};

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
