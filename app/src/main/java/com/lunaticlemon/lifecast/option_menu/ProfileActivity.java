package com.lunaticlemon.lifecast.option_menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.lunaticlemon.lifecast.member.LoginActivity.result_withdraw;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.result_nickchange;
import static com.lunaticlemon.lifecast.show_article.ShowArticleActivity.result_pwchange;

public class ProfileActivity extends AppCompatActivity {

    String TAG = "Profile";

    TextView textView_id, textView_nick, textView_nickchange;
    TextView textView_gender, textView_birthday, textView_created, textView_city;
    TextView textView_pwchange, textView_withdraw;

    // http request queue
    RequestQueue volley_queue;

    // 사용자 정보
    String id, nickname, gender, birthday, city, created;

    Boolean isNickChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        volley_queue = Volley.newRequestQueue(this);

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
    public void onStop()
    {
        super.onStop();

        // http request가 남아있을 시 모두 취소
        if(volley_queue != null)
            volley_queue.cancelAll(TAG);
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("기사보기 화면으로 돌아가시겠습니까");
        builder.setPositiveButton("예",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // 사용자가 닉네임 변경했을 시 ShowArticleActivity의 nickname 정보 변경
                // ShowArticleActivity의 onActivityResult 확인
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
    public void change_userdata(final String action, final String id, final String changed){

        // 서버와 http protocol을 이용하여 사용자 정보 변경
        // 1st parameter : action
        // 2nd parameter : id
        // 3rd parameter : changed
        // response (success : action 성공 / fail : action 실패)
        String url = "http://115.71.236.22/change_userdata.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        switch(action)
                        {
                            case "nick":
                                if(response.equals("success"))    // 변경 성공
                                {
                                    Toast.makeText(ProfileActivity.this, "변경 성공", Toast.LENGTH_SHORT).show();
                                    textView_nick.setText(changed);
                                    nickname = changed;
                                    isNickChanged = true;
                                }
                                else if(response.equals("fail"))    // 중복되는 닉네임 존재
                                {
                                    Toast.makeText(ProfileActivity.this, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "pw":
                                if(response.equals("success"))    // 변경 성공
                                {
                                    Toast.makeText(ProfileActivity.this, "변경 성공", Toast.LENGTH_SHORT).show();
                                    setResult(result_pwchange);
                                    finish();
                                }
                                else if(response.equals("fail")) // 변경 실패
                                {
                                    Toast.makeText(ProfileActivity.this, "변경 실패", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "withdraw":
                                if(response.equals("success"))    // 탈퇴 성공
                                {
                                    Toast.makeText(ProfileActivity.this, "탈퇴 성공", Toast.LENGTH_SHORT).show();
                                    setResult(result_withdraw);
                                    finish();
                                }
                                else if(response.equals("fail")) // 탈퇴 실패
                                {
                                    Toast.makeText(ProfileActivity.this, "탈퇴 실패", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfileActivity.this, "다시 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("action", action);
                params.put("id", id);
                params.put("changed", changed);

                return params;
            }
        };
        postRequest.setTag(TAG);

        volley_queue.add(postRequest);
    }
}
