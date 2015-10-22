package com.moor.im.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 获取屏幕宽度和高度的工具类
 * @author LongWei
 *
 */
public class WindowUtils {

	/**
	 * 获得屏幕的宽度
	 * @param context
	 * @return
	 */
	public static int getWindowWidth(Context context) {
	
		WindowManager wm = (WindowManager) (context
				.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int mScreenWidth = dm.widthPixels;
		return mScreenWidth;
	}
	/**
	 * 获得屏幕的高度
	 * @param context
	 * @return
	 */
	public static int getWindowHeigh(Context context) {
		
		WindowManager wm = (WindowManager) (context
				.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int mScreenHeigh = dm.heightPixels;
		return mScreenHeigh;
	}
}
