package com.moor.im.ui.activity;

import java.util.Random;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.CallingParser;
import com.moor.im.receiver.PhoneReceiver;

/**
 * 正在呼叫
 * 
 * @author Mr.li
 * 
 */
public class CallingActivity extends Activity {

	private String userName;
	private TextView mCallingNumber;
	
	private SharedPreferences sp;

	User user = UserDao.getInstance().getUser();
	
	// 当有来电的时候关闭当前页面
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if ("0".equals(msg.what + "")) {
				finish();
//				answerRingingCall();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobileApplication.getInstance().add(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_calling);
		Intent intent = getIntent();
		userName = intent.getStringExtra("phone_number");
		System.out.println("回拨的号码是："+userName);
		PhoneReceiver.handler = handler;
		sp = this.getSharedPreferences("SP", 4);
		init();
		dialBack(userName);

	}

	// 初始化方法
	public void init() {
		mCallingNumber = (TextView) findViewById(R.id.calling_number);
		mCallingNumber.setText(userName);
	}

	private void dialBack(final String otherNum) {
		String serverIp = user.pbxSipAddr;
		String servierIpStr = serverIp.split(":")[0];
		String actionId = new Random().nextInt() * 10000 + "";
		String account = user.account;
		String exten = user.exten;
		String pbx = user.pbx;
		
		String urlStr = "http://"+servierIpStr+"/app?Action=Dialout&ActionID="+actionId+"&Account="+account+"&Exten="+otherNum+"&FromExten="+exten+"&PBX="+pbx+"&ExtenType=Local";
		System.out.println("回拨请求地址是："+urlStr);
		MobileApplication.httpclient.get(urlStr, new TextHttpResponseHandler() {
			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
				Toast.makeText(CallingActivity.this, "请检查您的网络问题",
						Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onSuccess(int i, Header[] headers, String response) {
				System.out.println("电话回拨返回的数据是："+response);
				if(!"".equals(response) && response != null) {
					try {
						JSONObject jb = new JSONObject(response);
						boolean succeed = jb.getBoolean("Succeed");
						if(!succeed) {
							Toast.makeText(CallingActivity.this, "呼叫失败",
									Toast.LENGTH_LONG).show();
							finish();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}


	/**
	 * 自动接听
	 */
	private void answerRingingCall() {

		try {
			// 放开耳机按钮
			Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent3.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent2);
			sendOrderedBroadcast(localIntent3,
					"android.permission.CALL_PRIVILEGED");

			// 插耳机
			Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
			localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			localIntent1.putExtra("state", 1);
			localIntent1.putExtra("microphone", 1);
			localIntent1.putExtra("name", "Headset");
			sendOrderedBroadcast(localIntent1,
					"android.permission.CALL_PRIVILEGED");
			// 按下耳机按钮
			Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent2.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent1);
			sendOrderedBroadcast(localIntent2,
					"android.permission.CALL_PRIVILEGED");
			// 放开耳机按钮
			localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
			localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP,
					KeyEvent.KEYCODE_HEADSETHOOK);
			localIntent3.putExtra("android.intent.extra.KEY_EVENT",
					localKeyEvent2);
			sendOrderedBroadcast(localIntent3,
					"android.permission.CALL_PRIVILEGED");
			// 拔出耳机
			Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
			localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			localIntent4.putExtra("state", 0);
			localIntent4.putExtra("microphone", 1);
			localIntent4.putExtra("name", "Headset");
			sendOrderedBroadcast(localIntent4,
					"android.permission.CALL_PRIVILEGED");
		} catch (Exception e) {
			Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);  
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);  
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);  
			CallingActivity.this.sendOrderedBroadcast(meidaButtonIntent, null);

		}

	}

}
