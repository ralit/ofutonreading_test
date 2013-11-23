package org.ralit.bookbrainstall;

import android.app.Activity;
import android.os.Bundle;

public class SurfaceViewActivity extends Activity {

	public SurfaceViewActivity() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new SurfaceViewTest(this));
	}

}
