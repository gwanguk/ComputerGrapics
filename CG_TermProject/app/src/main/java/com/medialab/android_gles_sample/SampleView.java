package com.medialab.android_gles_sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.content.Context;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.medialab.android_gles_sample.renderer.BasicRenderer;

public abstract class SampleView extends Activity implements SensorEventListener {
    private GLView mGLView;
    private GLViewCallback mGLViewCallback;
    protected BasicRenderer mRenderer;
    int accX;
    int accY;
    int accZ;
    private SensorManager mSensorManager;
    private SensorEventListener gyroListener;
    private Sensor accSensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLView = new GLView(this);
        mGLViewCallback = new GLViewCallback(this);
        mRenderer = new BasicRenderer();


        mGLView.setRenderer(mGLViewCallback);
        setContentView(mGLView);
        addUi();



        super.onCreate(savedInstanceState);

        //센서 매니저 얻기
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //자이로스코프 센서(회전)
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    //정확도에 대한 메소드 호출 (사용안함)
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            accX= (int)(event.values[0] );
            accY= (int)(event.values[1] );
            accZ= (int)(event.values[2] );
            mGLViewCallback.accX =accX;
            mGLViewCallback.accY= accY;
            mGLViewCallback.accZ  =accZ;
            mRenderer.accX= accX;
            mRenderer.accY=accY;
            mRenderer.accZ=accZ;

        }

    }
    @Override
    protected void onPause() {
        mGLView.onPause();
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        mGLView.onResume();
        super.onResume();
        mSensorManager.registerListener(this, accSensor,SensorManager.SENSOR_DELAY_FASTEST);
    }
    public void addUi() {
        View btnLayout = getLayoutInflater().inflate(R.layout.sample_ui, null);
        this.addContentView(btnLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        mGLViewCallback.tvFpsText  = (TextView) findViewById(R.id.tvFps);
        mGLViewCallback.sensor = (TextView) findViewById(R.id.sensor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){

        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                mRenderer.TouchOff();
                break;

            case MotionEvent.ACTION_MOVE:
                mRenderer.SetTouchPoint(e.getX(), e.getY());
                break;

            case MotionEvent.ACTION_DOWN:
                mRenderer.TouchOn();
                mRenderer.SetTouchPoint(e.getX(), e.getY());
                break;
        }

        return super.onTouchEvent(e);
    }


    protected abstract void OnInit();

    protected void OnWindowUpdate(int w, int h)
    {
        mRenderer.SetViewPort(w, h);
    }

    protected void OnStep()
    {
        mRenderer.RenderFrame();
    }


}
