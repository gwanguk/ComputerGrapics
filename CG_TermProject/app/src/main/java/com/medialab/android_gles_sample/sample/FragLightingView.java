package com.medialab.android_gles_sample.sample;

import android.graphics.BitmapFactory;

import com.medialab.android_gles_sample.R;
import com.medialab.android_gles_sample.SampleView;
import com.medialab.android_gles_sample.joml.Vector3f;
import com.medialab.android_gles_sample.renderer.FileLoader;
import com.medialab.android_gles_sample.renderer.TexData;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FragLightingView extends SampleView {

	Vector3f BIRD= new Vector3f(0.0f, 0.0f, 0.0f);


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
		InputStream cube = FileLoader.GetStream(this, "obj3d/cube");

		TexData[] text_terrian = new TexData[1];
		text_terrian[0] =  FileLoader.ReadTexture(this, R.drawable.mountain);

		TexData[] text_target = new TexData[1];
		text_target[0] =  FileLoader.ReadTexture(this, R.drawable.tex_c_brick);

		mRenderer.SetNewModel(terrian, mRenderer.terrian);
		mRenderer.SetNewModel(cube, mRenderer.target);

		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, text_terrian, mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL,text_target, mRenderer.target);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(0.0f, 10.0f, 30.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
