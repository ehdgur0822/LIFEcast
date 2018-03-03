package com.lunaticlemon.lifecast.minigame;

import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.lunaticlemon.lifecast.R;
import com.lunaticlemon.lifecast.camera.ColorBlobDetector;
import com.lunaticlemon.lifecast.camera.CustomSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class MiniGameActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    static{ System.loadLibrary("opencv_java3"); }

    String TAG = "MiniGame";

    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    private Mat rgbaInnerWindow;

    private CustomSurfaceView mOpenCvCameraView;

    // 게임 오버 시 true
    private boolean is_gameover = false;

    // 카메라 방향
    public static final int VIEW_ORI_FRONT = 0;
    public static final int VIEW_ORI_BACK = 1;
    public int viewOri = VIEW_ORI_BACK;

    // 비행사, 총알
    private Mat astronaut, bullet;
    private Size astronaut_size = new Size(100, 100);
    private Size bullet_size = new Size(100, 100);

    // 난이도, 추가적인 총알 생성 시 사용
    private int hardness = 4;

    double iThreshold = 0;

    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    private int mDetectorType = JAVA_DETECTOR;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private boolean mIsColorSelected = false;

    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private Scalar CONTOUR_COLOR_WHITE;

    int numberOfFingers = 0;

    // 화면에 나타날 bullet list
    private List<Bullet> bullet_list;
    // 화면 밖으로 나가 삭제될 bullet
    private List<Bullet> bullet_to_delete;
    // 화면 밖으로 나간 bullet 개수 -> 점수계산에 사용
    private int deleted_bullet;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MiniGameActivity.this);

                    // 이미지 불러옴
                    try {
                        astronaut = Utils.loadResource(MiniGameActivity.this, R.drawable.astronaut, -1);
                        bullet = Utils.loadResource(MiniGameActivity.this, R.drawable.bullet, -1);
                    }catch (IOException e) {
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_game);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOpenCvCameraView = (CustomSurfaceView) findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(viewOri); // front-camera(1),  back-camera(0)

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mIntermediateMat = new Mat();

        // custum view 초기화
        Camera.Size resolution = mOpenCvCameraView.getResolution();
        Camera.Parameters cParams = mOpenCvCameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        mOpenCvCameraView.setParameters(cParams);

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        CONTOUR_COLOR_WHITE = new Scalar(255,255,255,255);


        // 총알 관련 변수 초기화
        bullet_list = new LinkedList<>();
        bullet_to_delete = new LinkedList<>();

        deleted_bullet = 0;

        // 초기 bullet list 생성
        init_bullet(width, height, 10);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        mIntermediateMat.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        // 범위 체크
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        // 사용자가 터치한 부분 영역
        Rect touchedRect = new Rect();

        touchedRect.x = (x>5) ? x-5 : 0;
        touchedRect.y = (y>5) ? y-5 : 0;

        touchedRect.width = (x+5 < cols) ? x + 5 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+5 < rows) ? y + 5 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // 터치한 영역의 색 구함
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        // 뒤 카메라 이용시 180도 회전
        if(viewOri == VIEW_ORI_BACK)
        {
            Core.flip(mRgba ,mRgba ,1);
            Core.flip(mGray ,mGray ,1);
        }

        iThreshold = 8000;

        //Imgproc.blur(mRgba, mRgba, new Size(5,5));
        Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(3, 3), 1, 1);
        //Imgproc.medianBlur(mRgba, mRgba, 3);

        // 사용자가 물체를 터치 안했을 시
        if (!mIsColorSelected)
            return mRgba;

        // 게임 오버 시, 사용자가 다이얼로그 확인 버튼 클릭 전
        if(is_gameover)
            return mRgba;

        // 사용자가 터치한 물체 경계선 list
        List<MatOfPoint> contours = mDetector.getContours();
        mDetector.process(mRgba);

        if (contours.size() <= 0) {
            return mRgba;
        }

        // 사용자가 터치한 물체를 감싸는 사각형 구함
        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0)	.toArray()));

        double boundWidth = rect.size.width;
        double boundHeight = rect.size.height;
        int boundPos = 0;

        for (int i = 1; i < contours.size(); i++) {
            rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width;
                boundHeight = rect.size.height;
                boundPos = i;
            }
        }

        Rect boundRect = Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray()));
        double boundRect_threshold = boundRect.br().y - boundRect.tl().y;
        boundRect_threshold = boundRect_threshold * 0.7;
        boundRect_threshold = boundRect.tl().y + boundRect_threshold;

        //Core.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR, 2, 8, 0 );
        //Imgproc.rectangle( mRgba, boundRect.tl(), new Point(boundRect.br().x, a), CONTOUR_COLOR, 2, 8, 0 );

        // 손가락 검출 위해 손가락 사이사이 부분 찾음
        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 3, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));

        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        if(hull.toArray().length < 3)
            return mRgba;

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        if(convexDefect.empty())
            return mRgba;

        List<MatOfPoint> hullPoints = new LinkedList<MatOfPoint>();
        List<Point> listPo = new LinkedList<Point>();
        for (int j = 0; j < hull.toList().size(); j++) {
            listPo.add(contours.get(boundPos).toList().get(hull.toList().get(j)));
        }

        MatOfPoint e = new MatOfPoint();
        e.fromList(listPo);
        hullPoints.add(e);

        List<MatOfPoint> defectPoints = new LinkedList<MatOfPoint>();
        List<Point> listPoDefect = new LinkedList<Point>();
        for (int j = 0; j < convexDefect.toList().size(); j = j+4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j+2));
            Integer depth = convexDefect.toList().get(j+3);
            if(depth > iThreshold && farPoint.y < boundRect_threshold){
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j+2)));
            }
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        //Imgproc.drawContours(mRgba, hullPoints, -1, CONTOUR_COLOR, 3);

        int defectsTotal = (int) convexDefect.total();

        // 손가락 개수 계산
        this.numberOfFingers = listPoDefect.size();
        if(this.numberOfFingers > 5) this.numberOfFingers = 5;

        // 사용자가 선택한 물체의 가운데 찾기
        MatOfPoint mop = new MatOfPoint();
        mop.fromList(listPo);

        Moments moments = Imgproc.moments(mop);

        Point centroid = new Point();

        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        //Imgproc.circle(mRgba, centroid, 6, new Scalar(255,255,255));

        double far = 0.0;
        Point far_point = new Point();
        boolean checked = false;

        // 사용자가 선택한 물체의 끝 점 계산
        for(Point p : listPo){
            //Imgproc.circle(mRgba, p, 6, new Scalar(255,255,0));
            if(euclideanDistance(centroid, p) > far)
            {
                far_point = p;
                checked = true;
            }
        }

        if(checked) {
            mIntermediateMat = mRgba;
            mRgba = putMask(mIntermediateMat, astronaut, far_point, astronaut_size);

            for(Bullet b : bullet_list)
            {
                // bullet이 화면 안에서 움직임
                if(b.move()) {
                    if(b.check_collision(far_point, astronaut_size))    // 충돌 발생
                    {
                        // 게임종료
                        Log.d(TAG, "astronaut" + far_point + "/" + astronaut_size.width + "/" + astronaut_size.height + " , " + "bullet" + b.bullet_location());
                        game_over();
                    }
                    else {
                        mIntermediateMat = mRgba;
                        mRgba = putMask(mIntermediateMat, bullet, b.bullet_location(), bullet_size);
                    }
                }
                else
                {
                    // bullet이 화면 밖으로 움직임 -> bullet 삭제필요
                    bullet_to_delete.add(b);
                }
            }

            deleted_bullet += bullet_to_delete.size();

            // 화면 밖으로 움직인 bullet 삭제
            for(Bullet b : bullet_to_delete)
            {
                bullet_list.remove(b);
            }

            // bullet이 화면 밖으로 움직인 경우 새 bullet 생성 후 list 비움
            if(bullet_to_delete.size() > 0) {
                make_bullet(mRgba.width(), mRgba.height(), bullet_to_delete.size());
                bullet_to_delete.clear();
            }


        }

        return mRgba;
    }

    // 두 점 a,b 사이의 거리
    public double euclideanDistance(Point a, Point b){
        double distance = 0.0;

        if(a != null && b != null){
            double xDiff = a.x - b.x;
            double yDiff = a.y - b.y;
            distance = Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
        }

        return distance;
    }

    // src의 center에 mask를 씌움
    public Mat putMask(Mat src, Mat mask, Point center, Size mask_size){

        // 그려질 영역 범위가 화면 밖일 때
        if((int)(center.x - mask_size.width/2) < 0 || (int)(center.y - mask_size.height/2) < 0 ||
                (int) (center.x + mask_size.width) > src.width() || (int) (center.y + mask_size.height) > src.height())
            return src;

        Mat mask_resized = new Mat();
        Mat src_roi = new Mat();
        Mat roi_gray = new Mat();

        Imgproc.resize(mask, mask_resized, mask_size);

        // 효과가 그려질 부분
        Rect roi = new Rect((int)(center.x - mask_size.width/2), (int)(center.y - mask_size.height/2),
                (int) mask_size.width, (int) mask_size.height);

        src.submat(roi).copyTo(src_roi);

        // 검정색 부분 투명하게 만들기
        Mat mask_grey = new Mat(); //greymask
        Mat roi_rgb = new Mat();
        Imgproc.cvtColor(mask_resized, mask_grey, Imgproc.COLOR_BGRA2GRAY);
        //Imgproc.threshold(mask_grey, mask_grey, 230, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(mask_grey, mask_grey, 50, 0, Imgproc.THRESH_TOZERO);

        ArrayList<Mat> maskChannels = new ArrayList<>(4);
        ArrayList<Mat> result_mask = new ArrayList<>(4);
        result_mask.add(new Mat());
        result_mask.add(new Mat());
        result_mask.add(new Mat());
        result_mask.add(new Mat());

        Core.split(mask_resized, maskChannels);

        Core.bitwise_and(maskChannels.get(0), mask_grey, result_mask.get(0));
        Core.bitwise_and(maskChannels.get(1), mask_grey, result_mask.get(1));
        Core.bitwise_and(maskChannels.get(2), mask_grey, result_mask.get(2));
        Core.bitwise_and(maskChannels.get(3), mask_grey, result_mask.get(3));

        Core.merge(result_mask, roi_gray);

        Core.bitwise_not(mask_grey, mask_grey);

        ArrayList<Mat> srcChannels = new ArrayList<>(4);
        Core.split(src_roi, srcChannels);
        Core.bitwise_and(srcChannels.get(0), mask_grey, result_mask.get(0));
        Core.bitwise_and(srcChannels.get(1), mask_grey, result_mask.get(1));
        Core.bitwise_and(srcChannels.get(2), mask_grey, result_mask.get(2));
        Core.bitwise_and(srcChannels.get(3), mask_grey, result_mask.get(3));

        Core.merge(result_mask, roi_rgb);

        Core.addWeighted(roi_gray, 1, roi_rgb, 1, 0, roi_rgb);

        roi_rgb.copyTo(new Mat(src,roi));

        return src;
    }

    // 초기 bullet을 number*3 개수만큼 생성
    public void init_bullet(int width, int height, int number)
    {
        Random rand = new Random();
        int temp;

        for(int i=0;i<number;i++)
        {
            // 오른쪽에서 생성
            // 랜덤한 높이 설정
            temp = rand.nextInt(height);
            bullet_list.add(new Bullet(width, temp, 0, width, 0, height, bullet_size));

            // 위에서 생성
            // 랜덤한 너비 생성
            temp = rand.nextInt(width/2) + width/2;
            bullet_list.add(new Bullet(temp, height , 0, width, 0, height, bullet_size));

            // 아래서 생성
            // 랜덤한 높이 설정
            temp = rand.nextInt(width/2) + width/2;
            bullet_list.add(new Bullet(temp, 0, 0, width, 0, height, bullet_size));
        }
    }

    // bullet을 number * hardness만큼 추가적으로 생성
    public void make_bullet(int width, int height, int number)
    {
        Log.d(TAG, "make bullet" + number);

        Random rand = new Random();
        int multiple = rand.nextInt(hardness) + 1;
        int temp;

        for(int i=0;i<number * multiple;i++)
        {
            switch(rand.nextInt(3))
            {
                case 0:
                    // 오른쪽에서 생성
                    // 랜덤한 높이 설정
                    temp = rand.nextInt(height);
                    bullet_list.add(new Bullet(width, temp, 0, width, 0, height, bullet_size));
                    break;
                case 1:
                    // 위에서 생성
                    // 랜덤한 너비 생성
                    temp = rand.nextInt(width/2) + width/2;
                    bullet_list.add(new Bullet(temp, height , 0, width, 0, height, bullet_size));
                    break;
                case 2:
                    // 아래서 생성
                    // 랜덤한 높이 설정
                    temp = rand.nextInt(width/2) + width/2;
                    bullet_list.add(new Bullet(temp, 0, 0, width, 0, height, bullet_size));
                    break;
            }
        }
    }

    // astronaut이 bullet에 닿았을때 (게임 종료) 실행
    public void game_over()
    {
        is_gameover = true;

        new Thread() {
            public void run() {
                MiniGameActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MiniGameActivity.this);
                            builder.setMessage("Game Over! " + String.valueOf(deleted_bullet * 10) + "점 달성.");
                            builder.setPositiveButton("게임 종료",new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    finish();
                                }
                            });
                            AlertDialog game_over_dialog = builder.create();
                            game_over_dialog.show();
                        }
                        catch (Exception e) {}
                    }
                });
            }
        }.start();

    }
}

