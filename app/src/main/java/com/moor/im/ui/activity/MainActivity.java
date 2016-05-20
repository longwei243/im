package com.moor.im.ui.activity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.csipsimple.api.SipUri;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.DialEvent;
import com.moor.im.event.LoginEvent;
import com.moor.im.http.HttpManager;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.Group;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAQueue;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.adapter.DiscussionAdapter;
import com.moor.im.ui.adapter.GroupAdapter;
import com.moor.im.ui.fragment.ContactFragment;
import com.moor.im.ui.fragment.DialFragment;
import com.moor.im.ui.fragment.DialFragment.OnMakeCallListener;
import com.moor.im.ui.fragment.MessageFragment;
import com.moor.im.ui.fragment.SetupFragment;
import com.moor.im.ui.view.ChangeColorTabItem;
import com.moor.im.ui.view.appmsg.AppMsg;
import com.moor.im.utils.LogUtil;
import com.moor.im.utils.NullUtil;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;

import de.greenrobot.event.EventBus;

/**
 * 主界面，控制滑动界面和底部tab
 * 
 * @author LongWei
 * 
 */
public class MainActivity extends FragmentActivity implements
		OnPageChangeListener, OnClickListener, OnMakeCallListener {

	private ViewPager mViewPager;
	private List<Fragment> mTabsFragment = new ArrayList<Fragment>();
	private FragmentPagerAdapter mAdapter;

	private List<ChangeColorTabItem> mTabItem = new ArrayList<ChangeColorTabItem>();

	private Fragment fragment_message;
	private Fragment fragment_contact;
	private Fragment fragment_dial;
	private Fragment fragment_setup;

	SharedPreferences MyPreferences;
	SharedPreferences.Editor editor;
	private SharedPreferences sp;

	private ImageView title_btn_contact_search;

	private AudioManager audioManager;
	private User user = UserDao.getInstance().getUser();

	private ISipService service;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			System.out.println("-----------执行了 ServiceConnection");
			service = ISipService.Stub.asInterface(arg1);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
		}
	};
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case 0x1111:
				AppMsg.Style style = new AppMsg.Style(AppMsg.LENGTH_STICKY, R.color.sticky);
				AppMsg provided = AppMsg.makeText(MainActivity.this, "当前没有网络连接", style, R.layout.sticky);
				provided.getView()
                .findViewById(R.id.remove_btn)
                .setOnClickListener(new CancelAppMsg());
				provided.show();
				break;
			case 0x1100:
				AppMsg.cancelAll(MainActivity.this);
				break;
			}
		};
	};
	private NetChangedReceiver ncr;
	static class CancelAppMsg implements View.OnClickListener {
       
        @Override
        public void onClick(View v) {
            AppMsg.cancelAll();
        }
    }

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		MobileApplication.getInstance().add(this);

		setContentView(R.layout.activity_main);

		MyPreferences = getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		editor = MyPreferences.edit();
		sp = this.getSharedPreferences("SP", 4);
		editor.putString("ClickState", "STATE_SHOW");
		editor.putString("moveState", "STATE_MOVE");
		editor.commit();
		EventBus.getDefault().register(this);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		ncr = new NetChangedReceiver();
		IntentFilter intentFilter = new IntentFilter("netchanged");
		registerReceiver(ncr, intentFilter);

		title_btn_contact_search = (ImageView) findViewById(R.id.title_btn_contact_search);

		title_btn_contact_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ContactsSearchActivity.class);
				startActivity(intent);
			}
		});

		mViewPager = (ViewPager) findViewById(R.id.id_main_viewpager);

		initDatas();

		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		
		if (MyPreferences.getInt("loginCount", 0) == 1) {
//			System.out.println("第一次创建sip账户:"+MyPreferences.getInt("loginCount", 0));
			// 创建了sip电话的账户，该账户只能有一个
			String sipExten = user.sipExten;
			String displayName = user.displayName;
			String sipExtenSecret = user.sipExtenSecret;
			String pbxSipAddr = user.pbxSipAddr;

			createAccount(displayName, sipExten, sipExtenSecret, pbxSipAddr);
			// 发送广播，通知重启sip栈
			Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
			sendBroadcast(intent);
		} else {
			System.out.println("sip账户已经存在了");
			// 发送广播，通知重启sip栈
			Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
			sendBroadcast(intent);
		}

		// 绑定sip电话服务
		bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
				, connection,
				Context.BIND_AUTO_CREATE);
