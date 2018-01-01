package com.lunaticlemon.lifecast.member;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    Spinner spinner_year, spinner_month, spinner_date, spinner_city;
    EditText edit_id, edit_pw, edit_nick;

    // 회원가입 시 사용자가 입력하는 데이터
    String id, pw, nick, city;
    int year = -1, month = -1, date = -1;

    Integer[] arr_year = new Integer[Calendar.getInstance().get(Calendar.YEAR) - 1949];
    Integer[] arr_month = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    Integer[] arr_date = new Integer[31];
    String[] arr_city = {
            "서울","부산","대구","인천","광주","대전","울산","경기",
            "강원","충북","충남","전북","전남","경북","경남","제주"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        spinner_year = (Spinner) findViewById(R.id.spinner_year);
        spinner_month = (Spinner) findViewById(R.id.spinner_month);
        spinner_date = (Spinner) findViewById(R.id.spinner_date);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);
        edit_id = (EditText) findViewById(R.id.editText_id);
        edit_pw = (EditText) findViewById(R.id.editText_pw);
        edit_nick = (EditText) findViewById(R.id.editText_nick);


        // insert 1950 ~ current year to arr_year
        for (int i = 1950; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
            arr_year[i - 1950] = i;
        }

        // insert 1~31 to arr_date
        for (int i = 1; i <= 31; i++) {
            arr_date[i - 1] = i;
        }

        ArrayAdapter<Integer> adapter_year = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_year);
        ArrayAdapter<Integer> adapter_month = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_month);
        ArrayAdapter<Integer> adapter_date = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, arr_date);
        ArrayAdapter<String> adapter_city = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arr_city);

        spinner_year.setAdapter(adapter_year);
        spinner_month.setAdapter(adapter_month);
        spinner_date.setAdapter(adapter_date);
        spinner_city.setAdapter(adapter_city);

        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get select item
                int selected_id = spinner_year.getSelectedItemPosition();
                year = arr_year[selected_id];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get select item
                int selected_id = spinner_month.getSelectedItemPosition();
                month = arr_month[selected_id];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get select item
                int selected_id = spinner_date.getSelectedItemPosition();
                date = arr_date[selected_id];

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

        //TODO check valildation

        
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }

    // 가입취소 버튼 클릭 시, LoginActivity로 돌아감
    public void onClickCancel(View v)
    {
        SignUpActivity.this.finish();
    }

}
