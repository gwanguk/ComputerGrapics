package com.medialab.android_gles_sample;

import android.media.AudioManager;
import android.media.SoundPool;

import android.content.Context;

import com.medialab.android_gles_sample.joml.Quaternionf;
import com.medialab.android_gles_sample.joml.Vector2f;
import com.medialab.android_gles_sample.renderer.BasicCamera;
import com.medialab.android_gles_sample.renderer.BasicShader;

/**
 * Created by KwangUk on 2016-06-24.
 */
public class Sound {
    SoundPool sound;
    int bgm;
    int bgm_id;

    int shot;
    int shot_id;

    int zoom;
    int zoom_id;

    int heart;
    int heart_id;

    int burst;
    int burst_id;

    int miss;
    int miss_id;

    int fly;
    int fly_id;

    int cheerup;
    int cheerup_id;
    public Sound(Context context) {
        sound = new SoundPool(8, AudioManager.STREAM_MUSIC,0);
        bgm  = sound.load(context, R.raw.bgm, 1);
        shot  = sound.load(context, R.raw.shot, 1);
        zoom = sound.load(context, R.raw.zoom,1);
        heart = sound.load(context, R.raw.heart,1);
        burst = sound.load(context, R.raw.burst,1);
        miss = sound.load(context, R.raw.miss,1);
        fly = sound.load(context, R.raw.fly,1);
        cheerup = sound.load(context, R.raw.cheerup,1);
    }

    public void sound_play(int num)
    {
        if(num==1)
             bgm_id = sound.play(bgm,1.0f,1.0f,1,-1,1.0f);
        if(num==2)
              shot_id = sound.play(shot,1.0f,1.0f,1,0,1.0f);
        if(num==3)
            zoom_id = sound.play(zoom,1.0f,1.0f,1,0,1.0f);
        if(num==4)
            heart_id = sound.play(heart,1.0f,1.0f,1,-1,1.0f);
        if(num==5)
            burst_id = sound.play(burst,1.0f,1.0f,1,0,1.0f);
        if(num==6)
            miss_id = sound.play(miss,1.0f,1.0f,1,0,1.0f);
        if(num==7)
            fly_id = sound.play(fly,1.0f,1.0f,1,0,1.0f);
        if(num==8)
            cheerup_id = sound.play(cheerup,1.0f,1.0f,1,0,1.0f);
    }
    public void sound_stop(int num)
    {
        if(num==1)
             sound.stop(bgm_id);
        if(num==2)
            sound.stop(shot_id);
        if(num==3)
            sound.stop(zoom_id);
        if(num==4)
            sound.stop(heart_id);
        if(num==5)
            sound.stop(burst_id);
        if(num==6)
            sound.stop(miss_id);
        if(num==7)
            sound.stop(fly_id);
        if(num==8)
            sound.stop(cheerup_id);

    }

}
