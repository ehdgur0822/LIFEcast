package com.lunaticlemon.lifecast.member;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lunaticlemon.lifecast.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    String TAG = "SignUp";

    EditText edit_id, edit_pw, edit_nick;
    TextView textView_id_valid, textView_pw_valid, textView_nick_valid;
    RadioGroup radioGroup_gender;
    Spinner spinner_year, spinner_month, spinner_day, spinner_city;

    // http request queue
    RequestQueue volley_queue;


    // 회원가입 시 사용자가 입력하는 데이터
    // id : 6~12자의 영문 또는 숫자
    // pw : 6~12자의 영문,숫자와 특수문자
    // nick : 숫자포함 2~5자 한글, 3~10자 영문,숫자
    // id_valid, pw_valid, nick_valid : 각각 id, pw, nick 유효성 여부 (true : 유효함, false : 유효하지않음)
    // city : 입력 전에는 null, 입력 후에는 사용자가 입력한 값으로 변환
    // gender : 입력 전에는 null, 입력 후에는 male or female 값으로 변환
    // year_count, month_count, day_count, city_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환 (사용자가 입력하였는지 여부 확인 위해 사용)
    String id, pw, nick, city, gender = "";
    boolean id_valid = false, pw_valid = false, nick_valid = false;
    int year, month, day;
    int year_count = -1, month_count = -1, day_count = -1, city_count = -1;


    // spinner에 들어갈 정보
    Integer[] arr_year = new Integer[Calendar.getInstance().get(Calendar.YEAR) - 1949]; //  1950 ~ 현재 년도
    Integer[] arr_month = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};  // 1월 ~ 12월
    Integer[] arr_day = new Integer[31]; // 1일 ~ 31일
    String[] arr_city = {
            "서울","부산","대구","인천","광주","대전","울산","경기",
            "강원","충북","충남","전북","전남","경북","경남","제주"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edit_id = (EditText) findViewById(R.id.editText_id);
        edit_pw = (EditText) findViewById(R.id.editText_pw);
        edit_nick = (EditText) findViewById(R.id.editText_nick);
        textView_id_valid = (TextView) findViewById(R.id.textView_id_valid);
        textView_pw_valid = (TextView) findViewById(R.id.textView_pw_valid);
        textView_nick_valid = (TextView) findViewById(R.id.textView_nick_valid);
        radioGroup_gender = (RadioGroup) findViewById(R.id.radiogroup_gender);
        spinner_year = (Spinner) findViewById(R.id.spinner_year);
        spinner_month = (Spinner) findViewById(R.id.spinner_month);
        spinner_day = (Spinner) findViewById(R.id.spinner_day);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);

        volley_queue = Volley.newRequestQueue(this);

        edit_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // id 유효성 체크
            @Override
            public void afterTextChanged(Editable s) {
                // id : 6~12자의 영문 또는 숫자
                if(Pattern.matches("^[a-zA-Z0-9]{6,12}$", s.toString()))
                {
                    final String check_data = s.toString();

                    // 서버와 http protocol을 이용하여 정보를 보내 사용자가 입력한 id 혹은 nickname 중복 확인
                    // 1st parameter : 사용자가 입력한 id 혹은 nickname
                    // 2nd parameter : 중복 확인하는 것이 id일 경우 id, nickname의 경우 nick
                    // response (false : 중복없음 / true : 중복)
                    String url = "http://115.71.236.22/check_duplicate.php";
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("false"))
                                    {
                                        id_valid = true;
                                        textView_id_valid.setText("valid");
                                        textView_id_valid.setTextColor(Color.GREEN);
                                    }
                                    else if(response.equals("true"))
                                    {
                                        id_valid = false;
                                        textView_id_valid.setText("already exist");
                                        textView_id_valid.setTextColor(Color.RED);
                                    }
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(SignUpActivity.this, "다시 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                    )
                    {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("data", check_data);
                            params.put("check", "id");

                            return params;
                        }
                    };
                    postRequest.setTag(TAG);

                    volley_queue.add(postRequest);
                }
                else
                {
                    id_valid = false;
                    textView_id_valid.setText("invalid");
                    textView_id_valid.setTextColor(Color.RED);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });


        edit_pw.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // pw 유효성 체크
            @Override
            public void afterTextChanged(Editable s) {
                // pw : 6~12자의 영문,숫자와 특수문자
                if(Pattern.matches("^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,12}", s.toString()))
                {
                    pw_valid = true;
                    textView_pw_valid.setText("valid");
                    textView_pw_valid.setTextColor(Color.GREEN);
                }
                else
                {
                    pw_valid = false;
                    textView_pw_valid.setText("invalid");
                    textView_pw_valid.setTextColor(Color.RED);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });


        edit_nick.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // nickname 유효성 체크
            @Override
            public void afterTextChanged(Editable s) {
                // nick : 숫자포함 2~5자 한글, 3~10자 영문,숫자
                if(Pattern.matches("^[0-9가-힣]{2,5}$", s.toString()) || Pattern.matches("^[a-zA-Z0-9]{3,10}$", s.toString()) || Pattern.matches("^[0-9a-zA-Z가-힣]{2,7}$", s.toString()))
                {
                    final String check_data = s.toString();

                    // 서버와 http protocol을 이용하여 정보를 보내 사용자가 입력한 id 혹은 nickname 중복 확인
                    // 1st parameter : 사용자가 입력한 id 혹은 nickname
                    // 2nd parameter : 중복 확인하는 것이 id일 경우 id, nickname의 경우 nick
                    String url = "http://115.71.236.22/check_duplicate.php";
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("false"))
                                    {
                                        nick_valid = true;
                                        textView_nick_valid.setText("valid");
                                        textView_nick_valid.setTextColor(Color.GREEN);
                                    }
                                    else if(response.equals("true"))
                                    {
                                        nick_valid = false;
                                        textView_nick_valid.setText("already exist");
                                        textView_nick_valid.setTextColor(Color.RED);
                                    }
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(SignUpActivity.this, "다시 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                    )
                    {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("data", check_data);
                            params.put("check", "nick");

                            return params;
                        }
                    };
                    postRequest.setTag(TAG);

                    volley_queue.add(postRequest);
                }
                else
                {
                    nick_valid = false;
                    textView_nick_valid.setText("invalid");
                    textView_nick_valid.setTextColor(Color.RED);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        radioGroup_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.radiobtn_male) {
                    gender = "male";
                }
                else if(checkedId == R.id.radiobtn_female) {
                    gender = "female";
                }
            }

        });

        // insert 1950 ~ current year to arr_year
        for (int i = 1950; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
            arr_year[i - 1950] = i;
        }

        // insert 1~31 to arr_date
        for (int i = 1; i <= 31; i++) {
            arr_day[i - 1] = i;
        }

        ArrayAdapter<Integer> adapter_year = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_year);
        ArrayAdapter<Integer> adapter_month = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_month);
        ArrayAdapter<Integer> adapter_day = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_day);
        ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr_city);

        spinner_year.setAdapter(adapter_year);
        spinner_year.setSelection(40);  // 초기 년도 설정
        spinner_month.setAdapter(adapter_month);
        spinner_day.setAdapter(adapter_day);
        spinner_city.setAdapter(adapter_city);

        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected_id = spinner_year.getSelectedItemPosition();
                // 사용자가 선택한 년도
                year = arr_year[selected_id];

                // year_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환
                year_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected_id = spinner_month.getSelectedItemPosition();

                // 사용자가 선택한 월
                month = arr_month[selected_id];

                // month_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환
                month_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected_id = spinner_day.getSelectedItemPosition();

                // 사용자가 선택한 일
                day = arr_day[selected_id];

                // day_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환
                day_count++;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selected_id = spinner_city.getSelectedItemPosition();

                // 사용자가 선택한 지역
                city = arr_city[selected_id];

                // city_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환
                city_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

    // 회원가입 버튼 클릭 시
    public void onClickSignUp(View v)
    {
        id = edit_id.getText().toString();
        pw = edit_pw.getText().toString();
        nick = edit_nick.getText().toString();

        if(id_valid)
        {
            if(pw_valid)
            {
                if(nick_valid)
                {
                    if(gender.equals("male") || gender.equals("female"))
                    {
                        if(year_count > 0 && month_count > 0 && day_count > 0)
                        {
                            if(city_count > 0)
                            {
                                // 서버와 http protocol을 이용하여 정보를 보내 database에 사용자 정보 추가
                                // parameter : (id, pw, nickname, gender, year, month, day, city)
                                String url = "http://115.71.236.22/signup.php";
                                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                                        new Response.Listener<String>()
                                        {
                                            @Override
                                            public void onResponse(String response) {
                                                if(response.equals("success")) {
                                                    // 회원가입 성공, LoginActivity로 돌아감
                                                    Toast.makeText(SignUpActivity.this, "가입성공", Toast.LENGTH_SHORT).show();
                                                    SignUpActivity.this.finish();
                                                }
                                                else {
                                                    Toast.makeText(SignUpActivity.this, "가입실패", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener()
                                        {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(SignUpActivity.this, "가입실패", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                )
                                {
                                    @Override
                                    protected Map<String, String> getParams()
                                    {
                                        // parameter : (id, pw, nickname, gender, year, month, day, city)
                                        Map<String, String>  params = new HashMap<String, String>();
                                        params.put("id", id);
                                        params.put("pw", pw);
                                        params.put("nickname", nick);
                                        params.put("gender", gender);
                                        params.put("birthday", Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day));
                                        params.put("city", city);

                                        return params;
                                    }
                                };
                                postRequest.setTag(TAG);

                                volley_queue.add(postRequest);

                            }
                            else
                            {
                                Toast.makeText(this, "지역을 체크해 주세요", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(this, "생년월일을 체크해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "성별을 체크해 주세요", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(this, "nickname을 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "password를 확인하세요", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "ID를 확인하세요", Toast.LENGTH_SHORT).show();
        }
    }

    // 가입취소 버튼 클릭 시, LoginActivity로 돌아감
    public void onClickCancel(View v)
    {
        SignUpActivity.this.finish();
    }

}
