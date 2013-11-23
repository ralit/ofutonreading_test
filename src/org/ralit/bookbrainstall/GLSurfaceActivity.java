package org.ralit.bookbrainstall;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;


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
		setEGLContextClientVersion(2);
		mRenderer = new GLSurfaceViewRenderer();
		setRenderer(mRenderer);
	}
	
}

class GLSurfaceViewRenderer implements GLSurfaceView.Renderer {
	private Context mContext;

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		
	}

}