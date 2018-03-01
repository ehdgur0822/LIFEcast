package com.lunaticlemon.lifecast.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

class PenDataAdapter extends BaseAdapter {

    Context con;

    // 펜 굵기
    public static final int [] pens = new int[] {
            1,2,3,4,5,
            6,7,8,9,10,
            11,13,15,17,20
    };

    int rowCount;
    int columnCount;


    public PenDataAdapter(Context context) {
        super();

        con = context;

        rowCount = 3;
        columnCount = 5;

    }

    public int getNumColumns() {
        return columnCount;
    }

    public int getCount() {
        return rowCount * columnCount;
    }

    public Object getItem(int position) {
        return pens[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup group) {

        // calculate position
        int rowIndex = position / rowCount;
        int columnIndex = position % rowCount;

        GridView.LayoutParams params = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT,
                GridView.LayoutParams.MATCH_PARENT);

        // create a Pen Image
        int areaWidth = 10;
        int areaHeight = 20;

        Bitmap penBitmap = Bitmap.createBitmap(areaWidth, areaHeight, Bitmap.Config.ARGB_8888);
        Canvas penCanvas = new Canvas();
        penCanvas.setBitmap(penBitmap);

        Paint mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        penCanvas.drawRect(0, 0, areaWidth, areaHeight, mPaint);

        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth((float)pens[position]);
        penCanvas.drawLine(0, areaHeight/2, areaWidth-1, areaHeight/2, mPaint);
        BitmapDrawable penDrawable = new BitmapDrawable(con.getResources(), penBitmap);

        // create a Button with the color
        Button aItem = new Button(con);
        aItem.setText(" ");
        aItem.setLayoutParams(params);
        aItem.setPadding(4, 4, 4, 4);
        aItem.setBackgroundDrawable(penDrawable);
        aItem.setHeight(120);
        aItem.setTag(pens[position]);

        // set listener
        aItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PenDialog.listener != null) {
                    PenDialog.listener.onPenSelected(((Integer)v.getTag()).intValue());
                }

                ((PenDialog)con).finish();
            }
        });

        return aItem;
    }
}
