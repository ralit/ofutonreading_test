package org.ralit.bookbrainstall;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;

public class GLSurfaceActivity extends Activity {

	private GLSurfaceViewTest view;
	
	public GLSurfaceActivity() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new GLSurfaceViewTest(this);
		setContentView(view);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		view.onResume();
	}

	@Override
	protected void onPause(){
		super.onPause();
		view.onPause();
	}

}


class GLSurfaceViewTest extends GLSurfaceView {

	private GLSurfaceViewRenderer mRenderer;
	
	public GLSurfaceViewTest(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGLSurfaceView(context);
	}
	
	public GLSurfaceViewTest(Context context) {
		super(context);
		initGLSurfaceView(context);
	}
	
	public void initGLSurfaceView(Context context) {
//		setEGLContextClientVersion(2);
		mRenderer = new GLSurfaceViewRenderer(context);
		setRenderer(mRenderer);
	}
	
}

class GLSurfaceViewRenderer implements GLSurfaceView.Renderer {
	
	private Context mContext;
	Img img;
	private int i = 0;
	
	public GLSurfaceViewRenderer(Context context) {
		mContext = context;
		img = new Img(context);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		img.draw(gl, i);
		i = i + 2;
		if(i > 1024) { i = 0; }
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		GLU.gluOrtho2D(gl, 0f, width, 0f, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//背景色をクリア
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		//ディザを無効化
		gl.glDisable(GL10.GL_DITHER);
		//深度テストを有効化
		gl.glEnable(GL10.GL_DEPTH_TEST);
		//テクスチャ機能ON
		gl.glEnable(GL10.GL_TEXTURE_2D);
		//透明可能に
		gl.glEnable(GL10.GL_ALPHA_TEST);
		//ブレンド可能に
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		img.setTexture(gl, mContext.getResources(), com.artifex.mupdfdemo.R.drawable.sq2048);
	}

}

class Img {
	
	private String tag = "ralit";
	private Context mContext;
	//テクスチャNo
	public int textureNo;
	//表示位置
	float  pos_x;
	float  pos_y;
	float  pos_z;
	//テクスチャ（画像）の位置とサイズ
	int    texX;
	int    texY;
	int    texWidth;
	int    texHeight;
	//配置する時の幅と高さ
	float  width;
	float  height;

	public Img(Context context) {
		mContext = context;
	}
	
	public void setTexture(GL10 gl, Resources res, int id) {
		
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), com.artifex.mupdfdemo.R.drawable.one_line);
		bitmap.getHeight();
		bitmap.getWidth();
		int width_2 = 2;
		while(bitmap.getWidth() <= width_2) { width_2 *= 2; }
		int height_2 = 2;
		while(bitmap.getHeight() <= height_2) { height_2 *= 2; }
		Bitmap bitmap2 = Bitmap.createBitmap(width_2, height_2, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		canvas.drawBitmap(bitmap2, 0, 0, paint);
		
//		Bitmap bitmap = BitmapFactory.decodeResource(res, id);
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		//テクスチャIDを割り当てる
		int[] textureID = new int[1];
		gl.glGenTextures(1, textureID, 0);
		textureNo = textureID[0];
		//テクスチャIDのバインド
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNo);
		//OpenGL ES用のメモリ領域に画像データを渡す。上でバインドされたテクスチャIDと結び付けられる。
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//テクスチャ座標が1.0fを超えたときの、テクスチャを繰り返す設定
		gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT );
		gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT );
		//テクスチャを元のサイズから拡大、縮小して使用したときの色の使い方を設定
		gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
		gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );
		texX      = 0;
		texY      = bitmap.getHeight();
		texWidth  = bitmap.getWidth();
		texHeight = -bitmap.getHeight();
		pos_x     = 0;
		pos_y     = 0;
		pos_z     = 0;
		width     = bitmap.getWidth();
		Log.i(tag, "bitmap.getWidth()" + bitmap.getWidth());
		height    = bitmap.getHeight();
		Log.i(tag, "bitmap.getHeight()" + bitmap.getHeight());
	}

	public void draw(GL10 gl, int i) {
		gl.glDisable(GL10.GL_DEPTH_TEST);
		//背景色を白色で塗りつぶし
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		//テクスチャ0番をアクティブにする
		gl.glActiveTexture(GL10.GL_TEXTURE0);
		//テクスチャIDに対応するテクスチャをバインドする
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureNo);
		//テクスチャの座標と幅と高さを指定
//		int rect[] = { texX,  texY,  texWidth, texHeight};
		int rect[] = { texX,  texY,  1200, -1600};
		//テクスチャ画像のどの部分を使うかを指定
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,GL11Ext.GL_TEXTURE_CROP_RECT_OES, rect, 0);
		//描画
		((GL11Ext) gl).glDrawTexfOES( pos_x - i, pos_y, pos_z, 600, 800);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}
	
}