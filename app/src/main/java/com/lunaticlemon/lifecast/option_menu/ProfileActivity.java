package com.lunaticlemon.lifecast.option_menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import static com.lunaticlemon.lifecast.member.LoginActivity.result_withdraw;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.result_nickchange;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.result_pwchange;

public class ProfileActivity extends AppCompatActivity {

    TextView textView_id, textView_nick, textView_nickchange;
    TextView textView_gender, textView_birthday, textView_created, textView_city;
    TextView textView_pwchange, textView_withdraw;

    String id, nickname, gender, birthday, city, created;
    Boolean isNickChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = getIntent().getExtras().getString("id");
        nickname = getIntent().getExtras().getString("nickname");
        gender = getIntent().getExtras().getString("gender");
        birthday = getIntent().getExtras().getString("birthday");
        city = getIntent().getExtras().getString("city");
        created = getIntent().getExtras().getString("created");

        textView_id = (TextView) findViewById(R.id.textView_id);
        textView_nick = (TextView) findViewById(R.id.textView_nick);
        textView_nickchange = (TextView) findViewById(R.id.textView_nickchange);
        textView_gender = (TextView) findViewById(R.id.textView_gender);
        textView_birthday = (TextView) findViewById(R.id.textView_birthday);
        textView_created = (TextView) findViewById(R.id.textView_created);
        textView_city = (TextView) findViewById(R.id.textView_city);
        textView_pwchange = (TextView) findViewById(R.id.textView_pwchange);
        textView_withdraw = (TextView) findViewById(R.id.textView_withdraw);

        textView_id.setText(id);
        textView_nick.setText(nickname);
        switch(gender)
        {
            case "male":
                textView_gender.setText("남성");
                break;
            case "female":
                textView_gender.setText("여성");
                break;
        }
        textView_birthday.setText(birthday);
        textView_created.setText(created);
        textView_city.setText(city);

        textView_nickchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비밀번호 변경 시 변경할 값 입력받는 다이얼로그
                AlertDialog.Builder dialog_input= new AlertDialog.Builder(ProfileActivity.this);
                final EditText editText_changed = new EditText(ProfileActivity.this);
                dialog_input.setView(editText_changed);
                dialog_input.setTitle("변경할 닉네임을 입력해주세요");
                dialog_input.setMessage("숫자포함 2~5자 한글, 3~10자 영문,숫자");
                dialog_input.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Text 값 받아서 로그 남기기
                        String toChange = editText_changed.getText().toString();
                        if(Pattern.matches("^[0-9가-힣]{2,5}$", toChange.toString()) || Pattern.matches("^[a-zA-Z0-9]{3,10}$", toChange.toString()) || Pattern.matches("^[0-9a-zA-Z가-힣]{2,7}$", toChange.toString()))
                        {
                            change_userdata("nick", id, toChange);
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "올바른 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                dialog_input.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog_input.show();
            }
        });

        textView_pwchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비밀번호 변경 시 변경할 값 입력받는 다이얼로그
                AlertDialog.Builder dialog_input= new AlertDialog.Builder(ProfileActivity.this);
                final EditText editText_changed = new EditText(ProfileActivity.this);
                dialog_input.setView(editText_changed);
                dialog_input.setTitle("변경할 비밀번호를 입력해주세요");
                dialog_input.setMessage("6~12자의 영문,숫자와 특수문자");
                dialog_input.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Text 값 받아서 로그 남기기
                        String toChange = editText_changed.getText().toString();
                        if(Pattern.matches("^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,12}", toChange.toString()))
                        {
                            change_userdata("pw", id, toChange);
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "올바른 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                dialog_input.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog_input.show();
            }
        });

        textView_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_userdata("withdraw", id, null);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("기사보기 화면으로 돌아가시겠습니까");
        builder.setPositiveButton("예",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton)
            {
                if(isNickChanged) {
                    setResult(result_nickchange, new Intent().putExtra("nick",nickname));
                }
                finish();
            }
        });

        builder.setNegativeButton("아니오",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton)
            {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 데이터베이스의 사용자 정보를 변경해줌 (닉네임 변경, 비밀번호 변경, 회원탈퇴)
    // 닉네임 변경 시 : (action = nick, id = 사용자의 아이디, changed = 변경할 닉)
    // 비밀번호 변경 시 : (action = pw, id = 사용자의 아이디, changed = 변경할 비밀번호)
    // 회원탈퇴 시 : (action = withdraw, id = 사용자의 아이디, changed = null)
    public void change_userdata(String action, String id, String changed){

        // 서버와 http protocol을 이용하여 사용자 정보 변경
        // 1st parameter : action
        // 2nd parameter : id
        // 3rd parameter : changed
        class Change_userdata extends AsyncTask<String, Void, String> {

            String action, changed;

            @Override
            protected String doInBackground(String... params) {

                action = (String)params[0];
                String id = (String) params[1];
                changed = (String) params[2];

                String serverURL = "http://115.71.236.22/change_userdata.php";
                String postParameters = "action=" + action + "&id=" + id + "&changed=" + changed;


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
                switch(action)
                {
                    case "nick":
                        if(result.equals("success"))    // 변경 성공
                        {
                            Toast.makeText(ProfileActivity.this, "변경 성공", Toast.LENGTH_SHORT).show();
                            textView_nick.setText(changed);
                            nickname = changed;
                            isNickChanged = true;
                        }
                        else // 중복되는 닉네임 존재
                        {
                            Toast.makeText(ProfileActivity.this, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "pw":
                        if(result.equals("success"))    // 변경 성공
                        {
                            Toast.makeText(ProfileActivity.this, "변경 성공", Toast.LENGTH_SHORT).show();
                            setResult(result_pwchange);
                            finish();
                        }
                        else // 변경 실패
                        {
                            Toast.makeText(ProfileActivity.this, "변경 실패", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "withdraw":
                        if(result.equals("success"))    // 탈퇴 성공
                        {
                            Toast.makeText(ProfileActivity.this, "탈퇴 성공", Toast.LENGTH_SHORT).show();
                            setResult(result_withdraw);
                            finish();
                        }
                        else // 탈퇴 실패
                        {
                            Toast.makeText(ProfileActivity.this, "탈퇴 실패", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }

        Change_userdata change_userdata_Task = new Change_userdata();
        change_userdata_Task.execute(action, id, changed);
    }
}
