package com.medialab.android_gles_sample.sample;

import com.medialab.android_gles_sample.R;
import com.medialab.android_gles_sample.SampleView;
import com.medialab.android_gles_sample.renderer.BasicShader;
import com.medialab.android_gles_sample.renderer.FileLoader;
import com.medialab.android_gles_sample.renderer.TexData;

import java.io.InputStream;

public class NormalMappingView extends SampleView {

	@Override
	public void OnInit()
	{
		String vs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.vs");
		String fs = FileLoader.ReadTxtFile(this, "shader/terrian/terrian.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.mShader);

		vs = FileLoader.ReadTxtFile(this, "shader/terrian/bird.vs");
		fs = FileLoader.ReadTxtFile(this, "shader/terrian/bird.fs");
		mRenderer.SetProgram(vs, fs, mRenderer.targetShader);

		InputStream teapot = FileLoader.GetStream(this, "obj3d/teapot");

		TexData[] textJ = new TexData[1];
		TexData[] normal_texJ = new TexData[1];
		textJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_c_brick);
		normal_texJ[0] = FileLoader.ReadTexture(this, R.drawable.tex_n_brick);

		mRenderer.SetNewModel(teapot, mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_GENERAL, textJ,mRenderer.terrian);
		mRenderer.SetTexture(TexData.Type.TEXDATA_NORMAL_MAP, normal_texJ,mRenderer.terrian);

		mRenderer.Initialize();

		//mViewRenderer->OffAutoRotate();
		mRenderer.GetCamera().SetEye(25.0f, 25.0f, 25.0f);
		mRenderer.GetCamera().SetAt(0, 0, 0);
	}

}
