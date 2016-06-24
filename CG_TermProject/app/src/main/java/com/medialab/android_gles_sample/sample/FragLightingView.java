package com.medialab.android_gles_sample.sample;

import android.graphics.BitmapFactory;

import com.medialab.android_gles_sample.R;
import com.medialab.android_gles_sample.SampleView;
import com.medialab.android_gles_sample.joml.Vector3f;
import com.medialab.android_gles_sample.renderer.FileLoader;
import com.medialab.android_gles_sample.renderer.TexData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FragLightingView extends SampleView  {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	@Override
	public void OnInit()
	{
		String vs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.vs");
		String fs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.mShader);
		vs = FileLoader.ReadTxtFile(this, "shader/bird/bird.vs");
		fs = FileLoader.ReadTxtFile(this, "shader/bird/bird.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.targetShader);
		vs = FileLoader.ReadTxtFile(this, "shader/noop.vs");
		fs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.zoomShader);
		vs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.vs");
		fs = FileLoader.ReadTxtFile(this, "shader/blur.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.blurShader);


		InputStream terrian = FileLoader.GetStream(this, "obj3d/green_mountain.obj");
		InputStream target = FileLoader.GetStream(this, "obj3d/van.obj");
		InputStream car = FileLoader.GetStream(this, "obj3d/van.obj");
		InputStream background = FileLoader.GetStream(this, "obj3d/cube.obj");
		InputStream aim = FileLoader.GetStream(this, "obj3d/aim.obj");
		InputStream zoom = FileLoader.GetStream(this, "obj3d/zoom.obj");
		InputStream bullet = FileLoader.GetStream(this, "obj3d/bullet.obj");
		InputStream floor = FileLoader.GetStream(this, "obj3d/rectangle");
		InputStream trees = FileLoader.GetStream(this, "obj3d/trees.obj");
		InputStream house = FileLoader.GetStream(this, "obj3d/house.obj");
		InputStream horse = FileLoader.GetStream(this, "obj3d/horse.obj");
		InputStream gun = FileLoader.GetStream(this, "obj3d/gun.obj");
		InputStream burst = FileLoader.GetStream(this, "obj3d/burst.obj");

		TexData[] text_terrian = new TexData[1]; //지형
		text_terrian[0] =  FileLoader.ReadTexture(this, R.drawable.green_mountain);
		TexData[] text_target = new TexData[1]; //타겟
		text_target[0] =  FileLoader.ReadTexture(this, R.drawable.van1);
		TexData[] text_background = new TexData[1]; //배경
		text_background[0] =  FileLoader.ReadTexture(this, R.drawable.background4);
		TexData[] text_aim = new TexData[1]; //배경
		text_aim[0] =  FileLoader.ReadTexture(this, R.drawable.red);
		TexData[] text_zoom = new TexData[1]; //zoom
		text_zoom[0] =  FileLoader.ReadTexture(this, R.drawable.zoom);
		TexData[] text_bullet = new TexData[1]; //zoom
		text_bullet[0] =  FileLoader.ReadTexture(this, R.drawable.brass);
		TexData[] text_floor = new TexData[1]; //zoom
		text_floor[0] =  FileLoader.ReadTexture(this, R.drawable.floor);
		TexData[] text_trees = new TexData[1]; //zoom
		text_trees[0] =  FileLoader.ReadTexture(this, R.drawable.mountaincolor2);
		TexData[] text_house = new TexData[1]; //zoom
		text_house[0] =  FileLoader.ReadTexture(this, R.drawable.house);
		TexData[] text_horse = new TexData[1]; //zoom
		text_horse[0] =  FileLoader.ReadTexture(this, R.drawable.horse);
		TexData[] text_gun = new TexData[1]; //zoom
		text_gun[0] =  FileLoader.ReadTexture(this, R.drawable.gun);
		TexData[] text_car = new TexData[1]; //zoom
		text_car[0] =  FileLoader.ReadTexture(this, R.drawable.car);
		TexData[] text_burst = new TexData[1]; //zoom
		text_burst[0] =  FileLoader.ReadTexture(this, R.drawable.red);
		TexData[] text_effect1 = new TexData[1]; //zoom
		text_effect1[0] =  FileLoader.ReadTexture(this, R.drawable.effect1);
		TexData[] text_effect2 = new TexData[1]; //zoom
		text_effect2[0] =  FileLoader.ReadTexture(this, R.drawable.effect2);

		mRenderer.SetNewModel(terrian, mRenderer.terrian);
		mRenderer.SetNewModel(background, mRenderer.background);
		mRenderer.SetNewModel(aim, mRenderer.aim);
		mRenderer.SetNewModel(target, mRenderer.target);
		mRenderer.SetNewModel(car, mRenderer.car);
		mRenderer.SetNewModel(zoom, mRenderer.zoom);
		mRenderer.SetNewModel(bullet, mRenderer.bullet);
		mRenderer.SetNewModel(floor, mRenderer.floor);
		mRenderer.SetNewModel(trees, mRenderer.trees);
		mRenderer.SetNewModel(house, mRenderer.house);
		mRenderer.SetNewModel(horse, mRenderer.horse);
		mRenderer.SetNewModel(gun, mRenderer.gun);
		mRenderer.SetNewModel(burst, mRenderer.burst);

		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, text_terrian, mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_target, mRenderer.target);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_aim, mRenderer.aim);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_background, mRenderer.background);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_zoom, mRenderer.zoom);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_bullet, mRenderer.bullet);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_floor, mRenderer.floor);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_trees, mRenderer.trees);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_house, mRenderer.house);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_horse, mRenderer.horse);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_gun, mRenderer.gun);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_car, mRenderer.car);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_burst, mRenderer.burst);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_effect1, mRenderer.effect_tex1);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_effect2, mRenderer.effect_tex2);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(0.0f, 200.0f, 195.0f);
		mRenderer.GetCamera().SetAt(-150.0f, 20, -30.f);
	}

	protected void onResume() {
		super.onResume();
	}

	//리스너 해제
	protected void onPause() {
		super.onPause();
	}

}
