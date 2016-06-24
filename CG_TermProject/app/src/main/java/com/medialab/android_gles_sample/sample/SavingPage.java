package com.medialab.android_gles_sample.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.medialab.android_gles_sample.R;
import com.medialab.android_gles_sample.SampleView;
import com.medialab.android_gles_sample.renderer.BasicShader;
import com.medialab.android_gles_sample.renderer.FileLoader;
import com.medialab.android_gles_sample.renderer.TexData;

import java.io.InputStream;

public class SavingPage extends SampleView {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        ImageView iv = (ImageView) findViewById(R.id.imageView2);
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.main);
        iv.setImageBitmap(image);
        iv.setScaleType(ImageView.ScaleType.FIT_XY); // 레이아웃 크기에 이미지를 맞춘다
        SoundPool sound= new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        int svae = sound.load(this, R.raw.cheerup, 1);
        int save_id = sound.play(svae, 1.0f, 1.0f, 1, -1, 1.0f);
    }
    @Override
    public void OnInit()
    {

    }

    protected void onResume() {
        super.onResume();
    }

    //리스너 해제
    protected void onPause() {
        super.onPause();
    }

}
