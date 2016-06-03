package com.medialab.android_gles_sample;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLViewCallback implements GLSurfaceView.Renderer{
    SampleView curView;

    public GLViewCallback(SampleView view) {
        curView = view;
    }


    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Create a minimum supported OpenGL ES context, then check:
        String version = gl.glGetString(
                GL10.GL_VERSION);
        Log.w("GLESVERSION", "Version: " + version);
        // The version format is displayed as: "OpenGL ES <major>.<minor>"
        // followed by optional content provided by the implementation.

        curView.OnInit();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        curView.OnWindowUpdate(width, height);
    }

    public float deltaTime;
    public TextView tvFpsText;
    public TextView sensor;
    private double prevTime = System.currentTimeMillis();
    private final Handler mHandler = new Handler();
    private int numFrame = 0;
    private float accumTime = 0;
    private float fps;
    public float accX=0;
    public float accY=0;
    public float accZ=0;

    public void onDrawFrame(GL10 gl) {

        //FPS counter
        numFrame++;
        // Calculate delta time for smooth camera moving or animation
        double currTime = System.currentTimeMillis();
        deltaTime = Math.min((float) (currTime - prevTime), 200.0f); // milliseconds
        accumTime += deltaTime;
        prevTime = currTime;
        if(accumTime > 250.0f) {
            fps = numFrame / (accumTime / 1000);
            accumTime = 0;
            numFrame = 0;
        }

        new Thread(new Runnable() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        if(tvFpsText != null) tvFpsText.setText("FPS : " + String.format("%.2f", fps));
                        if(sensor!=null) sensor.setText(String.format("accX : %.2f, accY : %.2f, accZ : %.2f",accX, accY, accZ));
                    }
                });
            }
        }).start();

        // For call renderer 'RenderFrame'
        curView.OnStep();
    }
}
