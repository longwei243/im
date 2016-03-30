package com.moor.im.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 获取屏幕宽度和高度的工具类
 * @author LongWei
 *
 */
public class WindowUtils {
	// 根据屏幕密度转换
	private static float mPixels = 0.0F;
	private static float density = -1.0F;
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

	/**
	 * 像素转化dip
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue){

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int)(pxValue / scale + 0.5f);

	}

	/**
	 * dip转化像素
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(float dipValue){
		final float scale = Resources.getSystem().getDisplayMetrics().density;
		return (int)(dipValue * scale + 0.5f);

	}

	/**
	 * @param context
	 * @param height
	 * @return
	 */
	public static int getMetricsDensity(Context context , float height) {
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(localDisplayMetrics);
		return Math.round(height * localDisplayMetrics.densityDpi / 160.0F);
	}


	public static int fromDPToPix(Context context, int dp) {
		return Math.round(getDensity(context) * dp);
	}

	/**
	 * @param context
	 * @return
	 */
	public static float getDensity(Context context) {

		if (density < 0.0F)
			density = context.getResources().getDisplayMetrics().density;
		return density;
	}

	public static int round(Context context, int paramInt) {
		return Math.round(paramInt / getDensity(context));
	}
}
