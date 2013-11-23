package org.ralit.bookbrainstall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class SurfaceViewTest extends SurfaceView implements Callback, Runnable {

	private Bitmap mImage;
	private SurfaceHolder mHolder;
	private Thread mLooper;
	private String tag = "ralit";
	
	public SurfaceViewTest(Context context) {
		super(context);
		Log.i(tag, "SurfaceViewTest()");
		getHolder().addCallback(this);
		Options options = new BitmapFactory.Options();
//		options.outHeight = getHolder().getSurfaceFrame().height();
//		options.outHeight = 700;
//		options.outWidth = 7000;
		mImage = BitmapFactory.decodeResource(getResources(), com.artifex.mupdfdemo.R.drawable.one_line, options);
	}

//	public SurfaceViewTest(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		// TODO Auto-generated constructor stub
//	}
//
//	public SurfaceViewTest(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		// TODO Auto-generated constructor stub
//	}

	
	private int mWidth;
	private int mPositionTop;
	private int mPositionLeft;
	private long mTime;
	private long mLapTime;
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(tag, "SurfaceChanged()");
		if (mLooper != null) {
			mWidth = width;
			mTime = System.currentTimeMillis();
			mLooper.start();
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(tag, "SurfaceCreated()");
		mHolder = holder;
		mLooper = new Thread(this);
		Canvas canvas = holder.lockCanvas();
		canvas.scale(5, 5);
		Paint paint = new Paint();
		canvas.drawBitmap(mImage, 0, 0, paint);
		holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(tag, "SurfaceDestroyed()");
		mLooper = null;
	}

	@Override
	public void run() {
		Log.i(tag, "run()");
		
		while (mLooper != null) {
			doDraw();
			
			long delta = System.currentTimeMillis() - mTime;
			mTime = System.currentTimeMillis();
			int nextPosition = (int) ((delta/1000.0) * 200);
			
			if (mPositionLeft - nextPosition > -mWidth) {
				mPositionLeft -= nextPosition;
			} else {
				Log.i(tag, Long.toString(mTime - mLapTime));
				mLapTime = mTime;
				mPositionLeft = 0;
			}
		}
	}
	
	private void doDraw() {
//		Log.i(tag, "doDraw");
		Canvas canvas = mHolder.lockCanvas();
		canvas.scale(5, 5);
		Paint paint = new Paint();
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(mImage, mPositionLeft, mPositionTop, paint);
		mHolder.unlockCanvasAndPost(canvas);
	}

}
