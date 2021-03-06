package org.ralit.bookbrainstall;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.recognize.HttpSceneryLineLayoutAnalysisRequest;
import jp.recognize.SceneryLineLayoutAnalyzer;
import jp.recognize.client.HttpSceneryLineLayoutAnalyzer;
import jp.recognize.common.ImageContentType;
import jp.recognize.common.RecognitionResult.LineLayout;
import jp.recognize.common.Shape.Rectangle;
import jp.recognize.common.client.HttpSceneryRecognitionRequest.InputStreamImageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.artifex.mupdfdemo.MuPDFCore;

public class MainActivity extends Activity implements AnimatorListener, FileOpenDialogListener{
	
	private LinearLayout linearlayout;
	private FrameLayout framelayout2;
	private FrameLayout framelayout;
	private FrameLayout rootframe;
	private FrameLayout markerframe;
	private ImageView markerview;
	private ImageView image;
	private ImageView image2;
	private ImageView select;
	private ImageView overview;
	private ImageView nowloadingview;
	private BitmapFactory.Options options;
	private float dH;
	private float dW;
	private float textZoom;
	private boolean focusChanged = false; 
	private boolean first = true;
	private boolean back = false;
	private boolean ispos = false;
	private float cH;
	private float cW;
	private Bitmap bmp;
	private Bitmap mutableBitmap;
	private Bitmap page;
	private Bitmap markedPage;
	private Bitmap markerBitmap;
	ArrayList<ArrayList<Integer>> pos = new ArrayList<ArrayList<Integer>>();
	private Paint frame;
	private Paint number;
	private Paint marker;
	private Canvas canvas;
	private Canvas markerCanvas;
	ObjectAnimator fadein;
	ObjectAnimator fadeout;
	ObjectAnimator move;
	private String tag = "ralit";
	private GestureDetector gesture;
	AnimatorSet set;
	private MuPDFCore pdf;
	private float marginRatio = 0.2f;
	private float margin;
	private String filepath;
	private String filename;
	private String filepath_for_docomo;
	
