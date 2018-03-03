package com.lunaticlemon.lifecast.minigame;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

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

public class PuzzleGameActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    static{ System.loadLibrary("opencv_java3"); }

    String TAG = "Puzzle";

    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    // puzzle 배경 화면
    private Mat puzzle_board;

    // 사용자의 물체 끝부분 위치를 나타낼 효과
    private Mat effect;

    private CustomSurfaceView mOpenCvCameraView;

    // 카메라 방향
    public static final int VIEW_ORI_FRONT = 0;
    public static final int VIEW_ORI_BACK = 1;
    public int viewOri = VIEW_ORI_FRONT;
    public int viewOrinum = 2;

    private ImageButton imageButton_takePicture;

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

    private PuzzleProcessor mPuzzleProcessor;
    private MenuItem mItemHideNumbers;
    private MenuItem mItemStartNewGame;

    private int mGameWidth, mGameHeight;
    private Point far_point;

    // 효과 경로 저장 list
    private List<Point> effect_trace;
    // 해당 point에 클릭 이벤트를 발생시킬 effect 개수
    private int effect_threshold = 20;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(PuzzleGameActivity.this);

                    // 효과 이미지 불러옴
                    try {
                        effect = Utils.loadResource(PuzzleGameActivity.this, R.drawable.effect1, -1);
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
        setContentView(R.layout.activity_puzzle_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CustomSurfaceView) findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(viewOri); // front-camera(1),  back-camera(0)

        effect_trace = new LinkedList<>();

        mPuzzleProcessor = new PuzzleProcessor();
        mPuzzleProcessor.prepareNewGame();

        imageButton_takePicture = (ImageButton) findViewById(R.id.imageButton_takePicture);

        imageButton_takePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 카메라 방향 변경
                viewOri++;
                viewOri %= viewOrinum;

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setCameraIndex(viewOri); // front-camera(1),  back-camera(0)
                mOpenCvCameraView.enableView();

                imageButton_takePicture.setVisibility(View.INVISIBLE);
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemHideNumbers = menu.add("Show/hide tile numbers");
        mItemStartNewGame = menu.add("Start new game");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemStartNewGame) {
            /* We need to start new game */
            mPuzzleProcessor.prepareNewGame();
        } else if (item == mItemHideNumbers) {
            /* We need to enable or disable drawing of the tile numbers */
            mPuzzleProcessor.toggleTileNumbers();
        }
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mIntermediateMat = new Mat();
        puzzle_board = new Mat();

        // custum view 초기화
        Camera.Size resolution = mOpenCvCameraView.getResolution();
        Camera.Parameters cParams = mOpenCvCameraView.getParameters();
        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        mOpenCvCameraView.setParameters(cParams);

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        puzzle_board = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        CONTOUR_COLOR_WHITE = new Scalar(255,255,255,255);

        mGameWidth = width;
        mGameHeight = height;
        mPuzzleProcessor.prepareGameSize(width, height);

        far_point = new Point();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        puzzle_board.release();
        mIntermediateMat.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        if(!mIsColorSelected) {
            int cols = mRgba.cols();
            int rows = mRgba.rows();

            int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
            int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;

            // 범위 체크
            if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

            // 사용자가 터치한 부분 영역
            Rect touchedRect = new Rect();

            touchedRect.x = (x > 5) ? x - 5 : 0;
            touchedRect.y = (y > 5) ? y - 5 : 0;

            touchedRect.width = (x + 5 < cols) ? x + 5 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y + 5 < rows) ? y + 5 - touchedRect.y : rows - touchedRect.y;

            Mat touchedRegionRgba = mRgba.submat(touchedRect);

            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            // 터치한 영역의 색 구함
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width * touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++)
                mBlobColorHsv.val[i] /= pointCount;

            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
            mDetector.setHsvColor(mBlobColorHsv);

            Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

            mIsColorSelected = true;

            touchedRegionRgba.release();
            touchedRegionHsv.release();
        }
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

        if(viewOri == VIEW_ORI_FRONT)
        {
            puzzle_board = inputFrame.rgba();
            return puzzle_board;
        }

        // 뒤 카메라 이용시 180도 회전
        if (viewOri == VIEW_ORI_BACK) {
            Core.flip(mRgba, mRgba, 1);
            Core.flip(mGray, mGray, 1);
        }

        iThreshold = 8000;

        //Imgproc.blur(mRgba, mRgba, new Size(5,5));
        Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(3, 3), 1, 1);
        //Imgproc.medianBlur(mRgba, mRgba, 3);

        // 사용자가 물체를 터치 안했을 시 back 카메라 보여줌
        if (!mIsColorSelected)
            return mRgba;

        // 사용자가 터치한 물체 경계선 list
        List<MatOfPoint> contours = mDetector.getContours();
        mDetector.process(mRgba);

        // 사용자가 터치한 물체가 검출되지 않을 때 퍼즐화면만 보여줌
        if (contours.size() <= 0) {
            return mPuzzleProcessor.puzzleFrame(puzzle_board);
        }

        // 사용자가 터치한 물체를 감싸는 사각형 구함
        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));

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

        if (hull.toArray().length < 3)
            return mPuzzleProcessor.puzzleFrame(puzzle_board);

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        if (convexDefect.empty())
            return mPuzzleProcessor.puzzleFrame(puzzle_board);

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
        for (int j = 0; j < convexDefect.toList().size(); j = j + 4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j + 2));
            Integer depth = convexDefect.toList().get(j + 3);
            if (depth > iThreshold && farPoint.y < boundRect_threshold) {
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j + 2)));
            }
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        //Imgproc.drawContours(mRgba, hullPoints, -1, CONTOUR_COLOR, 3);

        int defectsTotal = (int) convexDefect.total();

        this.numberOfFingers = listPoDefect.size();
        if (this.numberOfFingers > 5) this.numberOfFingers = 5;

        // 사용자가 선택한 물체의 가운데 찾기
        MatOfPoint mop = new MatOfPoint();
        mop.fromList(listPo);

        Moments moments = Imgproc.moments(mop);

        Point centroid = new Point();

        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        //Imgproc.circle(mRgba, centroid, 6, new Scalar(255,255,255));

        double far = 0.0;
        boolean checked = false;

        // 사용작가 선택한 물체의 끝 점 계산
        for (Point p : listPo) {
            //Imgproc.circle(mRgba, p, 6, new Scalar(255,255,0));
            if (euclideanDistance(centroid, p) > far) {
                far_point = p;
                checked = true;
            }
        }

        if (checked) {
            mPuzzleProcessor.puzzleFrame(puzzle_board).copyTo(mIntermediateMat);
            mIntermediateMat = putMask(mIntermediateMat, far_point, new Size(100, 100));
            check_click(far_point);
            return mIntermediateMat;
        }

        return mPuzzleProcessor.puzzleFrame(puzzle_board);
    }

    // 두 점 사이의 거리
    public double euclideanDistance(Point a, Point b){
        double distance = 0.0;

        if(a != null && b != null){
            double xDiff = a.x - b.x;
            double yDiff = a.y - b.y;
            distance = Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
        }

        return distance;
    }

    public Mat putMask(Mat src, Point center, Size mask_size){

        // 그려질 영역 범위가 화면 밖일 때
        if((int)(center.x - mask_size.width/2) < 0 || (int)(center.y - mask_size.height/2) < 0 ||
                (int) (center.x + mask_size.width) > src.width() || (int) (center.y + mask_size.height) > src.height())
            return src;

        Mat mask_resized = new Mat();
        Mat src_roi = new Mat();
        Mat roi_gray = new Mat();

        Imgproc.resize(effect, mask_resized, mask_size);

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

    public void check_click(Point effect_point)
    {
        effect_trace.add(effect_point);

        if(effect_trace.size() > effect_threshold)
        {
            boolean is_clicked = true;
            int last_box = calculate_box(effect_trace.get(effect_trace.size()-1));

            for(int i = effect_trace.size()-2;i > effect_trace.size()-effect_threshold-1;i--)
            {
                if(calculate_box(effect_trace.get(i)) != last_box)
                {
                    is_clicked = false;
                    break;
                }
            }

            // click인 경우 click이벤트 처리
            if(is_clicked) {
                mPuzzleProcessor.deliverTouchEvent((int) effect_trace.get(effect_trace.size() - 1).x, (int) effect_trace.get(effect_trace.size() - 1).y);
                effect_trace.clear();
            }


        }
    }

    // effect가 현재 위치한 퍼즐 번호를 알아냄
    // 실제 섞인 퍼즐의 번호가 아닌 절대적인 번호를 알아냄
    // example)
    //  1 . 2 . 3 . 4
    //  5 . 6 . 7 . 8
    //  9 .10 .11 .12
    // 13 .14 .15 .16
    public int calculate_box(Point effect)
    {
        int puzzle_width = mGameWidth / mPuzzleProcessor.getGridSize();
        int puzzle_height = mGameHeight / mPuzzleProcessor.getGridSize();

        int box_width;
        int box_height;

        for(int i = 1;;i++)
        {
            if(puzzle_width * i > effect.x) {
                box_width = i;
                break;
            }
        }

        for(int i = 1;;i++)
        {
            if(puzzle_height * i > effect.y) {
                box_height = i;
                break;
            }
        }

        return box_width + box_height * mPuzzleProcessor.getGridSize();
    }
}