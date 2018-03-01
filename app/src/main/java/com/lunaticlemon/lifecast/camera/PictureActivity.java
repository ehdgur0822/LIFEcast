package com.lunaticlemon.lifecast.camera;

import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class PictureActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    static{ System.loadLibrary("opencv_java3"); }

    String TAG = "Picture";

    // filter 종류
    public static final int VIEW_MODE_RGBA      = 0;
    public static final int VIEW_MODE_GRAY      = 1;
    public static final int VIEW_MODE_CANNY     = 2;
    public static final int VIEW_MODE_SEPIA     = 3;
    public static final int VIEW_MODE_BLUE      = 4;
    public static final int VIEW_MODE_GREEN     = 5;
    public static final int VIEW_MODE_SOBEL     = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;
    public int viewMode = VIEW_MODE_RGBA;
    public int viewModenum = 8;


    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    private Mat rgbaInnerWindow;
    private Mat mSepiaKernel, mBlueKernel, mGreenKernel;
    private Mat saved_picture;

    // 사용자가 선택한 효과
    private Mat effect1, effect2, effect3, effect4, effect5, effect6;
    private int selected_effect = 1;

    private CustomSurfaceView mOpenCvCameraView;

    // 카메라 방향
    public static final int VIEW_ORI_FRONT = 0;
    public static final int VIEW_ORI_BACK = 1;
    public int viewOri = VIEW_ORI_FRONT;
    public int viewOrinum = 2;

    private ImageView imageView_effect1, imageView_effect2, imageView_effect3;
    private ImageView imageView_effect4, imageView_effect5, imageView_effect6;
    private ImageButton imageButton_takePicture, imageButton_rotate, imageButton_filter;

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

    // 효과 경로 저장 최대 개수
    private int max_trace = 50;
    // 효과 경로 저장 list
    private List<Point> thumb_trace;
    // 효과 종류 저장 list
    private List<Integer> thumb_effect;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(PictureActivity.this);

                    // 효과 이미지 불러옴
                    try {
                        effect1 = Utils.loadResource(PictureActivity.this, R.drawable.effect1, -1);
                        effect2 = Utils.loadResource(PictureActivity.this, R.drawable.effect2, -1);
                        effect3 = Utils.loadResource(PictureActivity.this, R.drawable.effect3, -1);
                        effect4 = Utils.loadResource(PictureActivity.this, R.drawable.effect4, -1);
                        effect5 = Utils.loadResource(PictureActivity.this, R.drawable.effect5, -1);
                        effect6 = Utils.loadResource(PictureActivity.this, R.drawable.effect6, -1);
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
        setContentView(R.layout.activity_picture);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOpenCvCameraView = (CustomSurfaceView) findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(viewOri); // front-camera(1),  back-camera(0)

        imageView_effect1 = (ImageView) findViewById(R.id.imageView_effect1);
        imageView_effect2 = (ImageView) findViewById(R.id.imageView_effect2);
        imageView_effect3 = (ImageView) findViewById(R.id.imageView_effect3);
        imageView_effect4 = (ImageView) findViewById(R.id.imageView_effect4);
        imageView_effect5 = (ImageView) findViewById(R.id.imageView_effect5);
        imageView_effect6 = (ImageView) findViewById(R.id.imageView_effect6);
        imageButton_takePicture = (ImageButton) findViewById(R.id.imageButton_takePicture);
        imageButton_rotate = (ImageButton) findViewById(R.id.imageButton_rotate);
        imageButton_filter = (ImageButton) findViewById(R.id.imageButton_filter);

        setAlpha(1);    // default로 선택된 효과는 1번 효과

        thumb_trace = new LinkedList<>();
        thumb_effect = new LinkedList<>();

        imageView_effect1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 1;
                setAlpha(1);
            }
        });

        imageView_effect2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 2;
                setAlpha(2);
            }
        });

        imageView_effect3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 3;
                setAlpha(3);
            }
        });

        imageView_effect4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 4;
                setAlpha(4);
            }
        });

        imageView_effect5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 5;
                setAlpha(5);
            }
        });

        imageView_effect6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selected_effect = 6;
                setAlpha(6);
            }
        });

        imageButton_takePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // LIFEcast 디렉터리 생성 여부 확인
                File dir = new File(Environment.getExternalStorageDirectory() + "/LIFECast/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // LIFEcast 디렉터리에 사진 저장
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String timeStamp = sdf.format(new Date());
                String storageDir = Environment.getExternalStorageDirectory().getPath() + "/LIFECast/";
                String filename = "LIFECast_" + timeStamp + ".jpg";

                Mat image_to_write = new Mat();
                if(viewMode == VIEW_MODE_GRAY)  // 흑백  필터 적용 시
                    Imgproc.cvtColor(saved_picture, image_to_write, Imgproc.COLOR_GRAY2BGR);
                else
                    Imgproc.cvtColor(saved_picture, image_to_write, Imgproc.COLOR_RGB2BGR);
                boolean check_save = imwrite(storageDir + filename, image_to_write);

                // 파일 저장 여부 확인
                if(check_save) {
                    Log.d("onTouch", "click happen success");
                    galleryAddPic(storageDir + filename);
                    Toast.makeText(getApplicationContext(), "사진 저장 완료", Toast.LENGTH_LONG).show();
                }
                else
                    Log.d("onTouch", "click happen fail");
            }
        });

        imageButton_rotate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 카메라 방향 변경
                viewOri++;
                viewOri %= viewOrinum;

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setCameraIndex(viewOri); // front-camera(1),  back-camera(0)
                mOpenCvCameraView.enableView();
            }
        });

        imageButton_filter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // filter 변경
                viewMode++;
                viewMode %= viewModenum;

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.enableView();
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

        // filter 만들기
        // VIEW_MODE_SEPIA
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        // VIEW_MODE_BLUE
        mBlueKernel = new Mat(4, 4, CvType.CV_32F);
        mBlueKernel.put(0, 0, /* R */0.131f, 0.534f, 0.272f, 0f);
        mBlueKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mBlueKernel.put(2, 0, /* B */0.189f, 0.769f, 0.393f, 0f);
        mBlueKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        // VIEW_MODE_GREEN
        mGreenKernel = new Mat(4, 4, CvType.CV_32F);
        mGreenKernel.put(0, 0, /* R */0.131f, 0.534f, 0.272f, 0f);
        mGreenKernel.put(1, 0, /* G */0.189f, 0.769f, 0.393f, 0f);
        mGreenKernel.put(2, 0, /* B */0.168f, 0.686f, 0.349f, 0f);
        mGreenKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        mIntermediateMat.release();
        mSepiaKernel.release();
        mBlueKernel.release();
        mGreenKernel.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        viewMode = VIEW_MODE_RGBA;

        thumb_trace.clear();
        thumb_effect.clear();

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
            return saved_picture = setFilter(mRgba);

        // 사용자가 터치한 물체 경계선 list
        List<MatOfPoint> contours = mDetector.getContours();
        mDetector.process(mRgba);

        if (contours.size() <= 0) {
            return saved_picture = setFilter(mRgba);
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
            return saved_picture = setFilter(mRgba);

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos).toArray()), hull, convexDefect);

        if(convexDefect.empty())
            return saved_picture = setFilter(mRgba);

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

        // 사용작가 선택한 물체의 끝 점 계산
        for(Point p : listPo){
            //Imgproc.circle(mRgba, p, 6, new Scalar(255,255,0));
            if(euclideanDistance(centroid, p) > far)
            {
                far_point = p;
                checked = true;
            }
        }

        if(checked)
            addPoint(far_point, selected_effect);

        for(int i = 0 ; i < thumb_trace.size() ; i++)
        {
            mIntermediateMat = mRgba;
            mRgba = putMask(mIntermediateMat, thumb_trace.get(i), new Size(100, 100), thumb_effect.get(i));

        }

        // filter 적용
        mRgba = setFilter(mRgba);

        // 파일 저장하기 위한 matrix
        saved_picture = mRgba;


        return mRgba;
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

    public Mat putMask(Mat src, Point center, Size mask_size, int effect_number){

        // 그려질 영역 범위가 화면 밖일 때
        if((int)(center.x - mask_size.width/2) < 0 || (int)(center.y - mask_size.height/2) < 0 ||
                (int) (center.x + mask_size.width) > src.width() || (int) (center.y + mask_size.height) > src.height())
            return src;

        Mat mask_resized = new Mat();
        Mat src_roi = new Mat();
        Mat roi_gray = new Mat();

        // 사용자가 선택한 효과 size 변경
        switch(effect_number)
        {
            case 1:
                Imgproc.resize(effect1, mask_resized, mask_size);
                break;
            case 2:
                Imgproc.resize(effect2, mask_resized, mask_size);
                break;
            case 3:
                Imgproc.resize(effect3, mask_resized, mask_size);
                break;
            case 4:
                Imgproc.resize(effect4, mask_resized, mask_size);
                break;
            case 5:
                Imgproc.resize(effect5, mask_resized, mask_size);
                break;
            case 6:
                Imgproc.resize(effect6, mask_resized, mask_size);
                break;
            default:
                Imgproc.resize(effect1, mask_resized, mask_size);
                break;
        }


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

    // 사용자가 선택한 filter를 matrix에 적용
    public Mat setFilter(Mat mRgba)
    {
        switch (viewMode) {
            case PictureActivity.VIEW_MODE_RGBA:
                break;

            case PictureActivity.VIEW_MODE_GRAY:
                Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2GRAY);
                break;

            case PictureActivity.VIEW_MODE_CANNY:
                rgbaInnerWindow = mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                rgbaInnerWindow.release();
                break;

            case PictureActivity.VIEW_MODE_SOBEL:
                Mat gray = new Mat();
                Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_BGRA2GRAY);
                Mat grayInnerWindow = gray.submat(0, gray.height(), 0, gray.width());
                rgbaInnerWindow = mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                rgbaInnerWindow.release();
                break;

            case PictureActivity.VIEW_MODE_SEPIA:
                rgbaInnerWindow = mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
                rgbaInnerWindow.release();
                break;

            case PictureActivity.VIEW_MODE_BLUE:
                rgbaInnerWindow= mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mBlueKernel);
                rgbaInnerWindow.release();
                break;

            case PictureActivity.VIEW_MODE_GREEN:
                rgbaInnerWindow = mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Core.transform(rgbaInnerWindow, rgbaInnerWindow, mGreenKernel);
                rgbaInnerWindow.release();
                break;

            case PictureActivity.VIEW_MODE_POSTERIZE:
                rgbaInnerWindow = mRgba.submat(0, mRgba.height(), 0, mRgba.width());
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1./16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
                rgbaInnerWindow.release();
                break;
        }

       return mRgba;
    }

    // 효과가 표시될 point를 list에 추가
    public void addPoint(Point thumb_point, int effect_number)
    {
        if(thumb_trace.size() > max_trace)
        {
            thumb_trace.remove(0);
            thumb_effect.remove(0);
        }

        thumb_trace.add(thumb_point);
        thumb_effect.add(effect_number);
    }

    public void setAlpha(int effect_number)
    {
        float selected = 1.0f, unselected = 0.4f;

        // 선택된 효과 imageView 진하게 변경
        switch(effect_number)
        {
            case 1:
                imageView_effect1.setAlpha(selected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(unselected);
                break;
            case 2:
                imageView_effect1.setAlpha(unselected);
                imageView_effect2.setAlpha(selected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(unselected);
                break;
            case 3:
                imageView_effect1.setAlpha(unselected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(selected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(unselected);
                break;
            case 4:
                imageView_effect1.setAlpha(unselected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(selected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(unselected);
                break;
            case 5:
                imageView_effect1.setAlpha(unselected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(selected);
                imageView_effect6.setAlpha(unselected);
                break;
            case 6:
                imageView_effect1.setAlpha(unselected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(selected);
                break;
            default:
                imageView_effect1.setAlpha(selected);
                imageView_effect2.setAlpha(unselected);
                imageView_effect3.setAlpha(unselected);
                imageView_effect4.setAlpha(unselected);
                imageView_effect5.setAlpha(unselected);
                imageView_effect6.setAlpha(unselected);
                break;
        }
    }

    private void galleryAddPic(String _path)
    {
        // 저장된 사진 표시 위해 앨범 목록 갱신
        MediaScannerConnection.scanFile(this,
                new String[]{_path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }
}