//		bindService(new Intent(SipManager.INTENT_SIP_SERVICE), connection,
//				Context.BIND_AUTO_CREATE);


		List<Contacts> list = getContactsFromDB();
		if (list != null && list.size() != 0) {
//			System.out.println("联系人列表不是空的");
		} else {
//			System.out.println("联系人列表是空的");
//			LogUtil.d("MainActivity", "从网络加载联系人");
			getContactsFromNet();
		}
		if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_GROUP) == null) {
//			LogUtil.d("MainActivity", "从网络加载群组信息");
			getGroupDataFromNet();
		}

		if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DISCUSSION) == null) {
//			LogUtil.d("MainActivity", "从网络加载讨论组信息");
			getDiscussionDataFromNet();
		}
//		LogUtil.d("MainActivity", "从网络获取应用版本号");
		getVersionFromNet();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//检测是否修改过密码
		if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_CHANGED_PASSWORD) != null) {
			if("false".equals(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_CHANGED_PASSWORD))) {
				//修改过了
				sp.edit().clear().commit();
				editor.clear().commit();
				getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);
				UserDao.getInstance().deleteUser();
				ContactsDao.getInstance().clear();
				NewMessageDao.getInstance().deleteAllMsgs();

				Toast.makeText(MainActivity.this, "您最近修改过密码，请重新登录", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}
	}

	public void getDiscussionDataFromNet() {
		HttpManager.getDiscussionByUser(sp.getString("connecTionId", ""),
				new GetDiscussionResponseHandler());
	}

	class GetDiscussionResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
//			LogUtil.d("MainActivity", "获取讨论组返回结果:" + responseString);
			if ("true".equals(succeed)) {
				//将数据存到本地
				MobileApplication.cacheUtil.put(CacheKey.CACHE_DISCUSSION, responseString);

				//通知消息页重新刷新一次数据
				Message msg = new Message();
				msg.obj = "msg";
				MobileApplication.getHandler().sendMessage(msg);
			} else {
			}
		}
	}

	public void getGroupDataFromNet() {
		HttpManager.getGroupByUser(sp.getString("connecTionId", ""),
				new GetGroupResponseHandler());
	}


	class GetGroupResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
