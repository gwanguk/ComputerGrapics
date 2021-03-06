package com.medialab.android_gles_sample.renderer;


import android.content.Context;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;
import android.view.VelocityTracker;

//import com.medialab.android_gles_sample.R;
import com.medialab.android_gles_sample.Sound;
import com.medialab.android_gles_sample.joml.AxisAngle4f;
import com.medialab.android_gles_sample.joml.Matrix3f;
import com.medialab.android_gles_sample.joml.Matrix4f;
import com.medialab.android_gles_sample.joml.Quaternionf;
import com.medialab.android_gles_sample.joml.Vector2f;
import com.medialab.android_gles_sample.joml.Vector3f;
import com.medialab.android_gles_sample.joml.Vector4f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class BasicRenderer {

	private static String TAG = "BasicRenderer";

	class VertexStrcut {
		public Vec3 pos;
		public Vec3 nor;
		public Vec3 tex;

	}
	class OBJECT{
		private FloatBuffer mVertexData;
		private ShortBuffer mIndices;
		private FloatBuffer mTangentData;

		int mVertexSize;
		int mIndexSize;
		int mTangentSize;

		int faceComponentNum;

		// vertex buffer object and index buffer object
		int[] mVboVertices = {0};
		int[] mVboIndices = {0};
		int[] mVboTangents = {0};

		// variables for texture handling
		boolean mHasTexture;
		boolean mHasNorMap;

		// Texture object id
		int[] mTexId = {0};
		int[] mTexNorId = {0};

		public OBJECT()		{
			mHasTexture = false;
			mHasNorMap = false;
			faceComponentNum =3;

			mIndexSize = 0;
		}
	}


	Vector3f origin = new Vector3f(0,0,0);
	Vector3f origin_At = new Vector3f(0,10,0);
	Vector3f origin_Eye = new Vector3f(0.0f, 2.0f, 30.0f);

	public static int V_ATTRIB_POSITION = 0;
	public static int V_ATTRIB_NORMAL = 1;
	public static int V_ATTRIB_TEX = 2;
	public static int V_ATTRIB_TANGENT = 3;
	public static int V_ATTRIB_INSTPOSITION = 5;
	public static int TEX_POS_NORMAL = 6;
	public static int TEX_POS_CUBEMAP = 7;

	public static int GUN = 0;

	protected int mWidth;
	protected int mHeight;
	protected double mDeltaTime;

	public	int accX;
	public	int accY;
	public	int accZ;

	public OBJECT terrian ;
	public OBJECT target;
	public OBJECT car;
	public OBJECT aim;
	public OBJECT background;
	public OBJECT zoom;
	public OBJECT bullet;
	public OBJECT floor;
	public OBJECT trees;
	public OBJECT house;
	public OBJECT horse;
	public OBJECT gun;
	public OBJECT burst;

	public BasicShader mShader;
	public BasicShader targetShader;
	public BasicShader backgroundShader;
	public BasicShader zoomShader;
	public BasicShader blurShader;

	public BasicCamera mCamera;

	boolean mIsAutoRotateEye;
	boolean mIsFill;

	static boolean mIsTouchOn;
	static Vector2f mTouchPoint;

	static Quaternionf startRotQuat;
	static Quaternionf lastRotQuat;
	static Vector2f ancPts;
	static boolean isUpdateAnc;

	float bullet_speed = 25;
	float target_speed = 0.5f;
	float slow_speed = 1;

	public int remain_time=100;
	public int remain_bullet=10;
	public int remain_cars=5;
	public String result_text="";

	int[] car_table =new int[5];

	int ready=0;
	int fired=0;
	int hit=0;
	int effect=0;

	public	int[] effect_tex1 = {0};
	public int[] effect_tex2 = {0};
	public int[] effect_tex3 = {0};

	int render_start=0;

	Sound mSound;
	Context _context;

	// vertex buffer

	public BasicRenderer(Context context) {
		_context =context;
		mWidth = 0;
		mHeight = 0;
		mDeltaTime = 0;
		mIsAutoRotateEye = true;
		mIsFill = true;
		mIsTouchOn = false;
		mTouchPoint = new Vector2f(0);

		startRotQuat = new Quaternionf();
		lastRotQuat = startRotQuat;
		ancPts = new Vector2f(mTouchPoint);
		isUpdateAnc = false;

		mCamera = new BasicCamera();
		mShader = new BasicShader();
		targetShader = new BasicShader();
		backgroundShader = new BasicShader();
		zoomShader = new BasicShader();
		blurShader = new BasicShader();


		terrian = new OBJECT();
		target = new OBJECT();
		car = new OBJECT();
		aim = new OBJECT();
		background = new OBJECT();
		zoom = new OBJECT();
		bullet = new OBJECT();
		floor =new OBJECT();
		trees =new OBJECT();
		house =new OBJECT();
		horse =new OBJECT();
		gun =new OBJECT();
		burst =new OBJECT();

		accX=0;
		accY=0;
		accZ=0;

		car_table[0]=1;car_table[1]=1;car_table[2]=1;car_table[3]=1;car_table[4]=1;

	}

	public BasicCamera GetCamera() {
		return mCamera;
	}

	// Interface functions
/// Sets vertex shader and fragment shader for rendering
	public boolean SetProgram(String vertexSource, String fragmentSource, BasicShader shader) {
		shader.CreateProgram(vertexSource, fragmentSource);

		if (shader.GetProgram() == 0) {
			Log.e(TAG, "Could not create program.\n");
			return false;
		}

		//mShader.Use();

		return true;
	}

	public void SetSound(Sound _sound)
	{
		this.mSound=_sound;
	}


	/****************************
	 * *** Interface functions ***
	 ****************************/
	public void SetNewModel(InputStream objSource, OBJECT object) {
		ImportModel(objSource, object);
	}

	public void SetNewModel(InputStream objSource, float scale, OBJECT object) {
		//ImportModel(objSource,object, object);
	}

	public void SetTexture(TexData.Type type, TexData[] newTex, OBJECT obj) {
		switch (type) {
			case TEXDATA_GENERAL: // general texture
				Log.i(TAG, "Set Texture : general\n");
				obj.mHasTexture = true;
				CreateTexBuffer(newTex[0], obj.mTexId);
				break;
			case TEXDATA_NORMAL_MAP: // normal map
				Log.i(TAG, "Set Texture : normal map\n");
				obj.mHasNorMap = true;
//				if (mTangentData.empty())
				ComputeTangent(obj);
				CreateTexBuffer(newTex[0], obj.mTexNorId);
				break;
			default:
				break;
		}
	}
	public void SetTexture(TexData.Type type, TexData[] newTex, int[] texid) {
				Log.i(TAG, "Set Texture : general\n");
				CreateTexBuffer(newTex[0], texid);
	}

	public boolean Initialize() {
		Log.i(TAG, "Initialize renderer.\n");
		LogInfo();

		CountTickInit();
		SetState();

		return true;
	}

	public void SetViewPort(int w, int h) {
		Log.i(TAG, String.format("SetViewPort(%d, %d)\n", w, h));
		mWidth = w;
		mHeight = h;
		GLES20.glViewport(0, 0, w, h);
		BasicUtils.CheckGLerror("glViewport");

		mCamera.ComputePerspective(60.0f, w, h);
	}

	public void RenderFrame() {
		//Log.i(TAG, "RenderFrame()");
		ComputeTick();

		mDeltaTime = 0.01;


		//if (mIsAutoRotateEye) mCamera.RotateAuto(mDeltaTime);

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		BasicUtils.CheckGLerror("glClear");

		Draw();
	}

	/*****************************
	 * **** Texture functions *****
	 *****************************/
	void CreateTexBuffer(TexData newTex, int[] target) {
		Log.i(TAG, "CreateTexBuffer\n");
		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		BasicUtils.CheckGLerror("glPixelStorei");
		GLES20.glGenTextures(1, target, 0);
		BasicUtils.CheckGLerror("glGenTextures");

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, target[0]);
		BasicUtils.CheckGLerror("glBindTexture");

		TexBuffer(GLES20.GL_TEXTURE_2D, newTex);

		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		BasicUtils.CheckGLerror("glGenerateMipmap");

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		BasicUtils.CheckGLerror("glTexParameteri");

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		BasicUtils.CheckGLerror("glTexParameteri");

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		BasicUtils.CheckGLerror("glTexParameteri");

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	void TexBuffer(int type, TexData newTex) {
		Log.i(TAG, "TexBuffer");

		GLES20.glTexImage2D(type, 0,
				newTex.format,
				newTex.width, newTex.height, 0,
				newTex.format,
				GLES20.GL_UNSIGNED_BYTE, newTex.pixels);

		BasicUtils.CheckGLerror("glTexImage2D");
	}

	/*******************************
	 * **** Rendering functions *****
	 *******************************/
	void SetState() {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}

	void CreateVbo(OBJECT obj) {
		Log.i(TAG, "CreateVbo\n");
		GLES20.glGenBuffers(1, obj.mVboVertices, 0);
		GLES20.glGenBuffers(1, obj.mVboIndices, 0);

		obj.mVertexData.position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, obj.mVboVertices[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
				obj.mVertexSize * 4,
				obj.mVertexData, GLES20.GL_STATIC_DRAW);

		obj.mIndices.position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, obj.mVboIndices[0]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
				obj.mIndexSize * 2,
				obj.mIndices,
				GLES20.GL_STATIC_DRAW);

		int stride = 4 * (3	+ 3 + 2); // stride: sizeof(float) * number of components
		if(obj.faceComponentNum==2) // if facecomponent is 2
			stride = 4 * (3 + 2);
		int offset = 0;
		GLES20.glEnableVertexAttribArray(V_ATTRIB_POSITION);
		GLES20.glVertexAttribPointer(V_ATTRIB_POSITION, 3, GLES20.GL_FLOAT, false, stride, offset);

		if(obj.faceComponentNum==3) {
			offset += 4 * 3;
			GLES20.glEnableVertexAttribArray(V_ATTRIB_NORMAL);
			GLES20.glVertexAttribPointer(V_ATTRIB_NORMAL, 3, GLES20.GL_FLOAT, false, stride, offset);
		}

		// If renderer has texture, we should enable vertex attribute for texCoord
		if (obj.mHasTexture || obj.mHasNorMap) {
			offset += 4 * 3;
			GLES20.glEnableVertexAttribArray(V_ATTRIB_TEX);
			GLES20.glVertexAttribPointer(V_ATTRIB_TEX, 2, GLES20.GL_FLOAT, false, stride, offset);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, obj.mTexId[0]);
		}

		if (obj.mHasNorMap) {
			// Bump mapping need to change space (world and TBN)
			// mTangentBuffer calculated by ComputeTangent() when normal texture has set
			GLES20.glGenBuffers(1, obj.mVboTangents, 0);
			obj.mTangentData.position(0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, obj.mVboTangents[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
					obj.mTangentSize, obj.mTangentData, GLES20.GL_STATIC_DRAW);

			offset = 0;
			stride = 4 * 3;
			GLES20.glEnableVertexAttribArray(V_ATTRIB_TANGENT);
			GLES20.glVertexAttribPointer(V_ATTRIB_TANGENT, 3, GLES20.GL_FLOAT, false, stride, offset);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + TEX_POS_NORMAL);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, obj.mTexNorId[0]);
		}
	}

	Vector3f GetArcballVector(Vector2f point)
	{
		float radius = 1.0f;

		Vector3f P = new Vector3f(1.0f * point.x / mWidth * 2 - 1.0f,
				1.0f * point.y / mHeight * 2 - 1.0f,
				0);
		P.y = -P.y;

		float OP_squared = P.x * P.x + P.y * P.y;
		if (OP_squared <= radius * radius)
			P.z = (float)Math.sqrt(radius * radius - OP_squared); // Pythagore
		else
			P = P.normalize(); // nearest point

		return P;
	}

	float[] GetWorldMatrix(){
		float[] farray = new float[4*4];
		FloatBuffer fb = FloatBuffer.allocate(4 * 4);

		if (mIsTouchOn)
		{
			if (!isUpdateAnc)
			{
				ancPts.set(mTouchPoint);
				isUpdateAnc = true;
				Log.i(TAG, "Anchor Updated\n");
			}
			else
			{
				if (ancPts.x != mTouchPoint.x || ancPts.y != mTouchPoint.y)
				{
					// Get the vectors on the arcball
					Vector3f va = GetArcballVector(ancPts);
					Vector3f vb = GetArcballVector(mTouchPoint);

					// Get the rotation axis and the angle between the vector
					float angle = (float)Math.acos(Math.min(1.0f, va.dot(vb))) * 2.0f;

					Vector3f axisInCameraSpace = va.cross(vb).normalize();


					fb.put(GetCamera().GetViewMat(origin));
					fb.position(0);
					Matrix4f cameraToObjectSpace = new Matrix4f(fb).invert();
					Vector3f axisInObjectSpace = new Matrix3f(cameraToObjectSpace).transform(axisInCameraSpace).normalize();

					Quaternionf curRotQuat = new Quaternionf(new AxisAngle4f(angle, axisInObjectSpace.x, axisInObjectSpace.y, axisInObjectSpace.z));
					lastRotQuat = curRotQuat.mul(startRotQuat).normalize();
				}
			}
		}
		else
		{
			startRotQuat = lastRotQuat;
			isUpdateAnc = false;
		}
		Matrix4f rotationMat = new Matrix4f();
		lastRotQuat.get(rotationMat);
		rotationMat.get(farray);

		return farray;
	}


	Vector3f target_location = new Vector3f();
	Vector3f target_origin = new Vector3f(-400.0f, 0.0f,-20.0f);


	float moving_seed=0;
	int vib=1;
	float[] GetWorldMatrix_TARTGET()
	{
		moving_seed+=0.2*target_speed*slow_speed;
		target_location.set(target_origin.x+moving_seed,0.0f
				,	target_origin.z);
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		rotationMat.rotate((float)Math.PI/2,0.0f,-1.0f,0.0f);

		out = (transMat.translation(target_location.x , target_location.y, target_location.z).mul(rotationMat.mul(scaleMat.scale(0.05f))));

		out.get(farray);

		return farray;
	}

	Vector3f ter0 = new Vector3f(-520.0f, 0.0f,-20.0f);
	Vector3f ter1 = new Vector3f(-420.0f, 0.0f,-15.0f);
	Vector3f ter2 = new Vector3f(-490.0f, 0.0f,-17.0f);
	Vector3f ter3 = new Vector3f(-450.0f, 0.0f,-19.0f);
	Vector3f ter4 = new Vector3f(-550.0f, 0.0f,-17.0f);
	Vector3f ter0_location = new Vector3f();
	Vector3f ter1_location = new Vector3f();
	Vector3f ter2_location = new Vector3f();
	Vector3f ter3_location = new Vector3f();
	Vector3f ter4_location = new Vector3f();
	float left_right_seed=0;
	float[] GetWorldMatrix_CAR()
	{
		left_right_seed+=0.009f;
		if(left_right_seed>Math.PI*2)
			left_right_seed=0;
		Vector3f location = new Vector3f();
		float left_right;
		moving_seed+=0.19*target_speed*slow_speed;
		if(car_num==0)
		{
			left_right=(float)Math.sin((double)(left_right_seed)+2.5f)*slow_speed;
			ter0_location.set(ter0.x+moving_seed,0.0f,ter0.z+left_right*5);
			location.set(ter0_location);
		}
		else if(car_num==1)
		{
			left_right=(float)Math.sin((double)(left_right_seed)+2.0f)*slow_speed;
			ter1_location.set(ter1.x+moving_seed,0.0f,ter1.z+left_right*5);
			location.set(ter1_location);
		}
		else if(car_num==2)
		{
			left_right=(float)Math.sin((double)(left_right_seed)+1.5f)*slow_speed;
			ter2_location.set(ter2.x+moving_seed,0.0f,ter2.z+left_right*5);
			location.set(ter2_location);
		}
		else if(car_num==3)
		{
			left_right=(float)Math.sin((double)(left_right_seed)+1.0f)*slow_speed;
			ter3_location.set(ter3.x+moving_seed,0.0f,ter3.z+left_right*5);
			location.set(ter3_location);
		}
		else if(car_num==4)
		{
			left_right=(float)Math.sin((double)(left_right_seed)+0.5f)*slow_speed;
			ter4_location.set(ter4.x+moving_seed,0.0f,ter4.z+left_right*5);
			location.set(ter4_location);
		}

		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();

		rotationMat.rotate((float)Math.PI/2,0.0f,-1.0f,0.0f);

		out = (transMat.translation(location.x , location.y, location.z).mul(rotationMat.mul(scaleMat.scale(0.05f))));

		out.get(farray);

		return farray;
	}

	float[] GetWorldMatrix_TERRIAN(int number)
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		if(number==11) {
			transMat.translate(-50.0f, 0.0f, 150.0f);
			scaleMat.scale(40.0f,60.0f, 80.0f);
		}
		else if(number==12) {
			transMat.translate(0.0f, 0.0f, -300.0f);
			scaleMat.scale(60.0f, 60.0f, 100.0f);
		}
		rotationMat.rotation((float)(Math.PI/2),0.0f,1.0f,0.0f);

		out = (transMat.mul(rotationMat).mul(scaleMat));
		out.get(farray);

		return farray;
	}

	Vector3f Fake_EYE = new Vector3f();
	float scope_magnifiying_scale =3;
	float[] GetWorldMatrix_ZOOM()
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();
		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		out = (transMat.mul(rotationMat.mul(scaleMat)));
		out.get(farray);

		return farray;
	}

	float[] GetWorldMatrix_BACKGROUND() //3
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		Vector3f zero = new Vector3f(0,0,0);
		float[] viewMat_arr = GetCamera().GetViewMat(zero);
		viewMat.set(viewMat_arr);

		out = (transMat.translation(0.0f, 389.0f, 0.0f).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(400f))));
		out.get(farray);

		return farray;
	}
	float[] GetWorldMatrix_FLOOR() //6
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		Vector3f zero = new Vector3f(0,0,0);
		float[] viewMat_arr = GetCamera().GetViewMat(zero);
		viewMat.set(viewMat_arr);

		out = (transMat.translation(0.0f, 0.0f, 0.0f).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(420.0f,0.0f,200f))));
		out.get(farray);

		return farray;
	}

	float[] GetWorldMatrix_TREES() //6
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		rotationMat.rotate(-0.1f, 0.0f, 1.0f, 0.0f);

		out = (transMat.translation(0.0f, 0.0f, 10.0f).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(0.04f))));
		out.get(farray);

		return farray;
	}

	float[] GetWorldMatrix_HOUSE() //8
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		rotationMat.rotate(0, 0.0f, 0.0f, 0.0f);

		out = (transMat.translation(70.0f, 0.0f, -80.0f).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(0.2f))));
		out.get(farray);

		return farray;
	}
	float horse_moving_seed =0.0f;
	Vector3f horse_pos = new Vector3f(40.0f, 0.0f, -80.0f);
	float[] GetWorldMatrix_HORSE() //9
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		rotationMat.rotate(0, 0.0f, 0.0f, 0.0f);

		out = (transMat.translation(horse_pos).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(0.05f))));
		out.get(farray);

		return farray;
	}

	float[] GetWorldMatrix_GUN() //9
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Vector3f va = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f vb = new Vector3f(aim_unit);
		float angle =(float)Math.acos(va.dot(vb));
		Vector3f axis = va.cross(vb);

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat1 = new Matrix4f();
		Matrix4f rotationMat2 = new Matrix4f();
		Matrix4f rotationMat3 = new Matrix4f();
		Matrix4f rotationMat4 = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		Vector3f eye = new Vector3f(GetCamera().mEye.x,GetCamera().mEye.y,GetCamera().mEye.z);
		transMat.translation(eye.x, eye.y - 12, eye.z-5);
		rotationMat1.rotation((float) Math.PI / 2, 1.0f, 0.0f, 0.0f);
		rotationMat2.rotation((float)Math.PI/2, 0.0f, 1.0f, 0.0f);
		rotationMat3.rotation(0.1f, -1.0f, 0.0f, 0.0f);
		rotationMat4.rotation(angle, axis);

		out = (transMat.mul(rotationMat4.mul(rotationMat3.mul(rotationMat2.mul(rotationMat1))).mul(scaleMat.scale(1.0f))));
		out.get(farray);

		return farray;
	}

	Vector3f effect_location = new Vector3f();
	int hit_check(Vector3f bullet_pos)
	{
		int ret =0;
		if (bullet_pos.distance(ter0_location) < 5) {
			hit=1;
			effect_location.set(ter0_location);
			car_table[0]=0;
			ret= 1;
		}
		else if (bullet_pos.distance(ter1_location) < 5) {
			hit=1;
			effect_location.set(ter1_location);
			car_table[1]=0;
			ret= 1;

		}
		else if (bullet_pos.distance(ter2_location) < 5) {
			hit=1;
			effect_location.set(ter2_location);
			car_table[2]=0;
			ret= 1;

		}
		else if (bullet_pos.distance(ter3_location) < 5) {
			hit=1;
			effect_location.set(ter3_location);
			car_table[3]=0;
			ret= 1;

		}
		else if (bullet_pos.distance(ter4_location) < 5) {
			hit=1;
			effect_location.set(ter4_location);
			car_table[4]=0;
			ret= 1;

		}
		else {
			ret= 0;
		}

		return ret;
	}

	Vector3f bullet_pos = new Vector3f();
	float bullet_rotation_angle =0;
	float bullet_scale =1;
	Vector3f bullet_offset = new Vector3f();
	float[] GetWorldMatrix_BULLET() //5
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();
		Vector3f EYE_pos = new Vector3f(mCamera.mEye.x,mCamera.mEye.y,mCamera.mEye.z);
		float between_target_distance;
		between_target_distance= target_location.distance(bullet_pos);

		if(fired==0)
		{
			Vector3f tmp =new Vector3f();
			tmp.add(EYE_pos);
			tmp.add(aim_unit.x*40,aim_unit.y*40,aim_unit.z*40);
			bullet_pos.set(tmp);
		}
		else if(fired==1)
		{
			bullet_offset.set(aim_unit.x * bullet_speed*slow_speed,aim_unit.y*bullet_speed*slow_speed, aim_unit.z*bullet_speed*slow_speed);
			bullet_pos.add(bullet_offset);
			if(bullet_pos.y<0) {
				fired = 0;
				hit=1;
				effect=1;
				effect_location.set(last_proj_aim_location);
				slow_speed=1;
				bullet_scale=1;
				fly_start=0;
				mSound.sound_play(6);
				mSound.sound_play(1);
			}
			if(hit_check(bullet_pos)==1) {
				mSound.sound_play(1);
				mSound.sound_play(5);
				fired = 0;
				hit =1 ;
				effect=2;
				slow_speed=1;
				bullet_scale=1;
				fly_start=0;
				remain_cars--;
				if(remain_cars==0)
				{
					mSound.sound_stop(1);
					mSound.sound_stop(7);
					mSound.sound_play(8);
					result_text="GOOD!";
				}
			}
		}
		Vector3f va = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f vb = new Vector3f();
		vb.set(aim_unit);

		float angle = (float)Math.acos(va.dot(vb));
		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotatMat1 = new Matrix4f();
		Matrix4f rotatMat2 = new Matrix4f();
		Matrix4f rotatMat3 = new Matrix4f();
		transMat.translation(bullet_pos);
		bullet_rotation_angle+=1f;

		rotatMat1.rotate(bullet_rotation_angle, 1.0f, 0.0f, 0.0f);
		rotatMat2.rotate((float)Math.toRadians(90), 0.0f, 1.0f, 0.0f);
		rotatMat3.rotate(angle,va.cross(vb));

		scaleMat.scale(0.01f);

		out = (transMat.mul(rotatMat3.mul(rotatMat2.mul(rotatMat1)).mul(scaleMat)));
		out.get(farray);

		return farray;
	}

	Vector3f proj_aim_location = new Vector3f(0.0f, 0.0f,0.0f);
	Vector3f last_proj_aim_location =new Vector3f();
	Vector3f aim_location = new Vector3f(0.0f, 10.0f,-50.0f);
	Vector3f aim_unit = new Vector3f();
	float aim_speed = 3.0f;
	int set = 1;
	float aim_lastY=0, aim_lastZ=0, aim_lastX=0;
	Vector4f aim_diff= new Vector4f();
	float aim_dy=0, aim_dz=0, aim_dx=0;
	float[] GetWorldMatrix_AIM()
	{
		float left, up;
		if(set==1)
		{
			aim_lastY = accY;
			aim_lastZ = accZ;
			aim_lastX = accX;
			set=0;
		}
		aim_diff.y = accY-aim_lastY;
		aim_diff.z = accZ-aim_lastZ;
		aim_diff.x = accX-aim_lastX;
		aim_diff.w = 1.0f;

		if(fired==0) {
			if(aim_diff.z<0)
				proj_aim_location.x += (aim_diff.y+0.4) / aim_speed;
			else
				proj_aim_location.x += (aim_diff.y-0.4) / aim_speed;
			proj_aim_location.z -= aim_diff.z / aim_speed;
			//proj_aim_location.y += aim_dy / 5;


			if (proj_aim_location.x < -400.0f)
				proj_aim_location.x = -400.0f;
			if (proj_aim_location.x > 400.0f)
				proj_aim_location.x = 400.0f;
			if (proj_aim_location.z < -150.0f)
				proj_aim_location.z = -150.0f;
			if (proj_aim_location.z > 150.0f)
				proj_aim_location.z = 150.0f;
			last_proj_aim_location.set(proj_aim_location);
		}

	Vector3f EYE_position = new Vector3f(GetCamera().GetEye().x,GetCamera().GetEye().y, GetCamera().GetEye().z);

		aim_unit.set(proj_aim_location.x - EYE_position.x,
				proj_aim_location.y - EYE_position.y,
				proj_aim_location.z - EYE_position.z);
		aim_unit.normalize();
		aim_location.set(aim_unit.x * 5 + EYE_position.x, aim_unit.y*5+EYE_position.y,aim_unit.z*5+EYE_position.z);

		//aim_location.set(aim_unit.mul(10.0f).add(EYE_position));

		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();

		transMat.translation(aim_location.x, aim_location.y, aim_location.z);
		scaleMat.scale(0.03f);

		out = (transMat.mul(rotationMat.mul(scaleMat)));
		out.get(farray);

		return farray;
	}

	float current_effect=0;
	float effect_scale=0.1f;
	float[] GetWorldMatrix_BURST()
	{
		effect_scale+=0.005f;
		if(effect==1) {
			if (effect_scale > 0.3f) {
				effect_scale = 0.1f;
				hit = 0;
			}
		}
		else if(effect==2){
			if (effect_scale > 1.2f) {
				effect_scale = 0.1f;
				hit = 0;
			}
		}
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		rotationMat.rotation((float)Math.PI/2,-1.0f,0.0f,0.0f);
		float current_effect_scale=1;
		transMat.translation(effect_location);
		if(current_effect==0)
			current_effect_scale=effect_scale+0.1f;
		else if(current_effect==1)
			current_effect_scale=effect_scale;
		else if(current_effect==2)
			current_effect_scale=effect_scale-0.1f;

		scaleMat.scale(current_effect_scale/2);

		out =(transMat.mul(rotationMat.mul( scaleMat)));
		out.get(farray);

		return farray;
	}

	int zoom_mode=0;
	float magnifiying_scale =260;
	float blur_time=0;
	int fly_start=0;
	float[] GetViewMatrix()
	{
		float[] viewMat;
		if (mIsTouchOn) {
			zoom_mode=1;
			blur_time+=0.00001f;
			if(blur_time>=0.005f)
			{
				ready=1;
			}
			Vector3f at = new Vector3f(proj_aim_location.x+aim_unit.x*100,
					proj_aim_location.y+aim_unit.y*100,
					proj_aim_location.z+aim_unit.z*100);
			GetCamera().setAT(at);
			Vector3f zoomed_vec = new Vector3f(aim_unit.x*magnifiying_scale,
					aim_unit.y*magnifiying_scale,
					aim_unit.z*magnifiying_scale);
			viewMat=GetCamera().GetViewMat(zoomed_vec);
			Fake_EYE.set(mCamera.mEye.x+zoomed_vec.x,mCamera.mEye.y+zoomed_vec.y,mCamera.mEye.z+zoomed_vec.z);
		}
		else  {
			blur_time=0.0f;
			zoom_mode=0;
			GetCamera().setAT(origin_At);
			viewMat = GetCamera().GetViewMat(origin);
			Fake_EYE.set(mCamera.mEye.x, mCamera.mEye.y, mCamera.mEye.z);
			zoom_start=0;
			mSound.sound_stop(4);
		}

		if(fired==1&&zoom_mode==0)
		{
			if(fly_start==0) {
				mSound.sound_play(7);
				fly_start=1;
				remain_bullet--;
			}
			GetCamera().setAT(proj_aim_location);
			Vector3f tmp = new Vector3f();
			tmp.set(bullet_pos);
			tmp.sub(aim_unit.x*3, aim_unit.y*5, aim_unit.z*3);
			viewMat = GetCamera().GetViewMat_setEYE(tmp);
		}


		return viewMat;
	}

	float[] GetInverseTranspose(float[] inArray) {
		FloatBuffer fb = FloatBuffer.allocate(4 * 4);
		fb.put(inArray);
		fb.position(0);
		Matrix4f m = new Matrix4f(fb);

		float[] outArray = new float[4 * 4];
		m.invert().transpose().get(outArray);

		return outArray;
	}

	void PassUniform(BasicShader shader, int type, float blur) {
//		float[] worldMat = new float[16];
//		Matrix.setIdentityM(worldMat, 0);
		float[] worldMat;
		if(type==11)
			worldMat = GetWorldMatrix_TERRIAN(11);
		else if(type==12)
			worldMat = GetWorldMatrix_TERRIAN(12);
		else if(type==1)
			worldMat = GetWorldMatrix_TARTGET();
		else if(type==2)
			worldMat = GetWorldMatrix_AIM();
		else if(type==3)
			worldMat = GetWorldMatrix_BACKGROUND();
		else if(type==4)
			worldMat = GetWorldMatrix_ZOOM();
		else if(type==5)
			worldMat = GetWorldMatrix_BULLET();
		else if(type==6)
			worldMat = GetWorldMatrix_FLOOR();
		else if(type==7)
			worldMat = GetWorldMatrix_TREES();
		else if(type==8)
			worldMat = GetWorldMatrix_HOUSE();
		else if(type==9)
			worldMat = GetWorldMatrix_HORSE();
		else if(type==10)
			worldMat = GetWorldMatrix_GUN();
		else if(type==13)
			worldMat = GetWorldMatrix_CAR();
		else if(type==14)
			worldMat = GetWorldMatrix_BURST();
		else
			worldMat = GetWorldMatrix();

		float[] viewMat = GetViewMatrix();
		float[] projMat = mCamera.GetPerspectiveMat();

		shader.SetUniform("worldMat", worldMat);
		shader.SetUniform("viewMat", viewMat);
		shader.SetUniform("projMat", projMat);
		shader.SetUniform("invTransWorldMat", GetInverseTranspose(worldMat));
		shader.SetUniform("s_tex0", 0);
		shader.SetUniform("s_texNor", TEX_POS_NORMAL);
		shader.SetUniform("eyePos", mCamera.GetEye());
		shader.SetUniform("lightPos", 20.0f, 100.0f,-50.0f);
		shader.SetUniform("materialDiff", 0.8f, 1.0f, 0.7f);
		shader.SetUniform("materialSpec", 0.8f, 1.0f, 0.7f);
		shader.SetUniform("materialAmbi", 0.0f, 0.0f, 0.0f);
		shader.SetUniform("materialEmit", 0.0f, 0.0f, 0.0f);
		shader.SetUniform("materialSh", 100.0f);
		shader.SetUniform("sourceDiff", 0.7f, 0.7f, 0.7f);
		shader.SetUniform("sourceSpec", 1.0f, 1.0f, 1.0f);
		shader.SetUniform("sourceAmbi", 0.0f, 0.0f, 0.0f);
		shader.SetUniform("u_scale", 1.005f-blur,1.005f-blur,0.5f);
	}

	int car_num=0;
	int zoom_start=0;
	void Draw() {
		if(render_start==0)
		{
			mSound.sound_play(1);
			render_start=1;
		}
		remain_time = 100- (int)((target_location.x+400.0f)/800.0f *100.0f);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);

		if(ready==0&&zoom_mode==1) {
			blurShader.Use();
			CreateVbo(terrian);
			PassUniform(blurShader, 11, blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, terrian.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			PassUniform(blurShader, 12, blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, terrian.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(background);
			PassUniform(blurShader, 3, blur_time);
			GLES20.glFrontFace(GLES20.GL_CW);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, background.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			GLES20.glFrontFace(GLES20.GL_CCW);

			CreateVbo(floor);
			PassUniform(blurShader, 6, blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, floor.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(horse);
			PassUniform(blurShader, 9,blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, horse.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(trees);
			PassUniform(blurShader, 7,blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, trees.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(house);
			PassUniform(blurShader, 8,blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, house.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(target);
			PassUniform(blurShader, 1, blur_time);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, target.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(car);
			for(car_num=0;car_num<5;car_num++)
			{
				if(car_table[car_num]==1) {
					PassUniform(blurShader, 13, blur_time);
					GLES20.glDrawElements(GLES20.GL_TRIANGLES, car.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
				}
			}
		} else {
			mShader.Use();
			CreateVbo(terrian);
			PassUniform(mShader, 11, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, terrian.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			PassUniform(mShader, 12, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, terrian.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			mShader.Use();
			CreateVbo(background);
			PassUniform(mShader, 3, 1);
			GLES20.glFrontFace(GLES20.GL_CW);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, background.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			GLES20.glFrontFace(GLES20.GL_CCW);

			CreateVbo(floor);
			PassUniform(mShader, 6, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, floor.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(horse);
			PassUniform(mShader, 9, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, horse.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(trees);
			PassUniform(mShader, 7, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, trees.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(house);
			PassUniform(mShader, 8, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, house.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(target);
			PassUniform(mShader, 1, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, target.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(gun);
			PassUniform(mShader, 10, 1);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, gun.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

			CreateVbo(burst);
			if(hit==1) {
				if(effect==1)
				{
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, effect_tex2[0]);
					PassUniform(mShader, 14, 1);
					GLES20.glDrawElements(GLES20.GL_POINTS, burst.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
				}
				else if(effect==2) {
					for (current_effect = 0; current_effect < 3; current_effect++) {
						if (current_effect == 1)
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, effect_tex2[0]);
						else
							GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, effect_tex1[0]);
						PassUniform(mShader, 14, 1);
						GLES20.glDrawElements(GLES20.GL_POINTS, burst.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
					}
				}
			}

			CreateVbo(car);
			if(remain_cars>0) {
				for (car_num = 0; car_num < 5; car_num++) {
					if (car_table[car_num] == 1) {
						PassUniform(mShader, 13, 1);
						GLES20.glDrawElements(GLES20.GL_TRIANGLES, car.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
					}
				}
			}
		}

		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		targetShader.Use();
		PassUniform(targetShader, 5, 1);
		if(fired==1) {
			CreateVbo(bullet);
			GLES20.glDisable(GLES20.GL_CULL_FACE);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, bullet.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			GLES20.glEnable(GLES20.GL_CULL_FACE);
		}

		CreateVbo(aim);
		mShader.Use();
		PassUniform(mShader, 2, 1);
		if(!mIsTouchOn&&fired!=1)
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, aim.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

		CreateVbo(zoom);
		zoomShader.Use();
		PassUniform(zoomShader, 4, 1);
		if(mIsTouchOn) {
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, zoom.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
			if(zoom_start==0) {
				mSound.sound_play(3);
				zoom_start = 1;
				mSound.sound_play(4);
				mSound.sound_stop(1);
			}
		}
		else if(ready==1&&mIsTouchOn==false)
		{
			mSound.sound_play(2);

			fired=1;
			slow_speed=0.08f;
			ready=0;

		}
		GLES20.glDisable(GLES20.GL_BLEND);


		Bufferclear();

		BasicUtils.CheckGLerror("glDrawElements");
	}

	void Bufferclear ()
	{
		GLES20.glDeleteBuffers(1, background.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, background.mVboIndices, 0);
		GLES20.glDeleteBuffers(1, target.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, target.mVboIndices,0);
		GLES20.glDeleteBuffers(1, terrian.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, terrian.mVboIndices,0);
		GLES20.glDeleteBuffers(1, bullet.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, bullet.mVboIndices,0);
		GLES20.glDeleteBuffers(1, aim.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, aim.mVboIndices,0);
		GLES20.glDeleteBuffers(1, zoom.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, zoom.mVboIndices,0);
		GLES20.glDeleteBuffers(1, floor.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, floor.mVboIndices,0);
		GLES20.glDeleteBuffers(1, trees.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, trees.mVboIndices,0);
		GLES20.glDeleteBuffers(1, horse.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, horse.mVboIndices,0);
		GLES20.glDeleteBuffers(1, house.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, house.mVboIndices,0);
		GLES20.glDeleteBuffers(1, gun.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, gun.mVboIndices,0);
		GLES20.glDeleteBuffers(1, car.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, car.mVboIndices,0);
		GLES20.glDeleteBuffers(1, burst.mVboVertices, 0);
		GLES20.glDeleteBuffers(1, burst.mVboIndices,0);
	}


	/*****************************
	 * **** Utility functions *****
	 *****************************/
	void LogInfo() {
		BasicUtils.PrintGLstring("Version", GLES20.GL_VERSION);
		BasicUtils.PrintGLstring("Vendor", GLES20.GL_VENDOR);
		BasicUtils.PrintGLstring("Renderer", GLES20.GL_RENDERER);
		BasicUtils.PrintGLstring("Extensions", GLES20.GL_EXTENSIONS);
		BasicUtils.PrintGLstring("GLSLversion", GLES20.GL_SHADING_LANGUAGE_VERSION);
	}

	void CountTickInit() {
//		mTimer.InitTimer();
	}

	void ComputeTick() {
//		static double lastTick = 0;
//		double currTick = mTimer.GetElapsedTime();
//		mDeltaTime = currTick - lastTick;
//		lastTick = currTick;
		//Log.i(TAG, "Tick: %f\n", mDeltaTime);
	}

	void ComputeTangent(OBJECT obj) {
		int stride = 3 + 3 + 2;

		short[] indices = new short[obj.mIndices.capacity()];
		obj.mIndices.position(0);
		for (int i = 0; i < indices.length; ++i) {
			indices[i] = obj.mIndices.get();
		}

		float[] vertices = new float[obj.mVertexData.capacity()];
		obj.mVertexData.position(0);
		for (int i = 0; i < vertices.length; ++i) {
			vertices[i] = obj.mVertexData.get();
		}
		Vec3[] tangents = new Vec3[indices.length];

		// Compute Tangent Basis
		for (int i = 0; i < obj.mIndices.capacity(); i += 3) {
			// Get triangle position
			Vec3 p0 = new Vec3(vertices[indices[i] * stride],
					vertices[indices[i] * stride + 1],
					vertices[indices[i] * stride + 2]);
			Vec3 p1 = new Vec3(vertices[indices[i + 1] * stride],
					vertices[indices[i + 1] * stride + 1],
					vertices[indices[i + 1] * stride + 2]);
			Vec3 p2 = new Vec3(vertices[indices[i + 2] * stride],
					vertices[indices[i + 2] * stride + 1],
					vertices[indices[i + 2] * stride + 2]);

			// Get triangle UV coordinate
			Vec3 uv0 = new Vec3(vertices[indices[i] * stride + 6],
					vertices[indices[i] * stride + 7], 0.0f);
			Vec3 uv1 = new Vec3(vertices[indices[i + 1] * stride + 6],
					vertices[indices[i + 1] * stride + 7], 0.0f);
			Vec3 uv2 = new Vec3(vertices[indices[i + 2] * stride + 6],
					vertices[indices[i + 2] * stride + 7], 0.0f);

			// Compute delta
			Vec3 deltaPos1 = Vec3.sub(p1, p0);
			Vec3 deltaPos2 = Vec3.sub(p2, p0);

			Vec3 deltaUV1 = Vec3.sub(uv1, uv0);
			Vec3 deltaUV2 = Vec3.sub(uv2, uv0);

			//Compute the tangent
			float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
			Vec3 tangent = Vec3.mul(deltaPos1, deltaUV2.y).sub(Vec3.mul(deltaPos2, deltaUV1.y)).mul(r);

			// Put in temp array
			tangents[i] = tangent;
			tangents[i + 1] = tangent;
			tangents[i + 2] = tangent;
		}

		// Initialize indTangents
		Vec3[] indTangents = new Vec3[vertices.length / stride];
		for (int i = 0; i < indTangents.length; ++i) {
			indTangents[i] = new Vec3();
		}

		// Accumulate tangents by indices
		for (int i = 0; i < tangents.length; i++) {
			indTangents[indices[i]].add(tangents[i]);
		}

		// Convert array to bytebuffer for glsl transaction
		obj.mTangentData = ByteBuffer.allocateDirect(indTangents.length * 3 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < indTangents.length; i++) {
			obj.mTangentData.put(indTangents[i].getArray());
		}
		obj.mTangentSize = indTangents.length * 3 * 4;

	}

	public void ImportModel(InputStream obj, OBJECT object) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(obj));

		String str;

		String name = "";
		List<Float> pos = new Vector<>();
		List<Float> normal = new Vector<>();
		List<Float> tex = new Vector<>();
		List<Float> vertex = new Vector<>();
		List<Short> indices = new Vector<>();
		HashMap<String, Short> elem = new HashMap<>();
		int posNumComponents = 0;
		int norNumComponents = 0;
		int texNumComponents = 0;

		try {
			while ((str = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str);
				if (st.hasMoreTokens()) {
					String curToken = st.nextToken();

					switch (curToken) {
						case "v":
							posNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								pos.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "vn":
							norNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								normal.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "vt":
							texNumComponents = st.countTokens();
							while (st.hasMoreTokens()) {
								tex.add(Float.parseFloat(st.nextToken()));
							}
							break;

						case "f":
							while (st.hasMoreTokens()) {
								String element = st.nextToken();
								if (elem.containsKey(element)) {
									short index = elem.get(element);
									indices.add(index);
								} else {
									indices.add(((short) elem.size()));
									elem.put(element, (short) elem.size());
									String listIndex[];
									listIndex = element.split("/");
									object.faceComponentNum=listIndex.length;
									for (int i = 0; i < posNumComponents; ++i) {
										int k = Integer.parseInt(listIndex[0]) - 1;
										vertex.add(pos.get(k * posNumComponents + i));
									}
									if(listIndex.length==3) {
										for (int i = 0; i < norNumComponents; ++i) {
											int k = Integer.parseInt(listIndex[2]) - 1;
											vertex.add(normal.get(k * norNumComponents + i));
										}
									}
									for (int i = 0; i < texNumComponents - 1; ++i) {
										int k = Integer.parseInt(listIndex[1]) - 1;
										vertex.add(tex.get(k * texNumComponents + i));
									}
								}
							}
							break;

						case "g":
							//name = st.nextToken();
							break;

						default:
							break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		object.mVertexData = ByteBuffer.allocateDirect(vertex.size() * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		object.mIndices = ByteBuffer.allocateDirect(indices.size() * 2)
				.order(ByteOrder.nativeOrder()).asShortBuffer();
		for (int i = 0; i < vertex.size(); i++) object.mVertexData.put(vertex.get(i));
		for (int i = 0; i < indices.size(); i++) object.mIndices.put(indices.get(i));

		object.mVertexData.position(0);
		object.mIndices.position(0);
		object.mVertexSize = vertex.size();
		object.mIndexSize = indices.size();

	}

	public void TouchOn() {
		mIsTouchOn = true;
	}


	public void TouchOff() {
		mIsTouchOn = false;
	}

	public void SetTouchPoint(float x, float y) {
		mTouchPoint.x = x;
		mTouchPoint.y = y;
	}

}
