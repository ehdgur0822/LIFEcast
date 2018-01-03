package com.lunaticlemon.lifecast.member;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    EditText edit_id, edit_pw, edit_nick;
    TextView textView_id_valid, textView_pw_valid, textView_nick_valid;
    RadioGroup radioGroup_gender;
    Spinner spinner_year, spinner_month, spinner_day, spinner_city;

    // 회원가입 시 사용자가 입력하는 데이터
    // id : 6~12자의 영문 또는 숫자
    // pw : 6~12자의 영문,숫자와 특수문자
    // nick : 숫자포함 2~5자 한글, 3~10자 영문,숫자
    // id_valid, pw_valid, nick_valid : 각각 id, pw, nick 유효성 여부 (true : 유효함, false : 유효하지않음)
    // city : 입력 전에는 null, 입력 후에는 사용자가 입력한 값으로 변환
    // gender : 입력 전에는 null, 입력 후에는 male or female 값으로 변환
    // year_count, month_count, day_count, city_count : 입력 전에는 0, 입력 후 0이상의 값으로 변환
    String id, pw, nick, city, gender = "";
    boolean id_valid = false, pw_valid = false, nick_valid = false;
    int year, month, day;
    int year_count = -1, month_count = -1, day_count = -1, city_count = -1;

    Integer[] arr_year = new Integer[Calendar.getInstance().get(Calendar.YEAR) - 1949];
    Integer[] arr_month = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    Integer[] arr_day = new Integer[31];
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

        // id 유효성 체크
        edit_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // id : 6~12자의 영문 또는 숫자
                if(Pattern.matches("^[a-zA-Z0-9]{6,12}$", s.toString()))
                {
                    CheckDuplicate checkDuplicate_task = new CheckDuplicate();
                    checkDuplicate_task.execute(s.toString() , "id");
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

        // pw 유효성 체크
        edit_pw.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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

        // nickname 유효성 체크
        edit_nick.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nick : 숫자포함 2~5자 한글, 3~10자 영문,숫자
                if(Pattern.matches("^[0-9가-힣]{2,5}$", s.toString()) || Pattern.matches("^[a-zA-Z0-9]{3,10}$", s.toString()))
                {
                    CheckDuplicate checkDuplicate_task = new CheckDuplicate();
                    checkDuplicate_task.execute(s.toString() , "nick");
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
        spinner_month.setAdapter(adapter_month);
        spinner_day.setAdapter(adapter_day);
        spinner_city.setAdapter(adapter_city);

        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                int selected_id = spinner_year.getSelectedItemPosition();
                year = arr_year[selected_id];
                year_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                int selected_id = spinner_month.getSelectedItemPosition();
                month = arr_month[selected_id];
                month_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item
                int selected_id = spinner_day.getSelectedItemPosition();
                day = arr_day[selected_id];
                day_count++;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get select item
                int selected_id = spinner_city.getSelectedItemPosition();
                city = arr_city[selected_id];
                city_count++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                                // parameter : (id, pw, nickname, gender, year, month, day, city)
                                InsertMemberData InsertMemberData_task = new InsertMemberData();
                                InsertMemberData_task.execute(id, pw, nick, gender, Integer.toString(year), Integer.toString(month), Integer.toString(day), city);
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


    // database에 사용자 정보 추가
    // parameter : (id, pw, nickname, gender, year, month, day, city)
    class InsertMemberData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("success")) {
                // 회원가입 성공, LoginActivity로 돌아감
                Toast.makeText(SignUpActivity.this, "signup success", Toast.LENGTH_SHORT).show();
                SignUpActivity.this.finish();
            }
            else {
                Toast.makeText(SignUpActivity.this, "signup fail", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[0];
            String pw = (String)params[1];
            String nickname = (String)params[2];
            String gender = (String)params[3];
            String year = (String)params[4];
            String month = (String)params[5];
            String day = (String)params[6];
            String city = (String)params[7];

            String birthday = year + "-" + month + "-" + day;

            Log.d("insert", id+"-"+pw+"-"+nickname+"-"+gender+"-"+year+"-"+month+"-"+day+"-"+city+"-"+birthday);
            String serverURL = "http://115.71.236.22/signup.php";
            String postParameters = "id=" + id + "&pw=" + pw + "&nickname=" + nickname + "&gender=" + gender + "&birthday=" + birthday + "&city=" + city;


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

    // id 혹은 nickname 중복 확인
    // 1st parameter : 사용자가 입력한 id 혹은 nickname
    // 2nd parameter : 중복 확인하는 것이 id일 경우 id, nickname의 경우 nick
    class CheckDuplicate extends AsyncTask<String, Void, String> {

        String check;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // result가 false 이면 중복되는 데이터가 없음 ,true이면 이미 존재하는 데이터임
            if(result.equals("false"))
            {
                if(check.equals("id")) {
                    id_valid = true;
                    textView_id_valid.setText("valid");
                    textView_id_valid.setTextColor(Color.GREEN);
                }
                else if(check.equals("nick"))
                {
                    nick_valid = true;
                    textView_nick_valid.setText("valid");
                    textView_nick_valid.setTextColor(Color.GREEN);
                }
            }
            else if(result.equals("true"))
            {
                if(check.equals("id")) {
                    id_valid = false;
                    textView_id_valid.setText("already exist");
                    textView_id_valid.setTextColor(Color.RED);
                }
                else if(check.equals("nick"))
                {
                    nick_valid = false;
                    textView_nick_valid.setText("already exist");
                    textView_nick_valid.setTextColor(Color.RED);
                }
            }

        }


        @Override
        protected String doInBackground(String... params) {

            String data = (String)params[0];    // 체크할 대상의 내용
            check = (String)params[1];   // 중복 체크할 대상이 id인 경우 id, nick인 경우 nick 값을 가짐

            String serverURL = "http://115.71.236.22/check_duplicate.php";
            String postParameters = "data=" + data + "&check=" + check;


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
                return new String("Error: " + e.getMessage());
            }
        }
    }
}
