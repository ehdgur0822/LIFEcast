package com.lunaticlemon.lifecast.paint;

import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.lunaticlemon.lifecast.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PaintActivity extends AppCompatActivity {

    static{
        System.loadLibrary("jpgt");
        System.loadLibrary("pngt");
        System.loadLibrary("lept");
        System.loadLibrary("tess");
    }

    private final int GALLERY_CODE=1112;

    PaintBoard board;

    ImageView imageView_load;
    TextView textViewResult, sizeLegendTxt;
    Button colorBtn, penBtn, eraserBtn, undoBtn, colorLegendBtn, recognizeBtn, albumBtn;
    LinearLayout toolsLayout, boardLayout, addedLayout;

    TessBaseAPI mTess;
    String datapath;

    int mColor = 0xff000000;
    int mSize = 2;
    int oldColor;
    int oldSize;
    boolean eraserSelected = false;

    // 사용자가 불러온 이미지
    Bitmap load_image;

    // 사용자가 이미지를 불러왔을 경우 true , 불러오지 않았을 경우 false
    // 이미 이미지를 불러온 경우 사용자가 앨범 버튼 클릭 시 imageView_load 초기화
    boolean isLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        imageView_load = (ImageView) findViewById(R.id.imageView_load);
        toolsLayout = (LinearLayout) findViewById(R.id.toolsLayout);
        boardLayout = (LinearLayout) findViewById(R.id.boardLayout);
        colorBtn = (Button) findViewById(R.id.colorBtn);
        penBtn = (Button) findViewById(R.id.penBtn);
        eraserBtn = (Button) findViewById(R.id.eraserBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);
        recognizeBtn = (Button) findViewById(R.id.recognizeBtn);
        albumBtn = (Button) findViewById(R.id.albumBtn);
        textViewResult = (TextView) findViewById(R.id.textViewResult);

        //initialize Tesseract API
        String language = "eng+kor";
        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        // 학습된 언어 데이터 불러오기
        checkFile(new File(datapath + "tessdata/"), "eng");
        checkFile(new File(datapath + "tessdata/"), "kor");
        mTess.init(datapath, language);

        // init paint board
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        board = new PaintBoard(this);
        board.setLayoutParams(params);
        board.setPadding(2, 2, 2, 2);

        boardLayout.addView(board);

        // add legend
        LinearLayout.LayoutParams addedParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48);
        addedLayout = new LinearLayout(this);
        addedLayout.setLayoutParams(addedParams);
        addedLayout.setOrientation(LinearLayout.VERTICAL);
        addedLayout.setPadding(8,8,8,8);

        LinearLayout outlineLayout = new LinearLayout(this);
        outlineLayout.setLayoutParams(buttonParams);
        outlineLayout.setOrientation(LinearLayout.VERTICAL);
        outlineLayout.setBackgroundColor(Color.LTGRAY);
        outlineLayout.setPadding(1,1,1,1);

        colorLegendBtn = new Button(this);
        colorLegendBtn.setLayoutParams(buttonParams);
        colorLegendBtn.setText(" ");
        colorLegendBtn.setBackgroundColor(mColor);
        colorLegendBtn.setHeight(20);
        outlineLayout.addView(colorLegendBtn);
        addedLayout.addView(outlineLayout);

        sizeLegendTxt = new TextView(this);
        sizeLegendTxt.setLayoutParams(buttonParams);
        sizeLegendTxt.setText("Size : " + mSize);
        sizeLegendTxt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        sizeLegendTxt.setTextSize(16);
        sizeLegendTxt.setTextColor(Color.BLACK);
        addedLayout.addView(sizeLegendTxt);

        toolsLayout.addView(addedLayout);

        colorBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                ColorDialog.listener = new OnColorSelectedListener() {
                    public void onColorSelected(int color) {
                        mColor = color;
                        board.updatePaintProperty(mColor, mSize);
                        displayPaintProperty();
                    }
                };


                // show color dialog
                Intent intent = new Intent(getApplicationContext(), ColorDialog.class);
                startActivity(intent);

            }
        });

        penBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                PenDialog.listener = new OnPenSelectedListener() {
                    public void onPenSelected(int size) {
                        mSize = size;
                        board.updatePaintProperty(mColor, mSize);
                        displayPaintProperty();
                    }
                };


                // show pen dialog
                Intent intent = new Intent(getApplicationContext(), PenDialog.class);
                startActivity(intent);

            }
        });

        eraserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                eraserSelected = !eraserSelected;

                if (eraserSelected) {   // 지우개 선택 시 다른 버튼 안보이게 함
                    colorBtn.setEnabled(false);
                    penBtn.setEnabled(false);
                    undoBtn.setEnabled(false);

                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    oldColor = mColor;
                    oldSize = mSize;

                    mColor = Color.WHITE;
                    mSize = 15;

                    board.updatePaintProperty(mColor, mSize);
                    displayPaintProperty();

                } else {
                    colorBtn.setEnabled(true);
                    penBtn.setEnabled(true);
                    undoBtn.setEnabled(true);

                    colorBtn.invalidate();
                    penBtn.invalidate();
                    undoBtn.invalidate();

                    mColor = oldColor;
                    mSize = oldSize;

                    board.updatePaintProperty(mColor, mSize);
                    displayPaintProperty();

                }

            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                board.undo();
            }
        });

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isLoad)
                    processImage(board.getBitmap());
                else {
                    processImage(load_image);
                }
            }
        });

        albumBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isLoad)
                    selectGallery();
                else {
                    imageView_load.setVisibility(GONE);
                    isLoad = false;
                }

            }
        });

    }

    public int getChosenColor() {
        return mColor;
    }

    public int getPenThickness() {
        return mSize;
    }

    private void displayPaintProperty() {
        colorLegendBtn.setBackgroundColor(mColor);
        sizeLegendTxt.setText("Size : " + mSize);

        addedLayout.invalidate();
    }

    // 학습된 데이터를 통해 image에서 글자 인식 후 결과 보여줌
    public void processImage(Bitmap image){
        String OCRresult = null;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();

        textViewResult.setText(OCRresult);
    }

    // 디렉터리에 파일 있는지 확인
    private void checkFile(File dir, String language) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles(language);
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/" + language + ".traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles(language);
            }
        }
    }

    // 파일을 buffer로 가져옴
    private void copyFiles(String language) {
        try {
            String filepath = datapath + "/tessdata/" + language + ".traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/" + language + ".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 갤러리에서 사진 선택
    private void selectGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case GALLERY_CODE:
                    sendPicture(data.getData()); //갤러리에서 가져오기
                    break;
                default:
                    break;
            }

        }
    }

    private void sendPicture(Uri imgUri) {

        String imagePath = getRealPathFromURI(imgUri); // path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        bitmap = rotate(bitmap, exifDegree);

        load_image = bitmap;
        imageView_load.setImageDrawable(new BitmapDrawable(this.getResources(), bitmap));
        imageView_load.setVisibility(VISIBLE);
        isLoad = true;
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }
}
