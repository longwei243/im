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
import com.tencent.bugly.crashreport.CrashReport;

/**
 * 手机的Application,进行一些项目启动时的初始化工作
 * 
 * @author LongWei
 * 
 */
public class MobileApplication extends Application {

	private static MobileApplication mobileApplication;
	public static CacheUtils cacheUtil;
	
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

		IMChatManager.getInstance().setOnInitListener(new InitListener() {
			@Override
			public void oninitSuccess() {
				Log.d("MobileApplication", "sdk初始化成功");
				//初始化表情,界面效果需要
				new Thread(new Runnable() {
					@Override
					public void run() {
						com.m7.imkfsdk.utils.FaceConversionUtil.getInstace().getFileText(
								MobileApplication.getInstance());
					}
				}).start();
			}

			@Override
			public void onInitFailed() {
				Log.d("MobileApplication", "sdk初始化失败");
			}
		});

		//初始化IMSdk,启动了IMService
		IMChatManager.getInstance().init(MobileApplication.getInstance(), "com.moor.imkf.KEFU_NEW_MSG", "f228f440-7882-11e5-944c-43cb6c167371", "龙伟测试号", "7788");


		/**
		 * 初始化配置文件
		 */
//		PropertyConfigurator.getConfigurator(mobileApplication).configure();
//		final FileAppender fa = (FileAppender)logger.getAppender(1);
//		fa.setAppend(true);

		/**
		 * 初始化异常捕获
		 */
		CrashReport.initCrashReport(mobileApplication, "900005144", true);
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

		if (mobileApplication == null) {
			mobileApplication = new MobileApplication();
		}
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
