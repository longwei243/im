package com.moor.im.ui.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;

/**
 * 基础Activity
 * 
 * @author Mr.li
 * 
 */
public class MyBaseActivity extends FragmentActivity {

	protected int width;
	protected int height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getDisplay();
		super.onCreate(savedInstanceState);
	}

	private void getDisplay() {
		DisplayMetrics metrics = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		width = metrics.widthPixels;
		height = metrics.heightPixels;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}