//			LogUtil.d("MainActivity", "获取群组返回结果:" + responseString);
			if ("true".equals(succeed)) {
				//将数据存到本地
				MobileApplication.cacheUtil.put(CacheKey.CACHE_GROUP, responseString);
				Message msg = new Message();
				msg.obj = "msg";
				MobileApplication.getHandler().sendMessage(msg);
//				LogUtil.d("MainActivity", "通知消息页更新一下");
			}
		}
	}

	/**
	 * 从网络上加载联系人
	 */
	private void getContactsFromNet() {
//		System.out.println("从网络加载联系人");
		HttpManager.getContacts(sp.getString("connecTionId", ""),
				new getContactsResponseHandler());
	}

	class getContactsResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			if ("true".equals(succeed)) {

				ContactsDao.getInstance().clear();
				List<Contacts> contacts = HttpParser.getContacts(responseString);

				ContactsDao.getInstance().saveContacts(contacts);
				Message msg = new Message();
				msg.obj = "msg";
				MobileApplication.getHandler().sendMessage(msg);
			} else {
			}
		}
	}

	/**
	 * 从数据库读取所有联系人
	 */
	private List<Contacts> getContactsFromDB() {
//		System.out.println("从数据库加载联系人");
		return ContactsDao.getInstance().getContacts();
	}

	
	
	/**
	 * 从网络获取版本号
	 */
	public void getVersionFromNet() {
		HttpManager.getVersion(sp.getString("connecTionId", ""),
				new GetVersionResponseHandler());
	}
	
	class GetVersionResponseHandler extends TextHttpResponseHandler {

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				try {
					JSONObject jsonObject = new JSONObject(responseString);
					JSONObject jb = (JSONObject) jsonObject.get("AppVersion");
					String version = jb.getString("android");
					if(!getVersion().equals(NullUtil.checkNull(version))) {
						//有更新
						Intent updateIntent = new Intent(MainActivity.this, UpdateActivity.class);
						startActivity(updateIntent);
					}else {
						//没有更新
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
//				if(!message.equals("404"))
//					Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT)
//						.show();
			}
		}
	}
	/**
	 * 获取应用版本号
	 * @return
	 */
	public String getVersion() {
		 try {
			 PackageManager manager = this.getPackageManager();
			 PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			 String version = info.versionName;
			 return version;
		 } catch (Exception e) {
			 e.printStackTrace();
			 return "";
		}
	}

	private SipProfile createAccount(String displayName, String name,
			String password, String serverIp) {
		SipProfile account = new SipProfile();

		account.display_name = displayName;

		String[] serverParts = serverIp.split(":");
		account.acc_id = "<sip:" + SipUri.encodeUser(name) + "@"
				+ serverParts[0].trim() + ">";

		String regUri = "sip:" + serverIp;
		account.reg_uri = regUri;
		account.proxies = new String[] { regUri };

		account.realm = "*";
		account.username = name;
		account.data = password;
		account.scheme = SipProfile.CRED_SCHEME_DIGEST;
		account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
		account.transport = SipProfile.TRANSPORT_UDP;
		Uri uri = getContentResolver().insert(SipProfile.ACCOUNT_URI,
				account.getDbContentValues());
//		System.out.println("创建了sip账户");
		return account;

	}


	/**
	 * 初始化Fragment以及fragmentadapter
	 */
	private void initDatas() {
		fragment_message = new MessageFragment();
		mTabsFragment.add(fragment_message);

		fragment_contact = new ContactFragment();
		mTabsFragment.add(fragment_contact);

		fragment_dial = new DialFragment();
		mTabsFragment.add(fragment_dial);

		fragment_setup = new SetupFragment();
		mTabsFragment.add(fragment_setup);

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return mTabsFragment.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mTabsFragment.get(arg0);
			}
		};

		initTabIndicator();

	}

	/**
	 * 初始化底部tab，设置监听
	 */
	private void initTabIndicator() {
		ChangeColorTabItem tab_item_message = (ChangeColorTabItem) findViewById(R.id.id_indicator_one);
		ChangeColorTabItem tab_item_contact = (ChangeColorTabItem) findViewById(R.id.id_indicator_two);
		ChangeColorTabItem tab_item_dial = (ChangeColorTabItem) findViewById(R.id.id_indicator_three);
		ChangeColorTabItem tab_item_setup = (ChangeColorTabItem) findViewById(R.id.id_indicator_four);

		mTabItem.add(tab_item_message);
		mTabItem.add(tab_item_contact);
		mTabItem.add(tab_item_dial);
		mTabItem.add(tab_item_setup);

		tab_item_message.setOnClickListener(this);
		tab_item_contact.setOnClickListener(this);
		tab_item_dial.setOnClickListener(this);
		tab_item_setup.setOnClickListener(this);

		tab_item_message.setIconAlpha(1.0f);
	}

	/**
	 * 重置所有tab
	 */
	private void resetOtherTabs() {
		for (int i = 0; i < mTabItem.size(); i++) {
			mTabItem.get(i).setIconAlpha(0);
		}
	}

	@Override
	public void onClick(View v) {

		resetOtherTabs();

		switch (v.getId()) {
		case R.id.id_indicator_one:
			mTabItem.get(0).setIconAlpha(1.0f);
			mViewPager.setCurrentItem(0, false);

			editor.putString("moveState", "STATE_MOVE");
			editor.commit();
			break;
		case R.id.id_indicator_two:
			mTabItem.get(1).setIconAlpha(1.0f);
			mViewPager.setCurrentItem(1, false);

			editor.putString("moveState", "STATE_MOVE");
			editor.commit();
			break;
		case R.id.id_indicator_three:

			final String dia_key = MyPreferences.getString("ClickState", "")
					.toString().trim();
			final String dia_t1 = MyPreferences.getString("moveState", "")
					.toString().trim();

			// 若为“STATE_MOVE”,则表示从别的tabSTATE_MOVE过来的，只需切换过页面即可
			if (dia_t1.equals("STATE_MOVE")) {
				mTabItem.get(2).setIconAlpha(1.0f);
				mViewPager.setCurrentItem(2, false);

				editor.putString("moveState", "STATE_CURRENT");
				editor.commit();
			} else {
				// 就在当前界面判断是否STATE_SHOW
				if (dia_key.equals("") | dia_key.equals("STATE_SHOW")) {
					editor.putString("ClickState", "STATE_HIDE");
					editor.commit();
					// 设置隐藏后的图标
					mTabItem.get(2).setIcon(
							R.drawable.phone_pulldown_keyboard_normal);
					mTabItem.get(2).setIconAlpha(1.0f);
				} else {
					editor.putString("ClickState", "STATE_SHOW");
					editor.commit();
					mTabItem.get(2).setIcon(
							R.drawable.phone_popup_keyboard_normal);
					mTabItem.get(2).setIconAlpha(1.0f);
				}
			}
			EventBus.getDefault().post(new DialEvent());

			break;
		case R.id.id_indicator_four:
			mTabItem.get(3).setIconAlpha(1.0f);
			mViewPager.setCurrentItem(3, false);

			editor.putString("moveState", "STATE_MOVE");
			editor.commit();
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		if (positionOffset > 0) {
			ChangeColorTabItem left = mTabItem.get(position);
			ChangeColorTabItem right = mTabItem.get(position + 1);
			left.setIconAlpha(1 - positionOffset);
			right.setIconAlpha(positionOffset);
		}

	}

	@Override
	public void onPageSelected(int arg0) {
		if (arg0 != 2) {

			editor.putString("moveState", "STATE_MOVE");
			editor.commit();
			EventBus.getDefault().post(new DialEvent());
		} else {
			editor.putString("moveState", "STATE_CURRENT");
			editor.commit();

		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		unbindService(connection);
		unregisterReceiver(ncr);
	}

	public void onEventMainThread(LoginEvent loginEvent) {
		switch (loginEvent) {
			case LOGIN_FAILED:
				//登录失败400，用户后台改了密码了
				Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(loginIntent);
				finish();
				break;
		}
	}
	@Override
	public void makeCall(String callee) {
		// TODO 获取id
		Long id = -1L;
		Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null,
				null, null, null);
		if (c != null) {
			while (c.moveToNext()) {
				id = c.getLong(c.getColumnIndex("id"));
			}
		}
//		System.out.println(callee);
//		System.out.println(service);
		try {
			service.makeCall(callee, id.intValue());
		} catch (RemoteException e) {
			Toast.makeText(MainActivity.this, "拨打电话失败", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);  
            return true; 
		}
		return super.onKeyDown(keyCode, event); 
	}
	
	class NetChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if("closed".equals(intent.getStringExtra("netstate"))) {
				handler.sendEmptyMessage(0x1111);
			}else if("connected".equals(intent.getStringExtra("netstate"))) {
				handler.sendEmptyMessage(0x1100);
			}
		}
	}



}
