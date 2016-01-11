package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.event.LoginEvent;
import com.moor.im.utils.TimeUtil;

import de.greenrobot.event.EventBus;

/**
 * 启动页
 * 
 * @author Mr.li
 * 
 */
public class StartActivity extends Activity {

	SharedPreferences myPreferences;
	SharedPreferences.Editor editor;

	SharedPreferences sp;
	SharedPreferences.Editor spEditor;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x11:
				// 跳转到登陆页面
				Intent Login = new Intent(StartActivity.this,
						LoginActivity.class);
				startActivity(Login);
//				overridePendingTransition(R.anim.anim_fromright_toup6,
//						R.anim.anim_down_toleft6);
				finish();
				break;
				case 0x22:
					// 跳转到引导页面
					Intent intro = new Intent(StartActivity.this,
							IntroActivity.class);
					startActivity(intro);
//				overridePendingTransition(R.anim.anim_fromright_toup6,
//						R.anim.anim_down_toleft6);
					finish();
					break;
				case 0x33:
					// 跳转到主页面
					editor.putInt("loginCount", myPreferences.getInt("loginCount", 0) + 1);
					editor.commit();
					Intent main = new Intent(StartActivity.this,
							MainActivity.class);
					startActivity(main);
//				overridePendingTransition(R.anim.anim_fromright_toup6,
//						R.anim.anim_down_toleft6);
					finish();
					break;
			}

		}

	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		myPreferences = getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		editor = myPreferences.edit();

//		MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "StartAty，进入了onCreate方法");
		sp = getSharedPreferences("SP", 0);
		spEditor = sp.edit();
		String name = sp.getString("loginName", "");
		String password = sp.getString("loginPass", "");
		String isStoreUsernamePasswordRight = sp.getString("isStoreUsernamePasswordRight", "false");
		System.out.println("isStoreUsernamePasswordRight："+isStoreUsernamePasswordRight);
		if(name != null && password !=null && !"".equals(name) && !"".equals(password) && "true".equals(isStoreUsernamePasswordRight)){
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						Message msg = new Message();
						msg.what = 0x33;
						handler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
//			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "StartAty，自动跳转到mianAty方法 name=" + name + " password=" + password);
		}else{
			//		boolean firstRun = myPreferences.getBoolean("isFirstRun", true);
//		if(firstRun) {
			//是第一次进应用,进入引导页
//			editor.putBoolean("isFirstRun", false);
//			editor.commit();
//			new Thread() {
//				@Override
//				public void run() {
//					try {
//						Thread.sleep(2000);
//						Message msg = new Message();
//						msg.what = 0x22;
//						handler.sendMessage(msg);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
//
//		}else {
			//进登录界面
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						Message msg = new Message();
						msg.what = 0x11;
						handler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
//		}
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
