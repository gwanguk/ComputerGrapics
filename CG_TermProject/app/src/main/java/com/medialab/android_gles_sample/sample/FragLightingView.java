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
		mRenderer.SetProgram(vs, fs);

		InputStream mountain = FileLoader.GetStream(this, "obj3d/mountain");

		TexData[] textJ = new TexData[1];
		textJ[0] =  FileLoader.ReadTexture(this, R.drawable.mountain);


		mRenderer.SetNewModel(mountain, mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ, mRenderer.terrian);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(0.0f, 10.0f, 30.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
