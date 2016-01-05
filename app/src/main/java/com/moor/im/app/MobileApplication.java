package com.moor.im.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.csipsimple.api.SipConfigManager;
import com.csipsimple.service.SipService;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.loopj.android.http.AsyncHttpClient;
import com.moor.im.ui.dialog.KickedActicity;
import com.moor.im.utils.CacheUtils;
import com.moor.im.utils.FaceConversionUtil;
import com.moor.im.utils.LogUtil;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;

/**
 * 手机的Application,进行一些项目启动时的初始化工作
 * 
 * @author LongWei
 * 
 */
public class MobileApplication extends Application {

	private static MobileApplication mobileApplication;
	public static CacheUtils cacheUtil;

	public static boolean isKFSDK = false;
	
	private List<Activity> activities = new ArrayList<Activity>();

	/**
	 * 保存到文件中的日志
	 */
	public static final Logger logger = LoggerFactory.getLogger(MobileApplication.class);

	/**
	 * 网络请求框架
	 */
	public static AsyncHttpClient httpclient;

	private static Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x111:
				Intent intent = new Intent(mobileApplication, KickedActicity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mobileApplication.startActivity(intent);
				break;
			}
		};
	};

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		MobileApplication.handler = handler;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.d("MobileApplication", "进入了onCreate方法");
		mobileApplication = this;
		cacheUtil = CacheUtils.get(mobileApplication);
		httpclient = new AsyncHttpClient();
		
		httpclient.setMaxRetriesAndTimeout(0, 1000);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(
						getApplicationContext());
			}
		}).start();

		startSipService();
		startIMService();


		/**
		 * 初始化配置文件
		 */
//		PropertyConfigurator.getConfigurator(mobileApplication).configure();
//		final FileAppender fa = (FileAppender)logger.getAppender(1);
//		fa.setAppend(true);

	}

	/**
	 * 启动Voip的sip服务
	 */
	private void startSipService() {
		Thread t = new Thread("StartSip") {
			public void run() {
				Intent serviceIntent = new Intent(mobileApplication, SipService.class);
				startService(serviceIntent);
			}
		};
		t.start();
		LogUtil.d("MobileApplication", "启动SipService");
	}
	/**
	 * 启动IM服务
	 */
	private void startIMService() {
		Thread t = new Thread("StartImService") {
			public void run() {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.moor.im", "com.moor.im.tcpservice.service.IMService"));
				startService(intent);
			}
		};
		t.start();
		LogUtil.d("MobileApplication", "启动IMService");
	}


	
	public static MobileApplication getInstance() {
		return mobileApplication;
	}
	
	public void add(Activity a) {
		activities.add(a);
	}
	
	public void exit() {
		for (int i = 0; i < activities.size(); i++) {
			activities.get(i).finish();
		}
	}

}
