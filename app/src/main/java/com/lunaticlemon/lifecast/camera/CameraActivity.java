package com.lunaticlemon.lifecast.camera;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.lunaticlemon.lifecast.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, OnLocationChangedListener, SensorEventListener {

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraviewOn = false;
    private com.lunaticlemon.lifecast.camera.AugmentedPOI mPoi;

    private SensorManager sensorManager;
    private Sensor acc, mag;
    float mGravity[];
    float mMagnetic[];


    private double mAzimuthReal = 0;
    private double mAzimuthTeoretical = 0;
    private static double AZIMUTH_ACCURACY = 5;
    private double mMyLatitude = 0;
    private double mMyLongitude = 0;

    private com.lunaticlemon.lifecast.camera.MyCurrentAzimuth myCurrentAzimuth;
    private com.lunaticlemon.lifecast.camera.MyCurrentLocation myCurrentLocation;

    pl.droidsonroids.gif.GifTextView pointerIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pointerIcon = (pl.droidsonroids.gif.GifTextView) findViewById(R.id.icon);

        setupListeners();
        setupLayout();
        setAugmentedRealityPoint();
    }

    private void setAugmentedRealityPoint() {
        mPoi = new com.lunaticlemon.lifecast.camera.AugmentedPOI("name", "description", 50, 19.93919566   );
    }

    public double calculateTeoreticalAzimuth() {
        double dX = mPoi.getPoiLatitude() - mMyLatitude;
        double dY = mPoi.getPoiLongitude() - mMyLongitude;

        double phiAngle;
        double tanPhi;
        double azimuth = 0;

        tanPhi = Math.abs(dY / dX);
        phiAngle = Math.atan(tanPhi);
        phiAngle = Math.toDegrees(phiAngle);

        if (dX > 0 && dY > 0) { // I quater
            return azimuth = phiAngle;
        } else if (dX < 0 && dY > 0) { // II
            return azimuth = 180 - phiAngle;
        } else if (dX < 0 && dY < 0) { // III
            return azimuth = 180 + phiAngle;
        } else if (dX > 0 && dY < 0) { // IV
            return azimuth = 360 - phiAngle;
        }

        return phiAngle;
    }

    private List<Double> calculateAzimuthAccuracy(double azimuth) {
        double minAngle = azimuth - AZIMUTH_ACCURACY;
        double maxAngle = azimuth + AZIMUTH_ACCURACY;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    private boolean isHeading(int axis_x)
    {
        //Toast.makeText(this, "x" + axis_x, Toast.LENGTH_SHORT).show();

        if(axis_x >= 3)
            return true;
        else
            return false;

        // oreintation
        /*
        boolean success = SensorManager.remapCoordinateSystem(getQuaternion().getMatrix4x4().getMatrix(), axisX, axisY, mRotationMatrixTransformed);
        if (success) {
            SensorManager.getOrientation(mRotationMatrixTransformed, mOrientationValues);

            for (int i = 0; i < 3; i++) {
                mOrientationDegrees[i] = (float) Math.toDegrees(mOrientationValues[i]);
            }
//And for look through, add the rotation state
            if (mMode == MODE.LOOK_THROUGH) {
                // look through has different angles depending on rotation state
                switch (screenRotation) {
                    case Surface.ROTATION_90: {
                        mOrientationDegrees[2] += 90;
                        break;
                    }
                    case Surface.ROTATION_180: {
                        mOrientationDegrees[2] += 180;
                        break;
                    }
                    case Surface.ROTATION_270: {
                        mOrientationDegrees[2] += 270;
                        break;
                    }
                }
            }*/
    }

    private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
                return true;
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true;
        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLatitude = location.getLatitude();
        mMyLongitude = location.getLongitude();
        mAzimuthTeoretical = calculateTeoreticalAzimuth();

        //Toast.makeText(this,"latitude: "+location.getLatitude()+" longitude: "+location.getLongitude() + mAzimuthTeoretical, Toast.LENGTH_SHORT).show();

        updateDescription();
    }

    private void updateDescription() {
        Toast.makeText(this, mPoi.getPoiName() + " azimuthTeoretical "
                + mAzimuthTeoretical + " azimuthReal " + mAzimuthReal + " latitude "
                + mMyLatitude + " longitude " + mMyLongitude, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        myCurrentLocation.stop();

        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myCurrentLocation.start();

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setupListeners() {
        myCurrentLocation = new com.lunaticlemon.lifecast.camera.MyCurrentLocation(this);
        myCurrentLocation.buildGoogleApiClient(this);
        myCurrentLocation.start();

    }

    private void setupLayout() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraPreview);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (isCameraviewOn) {
            mCamera.stopPreview();
            isCameraviewOn = false;
        }

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
                isCameraviewOn = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isCameraviewOn = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int dv1 = 0;

        switch(sensorEvent.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = sensorEvent.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetic = sensorEvent.values;
                break;
        }

        if(mGravity != null && mMagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];

            if(SensorManager.getRotationMatrix(R, I, mGravity, mMagnetic))
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                dv1 = (int)(orientation[2] * 9);
            }
        }

        if(isHeading(dv1))
        {
            pointerIcon.setVisibility(View.VISIBLE);
        }
        else
        {
            pointerIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
