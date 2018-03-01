package com.lunaticlemon.lifecast.paint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

class ColorDataAdapter extends BaseAdapter {

    Context con;

    // 사용자가 선택할수 있는 색깔 목록
    public static final int [] colors = new int[] {
            0xff000000,0xff00007f,0xff0000ff,0xff007f00,0xff007f7f,0xff00ff00,0xff00ff7f,
            0xff00ffff,0xff7f007f,0xff7f00ff,0xff7f7f00,0xff7f7f7f,0xffff0000,0xffff007f,
            0xffff00ff,0xffff7f00,0xffff7f7f,0xffff7fff,0xffffff00,0xffffff7f,0xffffffff
    };

    int rowCount;
    int columnCount;


    public ColorDataAdapter(Context context) {
        super();

        con = context;

        // init black color
        rowCount = 3;
        columnCount = 7;

    }

    public int getNumColumns() {
        return columnCount;
    }

    public int getCount() {
        return rowCount * columnCount;
    }

    public Object getItem(int position) {
        return colors[position];
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

        // create a Button with the color
        Button aItem = new Button(con);
        aItem.setText(" ");
        aItem.setLayoutParams(params);
        aItem.setPadding(4, 4, 4, 4);
        aItem.setBackgroundColor(colors[position]);
        aItem.setHeight(120);
        aItem.setTag(colors[position]);

        // set listener
        aItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ColorDialog.listener != null) {
                    ColorDialog.listener.onColorSelected(((Integer)v.getTag()).intValue());
                }

                ((ColorDialog)con).finish();
            }
        });

        return aItem;
    }
}
