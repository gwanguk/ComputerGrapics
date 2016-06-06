package com.medialab.android_gles_sample.renderer;


import android.graphics.Shader;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;

import com.medialab.android_gles_sample.joml.AxisAngle4f;
import com.medialab.android_gles_sample.joml.Matrix3f;
import com.medialab.android_gles_sample.joml.Matrix4f;
import com.medialab.android_gles_sample.joml.Quaternionf;
import com.medialab.android_gles_sample.joml.Vector2f;
import com.medialab.android_gles_sample.joml.Vector3f;

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
	public OBJECT aim;
	public OBJECT background;
	public OBJECT zoom;
	public OBJECT bullet;

	public BasicShader mShader;
	public BasicShader targetShader;
	public BasicShader backgroundShader;

	public BasicCamera mCamera;

	boolean mIsAutoRotateEye;
	boolean mIsFill;

	static boolean mIsTouchOn;
	static Vector2f mTouchPoint;

	static Quaternionf startRotQuat;
	static Quaternionf lastRotQuat;
	static Vector2f ancPts;
	static boolean isUpdateAnc;

	float bullet_speed = 4;
	float target_speed = 1;
	float slow_speed = 1;

	int ready=0;
	int fired=0;

	int hit=0;

	// vertex buffer

	public BasicRenderer() {
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

		terrian = new OBJECT();
		target = new OBJECT();
		aim = new OBJECT();
		background = new OBJECT();
		zoom = new OBJECT();
		bullet = new OBJECT();

		accX=0;
		accY=0;
		accZ=0;
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


	/****************************
	 * *** Interface functions ***
	 ****************************/
	public void SetNewModel(InputStream objSource, OBJECT object) {
		ImportModel(objSource,object);
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
	Vector3f target_origin = new Vector3f(0.0f, 30.0f,-40.0f);
	float hit_effect =0;
	float moving_seed=0;
	float left=1;
	float[] GetWorldMatrix_TARTGET()
	{
		moving_seed+=0.01*target_speed*slow_speed;
		target_location.set(target_origin.x+(float) Math.sin((double) moving_seed + 180)*20,
				target_origin.y+(float) Math.cos((double) moving_seed)*20 ,
				target_origin.z);
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		rotationMat.rotate(90, 0.0f,1.0f,0.0f);

		if(hit==1&&hit_effect<20)
		{
			scaleMat.scale(hit_effect/10);
			hit_effect= hit_effect+1;
		}
		else if(hit==1&&hit_effect>=20)
		{
			hit=0;
			hit_effect=0;
		}


		Vector3f zero = new Vector3f(0,0,0);
		float[] viewMat_arr = GetCamera().GetViewMat(zero);
		viewMat.set(viewMat_arr);

		out = (transMat.translation(target_location.x, target_location.y, target_location.z).mul(rotationMat.mul(scaleMat.scale(0.01f))));
		out.get(farray);

		return farray;
	}


	float[] GetWorldMatrix_TERRIAN()
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		transMat.translate(0.0f, -15.0f, 0.0f);
		scaleMat.scale(1.0f * 0.000003f, 0.8f*0.000003f, 1.0f*0.000003f);

		out = (transMat.mul(rotationMat).mul(scaleMat));
		out.get(farray);

		return farray;
	}

	Vector3f ZOOM_location = new Vector3f();
	Vector3f Fake_EYE = new Vector3f();
	float scope_magnifiying_scale =3;
	float[] GetWorldMatrix_ZOOM()
	{
		float[] farray = new float[4*4];
		Matrix4f out = new Matrix4f();

		ZOOM_location.set(Fake_EYE.x+aim_unit.x*scope_magnifiying_scale,Fake_EYE.y+aim_unit.y*scope_magnifiying_scale,
				Fake_EYE.z+aim_unit.z*scope_magnifiying_scale);

		Vector3f va = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f vb = new Vector3f();
		vb.set(aim_unit);

		// Get the rotation axis and the angle between the vector
		float angle = (float)Math.acos(va.dot(vb));

		Vector3f axis = va.cross(vb).normalize();

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		Matrix4f rotationMat = new Matrix4f();
		Matrix4f viewMat= new Matrix4f();

		scaleMat.scale(10.0f);
		rotationMat.rotation(90, 1.0f, 0.0f, 0.0f);
		rotationMat.rotate(angle,axis);
		transMat.translation(ZOOM_location);

		Vector3f zero = new Vector3f(0,0,0);
		float[] viewMat_arr = GetCamera().GetViewMat(zero);
		viewMat.set(viewMat_arr);

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

		out = (transMat.translation(0.0f, 49.0f, 0.0f).mul(rotationMat.rotate(0, 0.0f, 0.0f, 0.0f).mul(scaleMat.scale(70.0f))));
		out.get(farray);

		return farray;
	}

	Vector3f bullet_pos = new Vector3f();
	Matrix4f bullet_lotation = new Matrix4f();
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
			bullet_pos.set(EYE_pos);
		}
		else if(fired==1)
		{
			bullet_offset.set(aim_unit.x * bullet_speed*slow_speed,aim_unit.y*bullet_speed*slow_speed, aim_unit.z*bullet_speed*slow_speed);
			bullet_pos.add(bullet_offset);
			if(bullet_pos.z<-50) {
				fired = 0;
				hit=0;
				slow_speed=1;
			}
			if(between_target_distance<5) {
				fired = 0;
				hit =1 ;
				slow_speed=1;
			}
		}

		Matrix4f scaleMat =new Matrix4f();
		Matrix4f transMat = new Matrix4f();
		scaleMat.scale(1.0f);
		transMat.translation(bullet_pos);
		bullet_lotation.rotate(1,aim_unit);
		Matrix4f viewMat= new Matrix4f();

		out = (transMat.mul(bullet_lotation.mul(scaleMat)));
		out.get(farray);

		return farray;
	}

	Vector3f proj_aim_location = new Vector3f(0.0f, 10.0f,-50.0f);
	Vector3f last_proj_aim_location =new Vector3f();
	Vector3f aim_location = new Vector3f(0.0f, 10.0f,-50.0f);
	Vector3f aim_unit = new Vector3f();
	int set = 1;
	float aim_lastY=0, aim_lastZ=0, aim_lastX=0;
	float aim_dy=0, aim_dz=0, aim_dx=0;
	float[] GetWorldMatrix_AIM()
	{
		float left, up;
		if(set==1)
		{
			aim_lastY = accY;
			aim_lastZ=accZ;
			aim_lastX = accX;
			set=0;
		}
		aim_dy = accY-aim_lastY;
		aim_dz =accZ-aim_lastZ;
		aim_dx = accX-aim_lastX;
		//aim_lastY =accY;
		//aim_lastZ=accZ;

		if(fired==0) {
			proj_aim_location.x += (aim_dy) / 5;
			proj_aim_location.y += aim_dz / 5;

			if (proj_aim_location.x < -50.0f)
				proj_aim_location.x = -50.0f;
			if (proj_aim_location.x > 50.0f)
				proj_aim_location.x = 50.0f;
			if (proj_aim_location.y < 0.0f)
				proj_aim_location.y = 0.0f;
			if (proj_aim_location.y > 100.0f)
				proj_aim_location.y = 100.0f;
			last_proj_aim_location.set(proj_aim_location);
		}




		Vector3f EYE_position = new Vector3f(GetCamera().GetEye().x,GetCamera().GetEye().y, GetCamera().GetEye().z);

		Vector3f va = new Vector3f(0.0f, 0.0f, -1.0f);
		Vector3f vb = aim_unit;

		// Get the rotation axis and the angle between the vector
		float angle = (float)Math.acos(Math.min(1.0f, va.dot(vb)));

		Vector3f axis = va.cross(vb).normalize();

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
		scaleMat.scale(0.3f);
		rotationMat.rotate(90, 1.0f, 0.0f, 0.0f);
		rotationMat.rotate(angle,axis);

		out = (transMat.mul(rotationMat.mul(scaleMat)));
		out.get(farray);

		return farray;
	}

	float magnifiying_scale =50;
	float[] GetViewMatrix()
	{
		float[] viewMat;
		if (mIsTouchOn) {
			GetCamera().setAT(proj_aim_location);
			Vector3f zoomed_vec = new Vector3f(aim_unit.x*magnifiying_scale,
					aim_unit.y*magnifiying_scale,
					aim_unit.z*magnifiying_scale);
			viewMat=GetCamera().GetViewMat(zoomed_vec);
			Fake_EYE.set(mCamera.mEye.x+zoomed_vec.x,mCamera.mEye.y+zoomed_vec.y,mCamera.mEye.z+zoomed_vec.z);
		}
		else  {
			GetCamera().setAT(origin_At);
			viewMat = GetCamera().GetViewMat(origin);
			Fake_EYE.set(mCamera.mEye.x, mCamera.mEye.y, mCamera.mEye.z);
		}

		if(fired==1)
		{
			GetCamera().setAT(last_proj_aim_location);
			Fake_EYE.set(bullet_pos.x-aim_unit.x,bullet_pos.y-aim_unit.y,bullet_pos.z-aim_unit.z);
			viewMat = GetCamera().GetViewMat(Fake_EYE);
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

	void PassUniform(BasicShader shader, int type) {
//		float[] worldMat = new float[16];
//		Matrix.setIdentityM(worldMat, 0);
		float[] worldMat;
		if(type==0)
			worldMat = GetWorldMatrix_TERRIAN();
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
		shader.SetUniform("lightPos", 50.0f, 50.0f, 50.0f);
		shader.SetUniform("materialDiff", 0.8f, 1.0f, 0.7f);
		shader.SetUniform("materialSpec", 0.8f, 1.0f, 0.7f);
		shader.SetUniform("materialAmbi", 0.0f, 0.0f, 0.0f);
		shader.SetUniform("materialEmit", 0.0f, 0.0f, 0.0f);
		shader.SetUniform("materialSh", 100.0f);
		shader.SetUniform("sourceDiff", 0.7f, 0.7f, 0.7f);
		shader.SetUniform("sourceSpec", 1.0f, 1.0f, 1.0f);
		shader.SetUniform("sourceAmbi", 0.0f, 0.0f, 0.0f);
	}


	void Draw() {
		CreateVbo(background);
		GLES20.glFrontFace(GLES20.GL_CW);
		mShader.Use();
		PassUniform(mShader, 3);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, background.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
		GLES20.glFrontFace(GLES20.GL_CCW);

		CreateVbo(target);
		mShader.Use();
		PassUniform(mShader, 1);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, target.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

		CreateVbo(terrian);
		mShader.Use();
		PassUniform(mShader, 0);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, terrian.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		targetShader.Use();
		PassUniform(targetShader, 5);
		if(fired==1) {
			CreateVbo(bullet);
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, bullet.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
		}

		CreateVbo(aim);
		targetShader.Use();
		PassUniform(targetShader, 2);
		if(!mIsTouchOn&&fired!=1)
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, aim.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);

		CreateVbo(zoom);
		targetShader.Use();
		PassUniform(targetShader, 4);
		if(mIsTouchOn) {
			ready=1;
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, zoom.mIndexSize, GLES20.GL_UNSIGNED_SHORT, 0);
		}
		else if(ready==1)
		{
			fired=1;
			slow_speed=0.1f;
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
							name = st.nextToken();
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
