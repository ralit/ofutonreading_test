package org.ralit.bookbrainstall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MarkedList extends Activity {
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout root = new LinearLayout(this);
		setContentView(root);
		listview = new ListView(this);
		root.addView(listview);
		createImageList();
	}
	
	void createImageList() {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagemove/" + "test");
		File[] filelist = dir.listFiles();
		try {
//			ArrayList<Bitmap> array = new ArrayList<Bitmap>();
			ArrayList<ImageItem> array = new ArrayList<ImageItem>();
			for (int i = 0; i < filelist.length; i++) {
				File file = new File(dir.getAbsolutePath() + "/" + filelist[i].getName());
				FileInputStream fis = new FileInputStream(file);
				Bitmap bmp = BitmapFactory.decodeStream(fis);
				ImageItem item = new ImageItem();
				item.setBitmap(bmp);
				array.add(item);
			}
//			ArrayAdapter<Bitmap> adapter = new ArrayAdapter<Bitmap>(this, com.artifex.mupdfdemo.R.layout.marked_list, bitmapArray);
//			listview.setAdapter(adapter);
			ImageAdapter adapter = new ImageAdapter(this, 0, array);
			listview.setAdapter(adapter);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void test() {
		ArrayList<String> array = new ArrayList<String>();
		array.add("ミキモピン");
		array.add("アグロバクテリウム");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
		listview.setAdapter(adapter);
	}
	
	public MarkedList() {
		// TODO Auto-generated constructor stub		
	}
}


class ImageItem {
	private Bitmap bitmap_;
	public void setBitmap(Bitmap bitmap) {
		bitmap_ = bitmap;
	}
	public Bitmap getBitmap() {
		return bitmap_;
	}
}


class ImageAdapter extends ArrayAdapter<ImageItem> {
	private LayoutInflater layoutInflater_;

	public ImageAdapter(Context context, int textViewResourceId, List<ImageItem> objects) {
		super(context, textViewResourceId, objects);
		layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 特定の行(position)のデータを得る
		ImageItem item = (ImageItem)getItem(position);
		// convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
		if (null == convertView) {
			convertView = layoutInflater_.inflate(com.artifex.mupdfdemo.R.layout.marked_list, null);
		}
		// ImageItemのデータをViewの各Widgetにセットする
		ImageView imageView;
		imageView = (ImageView)convertView.findViewById(com.artifex.mupdfdemo.R.id.image);
		imageView.setImageBitmap(item.getBitmap());
		return convertView;
	}
}