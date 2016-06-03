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


		InputStream terrian = FileLoader.GetStream(this, "obj3d/rectangle");
		InputStream target = FileLoader.GetStream(this, "obj3d/bird.obj");
		InputStream background = FileLoader.GetStream(this, "obj3d/cube.obj");
		InputStream aim = FileLoader.GetStream(this, "obj3d/rectangle");

		TexData[] text_terrian = new TexData[1]; //지형
		text_terrian[0] =  FileLoader.ReadTexture(this, R.drawable.mountain);

		TexData[] text_target = new TexData[1]; //타겟
		text_target[0] =  FileLoader.ReadTexture(this, R.drawable.tex_c_brick);

		TexData[] text_background = new TexData[1]; //배경
		text_background[0] =  FileLoader.ReadTexture(this, R.drawable.background);

		TexData[] text_aim = new TexData[1]; //배경
		text_aim[0] =  FileLoader.ReadTexture(this, R.drawable.aim);

		mRenderer.SetNewModel(terrian, mRenderer.terrian);
		mRenderer.SetNewModel(background, mRenderer.background);
		mRenderer.SetNewModel(aim, mRenderer.aim);
		mRenderer.SetNewModel(target, mRenderer.target);

		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, text_terrian, mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_target, mRenderer.target);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_aim, mRenderer.aim);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_background, mRenderer.background);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(0.0f, 5.0f, 30.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

	protected void onResume() {
		super.onResume();
	}

	//리스너 해제
	protected void onPause() {
		super.onPause();
	}

}
