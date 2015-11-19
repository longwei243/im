package com.moor.im.ui.activity;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.csipsimple.api.SipProfile;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.LoginEvent;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.tcpservice.service.IMService;
import com.moor.im.tcpservice.service.IMServiceConnector;
import com.moor.im.ui.view.appmsg.AppMsg;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NetUtils;

import de.greenrobot.event.EventBus;

/**
 * 登陆页面
 * 
 * @author Mr.li
 * 
 */

public class LoginActivity extends Activity implements OnClickListener {

	private EditText mLoginName, mLoginPass;
	private Button mLoginLog;
	private SharedPreferences sp;
	private Editor editor;
	private String connectionid;
	private ProgressBar pb;
	
	private IMService imService;

	private SocketManager socketManager;


	SharedPreferences myPreferences;
	SharedPreferences.Editor myeditor;
	

	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
		@Override
		public void onIMServiceConnected() {
			imService = imServiceConnector.getIMService();
			if(imService != null) {
				socketManager = imService.getSocketMgr();
			}
		}
		@Override
		public void onServiceDisconnected() {
			
		}
	};

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:

				// 获取用户信息
				connectionid = sp.getString("connecTionId", "");
				HttpManager.getUserInfo(connectionid,
						new GetUserInfoResponseHandler());
				break;
			case 2:
				Toast.makeText(LoginActivity.this, "登录失败,用户名或密码错误",
						Toast.LENGTH_SHORT).show();
				mLoginLog.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
				break;
			case 3:
				mLoginLog.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
				
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
		EventBus.getDefault().register(this);
		
		sp = this.getSharedPreferences("SP", 4);
		editor = sp.edit();

		myPreferences = getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		myeditor = myPreferences.edit();

		imServiceConnector.connect(LoginActivity.this);

		init();
		registerListener();
		
		if(!NetUtils.hasDataConnection(LoginActivity.this)) {
			AppMsg am = AppMsg.makeText(LoginActivity.this, "没有网络连接啊", AppMsg.STYLE_ALERT);
			am.setLayoutGravity(Gravity.BOTTOM);
			am.show();
		}

	}

	// 初始化方法
	public void init() {
		mLoginName = (EditText) this.findViewById(R.id.login_name);
		mLoginPass = (EditText) this.findViewById(R.id.login_pass);
		mLoginLog = (Button) this.findViewById(R.id.login_log);
		pb = (ProgressBar) this.findViewById(R.id.login_progress);

		// ===========完了删了
		mLoginName.setText("8029@7moor");
		mLoginPass.setText("8029");
//		mLoginName.setText("8001@phoneTest");
//		mLoginPass.setText("8001");
		// ====================

		String name = sp.getString("loginName", "");
		String password = sp.getString("loginPass", "");
		if (!"".equals(name) && !"".equals(password)) {
			mLoginName.setText(name);
			mLoginPass.setText(password);
		}

	}

	// 注册监听方法
	public void registerListener() {
		mLoginLog.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login_log:
			mLoginLog.setVisibility(View.GONE);
			pb.setVisibility(View.VISIBLE);
			final String username = mLoginName.getText().toString().trim();
			final String password = mLoginPass.getText().toString().trim();
			if (!"".equals(username) && !"".equals(password)) {
				editor.putString("loginName", mLoginName.getText().toString()
						.trim());
				editor.putString("loginPass", mLoginPass.getText().toString()
						.trim());
				editor.putString("isStoreUsernamePasswordRight", "false");
				editor.commit();
				new Thread(){
					@Override
					public void run() {
						try {
							if(imServiceConnector != null && imServiceConnector.getBinder() != null){
								imServiceConnector.getBinder().login(username, password);
							}else{
								imServiceConnector.connect(LoginActivity.this);
								imServiceConnector.getBinder().login(username, password);
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}.start();
			} else {
				Toast.makeText(LoginActivity.this, "请输入账号和密码",
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	// 获取用户信息
	class GetUserInfoResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(LoginActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
			mLoginLog.setVisibility(View.VISIBLE);
			pb.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
//			LogUtil.d("LoginActivity", "获取用户信息返回的数据是:"+responseString);
			if ("true".equals(succeed)) {
				User user = HttpParser.getUserInfo(responseString);
				// 用户信息存入数据库
				UserDao.getInstance().deleteUser();
				UserDao.getInstance().insertUser(user);

				if(!mLoginName.getText().toString().trim().equals(sp.getString("loginName", ""))) {
					getContentResolver().delete(SipProfile.ACCOUNT_URI, "1", null);
					MessageDao.getInstance().deleteAllMsgs();
					NewMessageDao.getInstance().deleteAllMsgs();
					ContactsDao.getInstance().clear();
				}

				myeditor.putInt("loginCount", myPreferences.getInt("loginCount", 0) + 1);
				myeditor.commit();
				// 跳转到首页
				Intent main = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(main);
				finish();
			} else {
				Toast.makeText(LoginActivity.this, "网络有问题", Toast.LENGTH_SHORT)
						.show();
				mLoginLog.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		imServiceConnector.disconnect(LoginActivity.this);
		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 登录事件响应方法
	 * @param event
	 */
	public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOGIN_SUCCESS:
            	LogUtil.i("LOGIN", "LoginEvent登录成功了");
				String name = sp.getString("loginName", "");
				String password = sp.getString("loginPass", "");
//				if("".equals(name) && "".equals(password)) {
					handler.sendEmptyMessage(1);
//				}else {
//					// 跳转到首页
//					editor.putInt("loginCount", sp.getInt("loginCount", 0) + 1);
//					editor.commit();
//					Intent main = new Intent(LoginActivity.this, MainActivity.class);
//					startActivity(main);
//					finish();
//				}

                break;
            case LOGIN_FAILED:
            	LogUtil.i("LOGIN", "LoginEvent登录失败了");
            	handler.sendEmptyMessage(2);
                break;
			case NONE:
				handler.sendEmptyMessage(3);
				break;
        }
    }

}