	private String RECOGNITION_URL = "https://recognize.jp/v1/scenery/api/line-region";
	private String ANALYSIS = "standard";
	private int index = 0;
	private byte[] jpegData;
	private LineLayout[] job;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(tag, "onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initRootView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(tag, "onCreateOptionsMenu()");
		menu.add("蛍光ペン");
		menu.add("ファイル選択");
		menu.add("SurfaceView");
		menu.add("GLSurfaceView");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(tag, "onOptionsItemSelected()");
		Log.i(tag, Integer.toString(item.getItemId()));
		Log.i(tag, item.toString());
		if (item.toString() == "蛍光ペン") {
			Intent intent = new Intent(getApplicationContext(), MarkedList.class);
			startActivityForResult(intent, 1);
			overridePendingTransition(com.artifex.mupdfdemo.R.animator.in_lower_right, com.artifex.mupdfdemo.R.animator.out_upper_left);
		} else if (item.toString() == "ファイル選択") {
			FileOpenDialog fod = new FileOpenDialog(this, this);
			fod.openDirectory(Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagemove");
		} else if (item.toString() == "SurfaceView") {
			Intent intent = new Intent(getApplicationContext(), SurfaceViewActivity.class);
			startActivityForResult(intent, 2);
			overridePendingTransition(com.artifex.mupdfdemo.R.animator.in_lower_right, com.artifex.mupdfdemo.R.animator.out_upper_left);
		} else if (item.toString() == "GLSurfaceView") {
			Intent intent = new Intent(getApplicationContext(), GLSurfaceActivity.class);
			startActivityForResult(intent, 2);
			overridePendingTransition(com.artifex.mupdfdemo.R.animator.in_lower_right, com.artifex.mupdfdemo.R.animator.out_upper_left);
		}
		return false;
	}
	
	public void onFileSelected(File file) {
		Log.i(tag, "onFileSelected()");
		filepath = file.getAbsolutePath();
		filename = file.getName();
		fadeinNowloading();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.i(tag, "onWindowFocusChanged()");
		super.onWindowFocusChanged(hasFocus);
		if (focusChanged) { return; }
		focusChanged = true;
		initChildrenView();
//		fadeinNowloading();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == resultCode) {
			if (requestCode == RESULT_OK) {
				
			}
		}
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		Log.i(tag, "onAnimationEnd()");
		if (first) {
			first = false;
			recognize();
			if(!ispos) {
				setPosition();
			}
				Collections.sort(pos, new PositionComparator());
				deleteLongcat();
				deleteDuplicate();
				expand();
			paintPosition();
			savePaintedImage();
			setimage();
			setimage2();
			fadeoutNowloading();
			gesture = new GestureDetector(this, gestureListener);
			animation();
		} else {
			if (!back) { ++index; }
			back = false;
			Log.i(tag, "index: " + index);
			if (index < pos.size()) {
				setimage();
				animation2();
				animation();	
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return gesture.onTouchEvent(ev);
	}
	
	private final SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onFling(MotionEvent ev1, MotionEvent ev2, float vx, float vy) {
				if (ev1.getY() > dH && ev2.getY() > dH) {
					// 蛍光ペン
					mark(ev1, ev2);
				} else if (Math.abs(ev1.getY() - ev2.getY()) > 250) { 
					// 無視
					return false; 
				} else if (ev2.getX() - ev1.getX() > 120 && Math.abs(vx) > 200) {
					// 1行戻る
					back = true;
					if (set.getChildAnimations().get(0).isRunning()) { 
						if (index > 0) { --index; }
					}
					set.cancel();
				} else if (ev1.getX() - ev2.getX() > 120 && Math.abs(vx) > 200) {
					// 1行進む
					set.cancel();
				}
			return false;
		}
	};
	
//	@Override
//	public void onResume() {
//		super.onResume();
//		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
//	}
	
//	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//		@Override
//		public void onManagerConnected(int status) {
//			switch (status) {
//				case LoaderCallbackInterface.SUCCESS: {
//					Log.i(tag, "OpenCV loaded successfully");
//					break;
//				}
//				default: {
//					super.onManagerConnected(status);
//					break;
//				}
//			}
//		}
//	};

	private void mark(MotionEvent ev1, MotionEvent ev2) {
		float w = (float) mutableBitmap.getWidth();
		float h = (float) mutableBitmap.getHeight();					
		// 実際に指を滑らせた位置にマーカーを引く
		Point screen = new Point();
		getWindowManager().getDefaultDisplay().getSize(screen);
		float linemid = (pos.get(index).get(3) + pos.get(index).get(1)) / 2;
		float realx1 = ev1.getX() * (w / dW);
		float realy1 = linemid + (w / dW) * (ev1.getY() - (1f/2f) * dH  - (screen.y - dH));
		float realx2 = ev2.getX() * (w / dW);
		float realy2 = linemid + (w / dW) * (ev2.getY() - (1f/2f) * dH  - (screen.y - dH));
		marker.setColor(Color.RED);
		marker.setAlpha(64);
		int i = index;
		int add = 1;
		Log.i(tag, "i: " + i);
		while ( 0 <= i && i <= pos.size() ) {
			Log.i(tag, "i = " + i + ": " + pos.get(i).get(0) + " < " + realx1 + " < " + pos.get(i).get(2) + ", " + pos.get(i).get(1) + " < " + realy1 + " < " + pos.get(i).get(3));
			if (pos.get(i).get(0) < realx1 && realx1 < pos.get(i).get(2) && pos.get(i).get(1) < realy1 && realy1 < pos.get(i).get(3)) {
				break;
			}
			i += add;
			add = add > 0 ? -1 * (add+1) : -1 * (add-1);
		}
		if ( 0 <= i && i <= pos.size() ) {
			Rect rect = new Rect((int)realx1, pos.get(i).get(1), (int)realx2, pos.get(i).get(3));
			markerCanvas.drawRect(rect, marker);
			markedPage = Bitmap.createScaledBitmap(markerBitmap, (int)dW, (int)(dW * (h/w)), false);
			markerview.setImageBitmap(markedPage);
			saveMarkedImage(Bitmap.createBitmap(bmp, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top), "test", rect);
		}
	}

	private void expand() {
		Log.i(tag, "expand()");
		TimingLogger timing = new TimingLogger(tag, "timinglogger");
		
		long start = System.currentTimeMillis();
		int ww = bmp.getWidth();
		int hh = bmp.getHeight(); 
		int pixels[] = new int[ww * hh];
		bmp.getPixels(pixels, 0, ww, 0, 0, ww, hh);
		long end = System.currentTimeMillis();
		Log.i(tag, "getPixels(): " + (end - start));
		
//		timing.addSplit("getpixels");
		start = System.currentTimeMillis();
		for (int i = 0; i < pos.size(); ++i) {
			ArrayList<Integer> array = pos.get(i);
			int y = (array.get(3) + array.get(1)) / 2;
			int newL = -1;
			int newR = -1;
			for (int x = 0; x < array.get(0); ++x) {
				if(pixels[x + y * ww] == -16777216) { newL = x; break; }
			}
			for (int x = ww-1; array.get(2) < x; --x) {
				if(pixels[x + y * ww] == -16777216) { newR = x; break; }
			}
			if (newL != -1) { array.set(0, newL - margin > 0 ? (int)(newL - margin) : 0); }
			if (newR != -1) { array.set(2, newR + margin < ww ? (int)(newR + margin) : ww-1); }
		}
		end = System.currentTimeMillis();
		Log.i(tag, "pixel走査: " + (end - start));
//		timing.addSplit("pixel走査");
//		timing.dumpToLog();
		Log.i(tag, timing.toString());
	}
	
	private void deleteLongcat() {
		Log.i(tag, "deleteLongcat()");
		ArrayList<Integer> fatCat = new ArrayList<Integer>();
		ArrayList<Integer> longCat = new ArrayList<Integer>();
		for (int i = 0; i < pos.size(); ++i) {
			if (pos.get(i).get(2) - pos.get(i).get(0) > pos.get(i).get(3) - pos.get(i).get(1)) {
				fatCat.add(i);
			} else {
				longCat.add(i);
			}
		}
		if (fatCat.size() >= longCat.size()) {
			for (int i = 0; i < longCat.size(); ++i ) {
				Log.i(tag, "longCat: " + longCat);
				pos.remove((int)longCat.get(i));
			}
		} else {
			for (int i = 0; i < fatCat.size(); ++i ) {
				Log.i(tag, "fatCat: " + fatCat);
				pos.remove((int)fatCat.get(i));
			}
		}
	}
	
	private void deleteDuplicate() {
		Log.i(tag, "deleteDuplicate()");
		for (int i = 1; i < pos.size();) {
			ArrayList<Integer> i0 = pos.get(i);
			ArrayList<Integer> i1 = pos.get(i - 1);
			if (i0.get(1) < i1.get(3)) {  
				i1.set(0, i0.get(0) < i1.get(0) ? i0.get(0) : i1.get(0));
				i1.set(2, i0.get(2) > i1.get(2) ? i0.get(2) : i1.get(2));
				pos.remove(i);
			} else {
				++i;
			}
		}
	}
	
	public void initRootView() {
		Log.i(tag, "initRootView()");
		rootframe = new FrameLayout(this);
		nowloadingview = new ImageView(this);
		nowloadingview.setAlpha(0f);
		rootframe.addView(nowloadingview);
		setContentView(rootframe);
	}	
	

	private void initChildrenView() {
		Log.i(tag, "initChildrenView()");
		Log.i(tag, "rootframe" + rootframe.getHeight() + rootframe.getWidth());
		linearlayout = new LinearLayout(this);
		linearlayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		linearlayout.setOrientation(1); // vertical
		rootframe.addView(linearlayout);
		
		framelayout = new FrameLayout(this);
		framelayout.setLayoutParams(new LayoutParams(rootframe.getWidth(), rootframe.getHeight() / 2));
		framelayout2 = new FrameLayout(this);
		framelayout2.setLayoutParams(new LayoutParams(rootframe.getWidth(), rootframe.getHeight() / 2));
		linearlayout.addView(framelayout);
		linearlayout.addView(framelayout2);
		
		image = new ImageView(this);
		image2 = new ImageView(this);
		image2.setAlpha(0f);
		overview = new ImageView(this);
		framelayout.addView(image);
		framelayout.addView(image2);
//		framelayout2.addView(overview);
		markerview = new ImageView(this);
		markerframe = new FrameLayout(this);
		framelayout2.addView(markerframe);
		markerframe.addView(overview);
		markerframe.addView(markerview);
//		overview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//		markerview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	public void fadeinNowloading() {
		Log.i(tag, "fadeinNowloading()");
		nowloadingview.setImageResource(com.artifex.mupdfdemo.R.drawable.recognizing);
		ObjectAnimator anim = ObjectAnimator.ofFloat(nowloadingview, "alpha", 1f);
		anim.setDuration(1000);
		anim.addListener(this);
		anim.start();
	}
	
	public void fadeoutNowloading() {
		Log.i(tag, "fadeoutNowloading()");
		ObjectAnimator anim = ObjectAnimator.ofFloat(nowloadingview, "alpha", 0f);
		anim.setDuration(1000);
		anim.start();
	}
	
	public void animation() {
		Log.i(tag, "animation()");
		set = new AnimatorSet();
		if(index % 2 == 0) {
			fadein = ObjectAnimator.ofFloat(image, "alpha", 0f, 1f);
			fadeout = ObjectAnimator.ofFloat(image2, "alpha", 1f, 0f);
		} else if(index % 2 == 1) {
			fadein = ObjectAnimator.ofFloat(image2, "alpha", 0f, 1f);
			fadeout = ObjectAnimator.ofFloat(image, "alpha", 1f, 0f);
		}
		fadein.setDuration(1000);
		fadeout.setDuration(1000);
		move = ObjectAnimator.ofFloat(select, "x", dW * textZoom / (float)2, -dW * textZoom / (float)2);
		move.setDuration(15000);
		move.setInterpolator(new LinearInterpolator());
		set.play(fadein).with(fadeout);
		set.play(fadein).before(move);
		set.addListener(this);
		set.start();
	}
	
	private void animation2() {
		float h = mutableBitmap.getHeight();
		float w = mutableBitmap.getWidth();
		float linemid = (pos.get(index).get(3) + pos.get(index).get(1)) / 2;
		float distance = h / 2 - linemid;
//		float i = distance * (overview.getWidth() / w);
//		ObjectAnimator anim = ObjectAnimator.ofFloat(overview, "y", i);
		float i = distance * (markerframe.getWidth() / w);
		ObjectAnimator anim = ObjectAnimator.ofFloat(markerframe, "y", i);
		anim.setDuration(1000);
		anim.start();
	}
	
	private void prepare_image() {
		Log.i(tag, "prepare_image()");
		dH = (float) framelayout.getHeight();
		dW = (float) framelayout.getWidth();
		cW = (pos.get(index).get(2) - pos.get(index).get(0));
		cH = (pos.get(index).get(3) - pos.get(index).get(1));
		textZoom = dH / (cH * (dW/cW));
//		Log.i("dH", Float.toString(dH));
//		Log.i("dW", Float.toString(dW));
//		Log.i("cW", Float.toString(cW));
//		Log.i("cH", Float.toString(cH));
//		Log.i("textZoom", Float.toString(textZoom));
	}
	
	public void setimage() {
		Log.i(tag, "setimage()");
		if (index % 2 == 0) { select = image; } else { select = image2; }
		int w = pos.get(index).get(2) - pos.get(index).get(0);
		int h = pos.get(index).get(3) - pos.get(index).get(1);
//		select.setImageBitmap(Bitmap.createScaledBitmap(Bitmap.createBitmap(bmp, pos.get(index).get(0), pos.get(index).get(1), w, h), 2048, (int)(2048 * ((float)h/(float)w)), false));
		select.setImageBitmap(Bitmap.createBitmap(bmp, pos.get(index).get(0), pos.get(index).get(1), w, h));
		prepare_image();
		select.setScaleX(textZoom);
		select.setScaleY(textZoom);
		select.setX(dW * textZoom / (float)2);
		select.setY(0);
		Log.i(tag, "setimage()" + pos.get(index));
		Log.i(tag, "setimage()" + textZoom);
	}
	
	private void setimage2() {
		Log.i(tag, "setimage2()");
		float w = (float) mutableBitmap.getWidth();
		float h = (float) mutableBitmap.getHeight();
		float ratio = dH / h;
		float small_w = w * ratio;
		float scale_ratio = dW / small_w;
		page = Bitmap.createScaledBitmap(mutableBitmap, (int)dW, (int)(dW * (h/w)), false);
		markedPage = Bitmap.createScaledBitmap(markerBitmap, (int)dW, (int)(dW * (h/w)), false);
		overview.setImageBitmap(page);
		markerview.setImageBitmap(markedPage);

//		overview.setScaleX(scale_ratio);
//		overview.setScaleY(scale_ratio);
		markerframe.setScaleX(scale_ratio);
		markerframe.setScaleY(scale_ratio);
		float linemid = (pos.get(index).get(3) + pos.get(index).get(1)) / 2;
		float distance = h / 2 - linemid;
//		float i = distance * (overview.getWidth() / w);
//		overview.setY(i);
		float i = distance * (markerframe.getWidth() / w);
		markerframe.setY(i);
		Log.i(tag, "i: " + i);
	}
	
	private void recognize() {
		Log.i(tag, "recognize()");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				docomo();
			}
		});
		try {
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	private void openPDF() {
		Log.i(tag, "openPDF()");
//		File file = new File(Environment.getExternalStorageDirectory().getPath() + "/imagemove/");
//		String attachName = file.getAbsolutePath() + "/" + "file.pdf";
		try {
			pdf = new MuPDFCore(this, filepath);
			int page_max = pdf.countPages();
			PointF size = new PointF();
			size = pdf.getPageSize(0);
			bmp = Bitmap.createBitmap((int)size.x, (int)size.y, android.graphics.Bitmap.Config.ARGB_8888);
			pdf.drawPage(bmp, 0, (int)size.x, (int)size.y, 0, 0, (int)size.x, (int)size.y);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, 90, bos);
			jpegData = bos.toByteArray();
			save_image_for_docomo(bmp, 80);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private void openResource() {
		Log.i(tag, "openResource()");
		options = new BitmapFactory.Options();
		options.inScaled = false;
		bmp = BitmapFactory.decodeResource(getResources(), com.artifex.mupdfdemo.R.drawable.organic, options);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 90, bos);
		jpegData = bos.toByteArray();
		save_image_for_docomo(bmp, 80);
	}

	private void openImage() {
		Log.i(tag, "openImage()");
		options = new BitmapFactory.Options();
		options.inScaled = false;
		File file = new File(filepath);
		try {
			FileInputStream fis = new FileInputStream(file);
			bmp = BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 90, bos);
		jpegData = bos.toByteArray();
		save_image_for_docomo(bmp, 80);
	}
	
	public void save_pos(){
		Log.i(tag, "save_pos()");
        JSONArray jsArray = new JSONArray(pos);
        HashMap<String, JSONArray> map = new HashMap<String, JSONArray>();
        map.put("pos", jsArray);
        JSONObject json = new JSONObject(map);
        SharedPreferences sharedprefarence = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedprefarence.edit();
//        editor.putString("pos_list", json.toString()).commit();
        editor.putString(filename, json.toString()).commit();
    }
	
    private void load_pos(){
    	Log.i(tag, "load_pos()");
        SharedPreferences sharedprefarence = PreferenceManager.getDefaultSharedPreferences(this);
//        String rawJson = sharedprefarence.getString("pos_list", "no_data");
        String rawJson = sharedprefarence.getString(filename, "no_data");
        if (rawJson == "no_data") { ispos = false; return; }
        try {
        	JSONObject json = new JSONObject(rawJson);
            JSONArray jsonArray = json.getJSONArray("pos");
            pos.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
            	String rawJson2 = jsonArray.getString(i);
            	JSONArray internal = new JSONArray(rawJson2);
            	ArrayList<Integer> tmp = new ArrayList<Integer>();
            	for (int j = 0; j < internal.length(); j++) {
            		tmp.add(internal.getInt(j));
            	}
            	pos.add(tmp);
            }
            Log.i(tag, pos.toString());
        }
        catch (JSONException e) {
        	e.printStackTrace();
        }
        ispos = true;
}

    private void openZip() {
    	Log.i(tag, "openZip()");
    	try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(filepath));
			ZipEntry ze;
			BufferedOutputStream bos;
			int len;
			
			while ((ze = zis.getNextEntry()) != null) {
				File file = new File(ze.getName());
				File tmpdir = new File(Environment.getExternalStorageDirectory().getPath() + "/imagemove/tmp_zip");
				try {
					if (!tmpdir.exists()) { tmpdir.mkdir(); }
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				bos = new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagemove/tmp_zip/" + file.getName()));
				byte[] buffer = new byte[1024];
				while ((len = zis.read(buffer)) != -1) { bos.write(buffer, 0, len); }
				zis.closeEntry();
				bos.close();
				bos = null;
				
				File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagemove/tmp_zip");
				File[] fileList = dir.listFiles();
				filepath = fileList[0].getAbsolutePath();
				openImage();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private void docomo () {
		Log.i(tag, "docomo()");
		Api api = new Api();

		Log.i(tag, filepath);
		Log.i(tag, filename);
		
		if (filepath.endsWith(".pdf")) {
			openPDF();
		} else if (filepath.endsWith(".jpg")) {
			openImage();
		} else if (filepath.endsWith(".zip")) {
			openZip();
		}
//		openResource();
		
//		load_pos();
		if (ispos) { return; }
		
		try {
			SceneryLineLayoutAnalyzer analyzer;
			analyzer = new HttpSceneryLineLayoutAnalyzer(new URL(RECOGNITION_URL));
			job = analyzer.analyze(new HttpSceneryLineLayoutAnalysisRequest(
					api.API_KEY, 
					ANALYSIS, 
					new InputStreamImageContent(ImageContentType.IMAGE_JPEG, new ByteArrayInputStream(jpegData)),
					null /* new HttpSceneryLineLayoutAnalysisHint(aImageTrimRectangle, aImageRotationDegree, aLetterColor) */
					));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private void setPosition() {
		Log.i(tag, "setPosition()");
		for (LineLayout line : job) {
			Rectangle bounds = line.getShape().getBounds();
			ArrayList<Integer> internal = new ArrayList<Integer>();
			margin = (bounds.getBottom() - bounds.getTop()) * marginRatio;
			internal.add(bounds.getLeft() - (int)margin);
			internal.add(bounds.getTop() - (int)margin);
			internal.add(bounds.getRight() + (int)margin);
			internal.add(bounds.getBottom() + (int)margin);
			pos.add(internal);	
		}
		save_pos();
	}
	
	private void paintPosition() {
		Log.i(tag, "paintPosition()");
		frame = new Paint();
		frame.setStyle(Style.STROKE);
		frame.setColor(Color.RED);
		frame.setStrokeWidth(4);
		number = new Paint();
		number.setStyle(Style.FILL_AND_STROKE);
		number.setColor(Color.RED);
		number.setStrokeWidth(1);
		number.setTextSize(20);
		marker = new Paint();
		marker.setStyle(Style.FILL_AND_STROKE);
		marker.setColor(Color.YELLOW);
		marker.setStrokeWidth(1);
		marker.setAlpha(64);
		mutableBitmap = bmp.copy(bmp.getConfig(), true);
		
		prepare_image();
//		float w = (float) mutableBitmap.getWidth();
//		float h = (float) mutableBitmap.getHeight();
//		float viewh = dW * (h/w);
//		Log.i(tag, "w: " + w + ", h: " + h + ", viewh: " + viewh);
		markerBitmap = Bitmap.createBitmap(mutableBitmap.getWidth(), mutableBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		
		canvas = new Canvas(mutableBitmap);
		markerCanvas = new Canvas(markerBitmap);
		for (int i = 0; i < pos.size(); ++i) {
			Rect rect = new Rect(pos.get(i).get(0), pos.get(i).get(1), pos.get(i).get(2), pos.get(i).get(3));
			canvas.drawRect(rect, frame);
			canvas.drawText(Integer.toString(i), pos.get(i).get(0), pos.get(i).get(1), number);
			markerCanvas.drawRect(rect, marker);
		}
		
	}
	
	private void printPosition() {
		Log.i(tag, "printPosition()");
		for (int i = 0; i < pos.size(); ++i) {
			Log.i("pos", i + ": " + pos.get(i));
		}
	}
	
	private void savePaintedImage() {
		Log.i(tag, "savePaintedImage()");
		// ファイルの保存
		Log.i(tag, Environment.getExternalStorageDirectory().getPath());
		File file = new File(Environment.getExternalStorageDirectory().getPath() + "/imagemove/");
		try {
			if (!file.exists()) { file.mkdir(); }
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		String attachName = file.getAbsolutePath() + "/" + "imagemove.jpg";
		try {
			FileOutputStream out = new FileOutputStream(attachName);
			mutableBitmap.compress(CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
//			mutableBitmap.recycle();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void save_image_for_docomo(Bitmap bmp, int compress) {
		Log.i(tag, "save_image_for_docomo()");
		File file = new File(Environment.getExternalStorageDirectory().getPath() + "/imagemove/send_to_docomo/");
		try {
			if (!file.exists()) { file.mkdir(); }
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		String attachName = file.getAbsolutePath() + "/" + "tmp.jpg";
		filepath_for_docomo = attachName;
		try {
			FileOutputStream out = new FileOutputStream(attachName);
			bmp.compress(CompressFormat.JPEG, compress, out);
			out.flush();
			out.close();
//			mutableBitmap.recycle();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveMarkedImage(Bitmap bitmap, String page, Rect rect) {
		Log.i(tag, "saveMarkedImage()");
		String dir = Environment.getExternalStorageDirectory().getPath() + "/imagemove/";
		File file = new File(dir + page + "/");
		try {
			if (!file.exists()) { file.mkdir(); }
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		String attachName = file.getAbsolutePath() + "/mark_" + rect.left + "_" + rect.top + "_" + rect.right + "_" + rect.bottom + ".jpg";
		try {
			FileOutputStream out = new FileOutputStream(attachName);
			bitmap.compress(CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onAnimationCancel(Animator animation) {
		Log.i(tag, "onAnimationCancel()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		Log.i(tag, "onAnimationRepeat()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animator animation) {
		Log.i(tag, "onAnimationStart()");
		// TODO Auto-generated method stub
		
	}
	
	private void log(String message) {
		Log.i(tag, message);
	}
}

class PositionComparator implements java.util.Comparator<ArrayList<Integer>> {
	@Override
	public int compare(ArrayList<Integer> lhs, ArrayList<Integer> rhs) {
		return lhs.get(1) - rhs.get(1);
	}
}


interface FileOpenDialogListener {
	void onFileSelected(final File file);
}


class FileOpenDialog implements DialogInterface.OnClickListener {

	private Context parent = null;
	private int selectedItemIndex = -1;
	private File[] fileList;
	private String cd = null;
	private Stack<String> directories = new Stack<String>();
	private FileOpenDialogListener listener;
	private File lastSelectedItem;
	
	public FileOpenDialog(final Context parent, final FileOpenDialogListener listener) {
		super();
		this.parent = parent;
		this.listener = listener;
	}
	
	public void openDirectory(String dir) {
		this.fileList = new File(dir).listFiles();
		this.cd = dir;
		
		String[] fileNameList = null;
		int itemCount = 0;
		
		// ルートディレクトリ以外では上の階層に移動できるようにする
		if (0 < this.directories.size()) {
			fileNameList = new String[this.fileList.length + 1];
			fileNameList[itemCount] = "↑";
			itemCount++;
		} else {
			fileNameList = new String[this.fileList.length];
		}
		
		// ファイル名を表示
		for (File file : this.fileList) {
			if (file.isDirectory()) { fileNameList[itemCount] = file.getName() + "/"; }
			else { fileNameList[itemCount] = file.getName(); }
			itemCount++;
		}
		
		// ダイアログ表示
		new AlertDialog.Builder(this.parent).setTitle(dir).setItems(fileNameList, this).show();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		this.selectedItemIndex = which;
		if (this.fileList == null) { return; }
		
		int selectedItemIndex = this.selectedItemIndex;
		if (0 < this.directories.size()) { selectedItemIndex--; } // "↑"項目ぶんずれる。
		
		// ファイルをタップ
		if (selectedItemIndex < 0) { // "↑"がタップされた
			this.openDirectory(this.directories.pop());
		} else {
			this.lastSelectedItem = fileList[selectedItemIndex];
			if (this.lastSelectedItem.isDirectory()) {
				this.directories.push(cd);
				this.openDirectory(this.lastSelectedItem.getAbsolutePath());
			} else {
				this.listener.onFileSelected(this.lastSelectedItem);
			}
		}
	}
	
}