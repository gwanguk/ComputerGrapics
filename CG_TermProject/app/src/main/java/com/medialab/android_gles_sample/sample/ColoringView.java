package com.medialab.android_gles_sample.sample;

import android.os.Bundle;

import com.medialab.android_gles_sample.SampleView;
import com.medialab.android_gles_sample.renderer.FileLoader;

import java.io.InputStream;

public class ColoringView extends SampleView {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	@Override
	public void OnInit()
	{
		String vs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.vs");
		String fs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.mShader);


		InputStream teapot = FileLoader.GetStream(this, "obj3d/cube.obj");

	//	TexData[] textJ = new TexData[1];
	//	textJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_c_brick);

		mRenderer.SetNewModel(teapot, mRenderer.terrian);
	//	mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ);

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
