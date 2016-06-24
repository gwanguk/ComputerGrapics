package com.medialab.android_gles_sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

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
    public TextView bullet;
    public TextView car;
    public TextView time;
    public TextView result;
    private double prevTime = System.currentTimeMillis();
    private final Handler mHandler = new Handler();
    private int numFrame = 0;
    private float accumTime = 0;
    private float fps;
    public float accX=0;
    public float accY=0;
    public float accZ=0;
    public int remaining_bullet=6;
    public int remaining_car=5;
    public int remaining_time=100;
    public String result_text ="";

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
            //accumTime = 0;
            numFrame = 0;
        }

        new Thread(new Runnable() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        if(time != null) time.setText("TIME : " + String.format("%d second", remaining_time));
                        if(car!=null) car.setText(String.format("Remaning Enemy: %d",remaining_car));
                        if(bullet!=null) bullet.setText(String.format("Remaing Bullet : %d", remaining_bullet));
                        if(bullet!=null) result.setText(String.format("%s", result_text));
                    }
                });
            }
        }).start();

        // For call renderer 'RenderFrame'
        curView.OnStep();
    }
}
