package com.lunaticlemon.lifecast.paint;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import com.lunaticlemon.lifecast.R;

/**
 * Created by lemon on 2018-02-07.
 */

public class PenDialog extends Activity {

    GridView grid;
    Button closeBtn;
    PenDataAdapter adapter;

    public static OnPenSelectedListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paint_dialog);

        this.setTitle("굵기 선택");

        grid = (GridView) findViewById(R.id.colorGrid);
        closeBtn = (Button) findViewById(R.id.closeBtn);

        grid.setColumnWidth(14);
        grid.setBackgroundColor(Color.GRAY);
        grid.setVerticalSpacing(4);
        grid.setHorizontalSpacing(4);

        adapter = new PenDataAdapter(this);
        grid.setAdapter(adapter);
        grid.setNumColumns(adapter.getNumColumns());

        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // dispose this activity
                finish();
            }
        });

    }

}



