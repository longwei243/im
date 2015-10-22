package com.moor.im.ui.activity;

import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.User;
import com.moor.im.ui.base.MyBaseActivity;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.Utils;

public class ContactDetailActivity extends MyBaseActivity implements
		OnClickListener {

	private String _id,otherName;
	private Button mSendMessage, mCallPhone;
	private TextView contact_detail_tv_name,
					contact_detail_tv_num, 
					contact_detail_tv_phone,
					contact_detail_tv_email,
					contact_detail_tv_product;

	RoundImageView contact_detail_image;

	ImageView title_btn_back;

	User user = UserDao.getInstance().getUser();
	
	private Contacts contact;
	private SharedPreferences sp;
	private ISipService service;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			service = ISipService.Stub.asInterface(arg1);

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			service = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		MobileApplication.getInstance().add(this);
		setContentView(R.layout.activity_contact_detail);
		sp = getSharedPreferences("SP", 4);
		Intent intent = getIntent();
		_id = intent.getStringExtra("_id");
		otherName = intent.getStringExtra("otherName");
		contact = (Contacts) intent.getSerializableExtra("contact");
		
		init();
		registerListener();
		

		//绑定sip电话服务
//		bindService(new Intent(SipManager.INTENT_SIP_SERVICE),
//				connection, Context.BIND_AUTO_CREATE);
		bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
				, connection,
				Context.BIND_AUTO_CREATE);
	}

	// 初始化方法
	public void init() {
		mSendMessage = (Button) this.findViewById(R.id.send_message);
		mCallPhone = (Button) this.findViewById(R.id.call_phone);
		contact_detail_tv_name = (TextView) findViewById(R.id.contact_detail_tv_name);
		contact_detail_tv_num = (TextView) findViewById(R.id.contact_detail_tv_num);
		contact_detail_tv_phone = (TextView) findViewById(R.id.contact_detail_tv_phone);
		contact_detail_tv_email = (TextView) findViewById(R.id.contact_detail_tv_email);
		contact_detail_tv_product = (TextView) findViewById(R.id.contact_detail_tv_product);
		if("".equals(contact.mobile)) {
			mCallPhone.setVisibility(View.GONE);
			contact_detail_tv_phone.setText("未绑定");
		}else{
			contact_detail_tv_phone.setText(contact.mobile);
		}
		if("".equals(contact.email)) {
			contact_detail_tv_email.setText("未绑定");
		}else{
			contact_detail_tv_email.setText(contact.email);
		}
		contact_detail_tv_name.setText(contact.displayName);
		contact_detail_tv_num.setText(contact.exten);
		if("zj".equals(contact.product)) {
			contact_detail_tv_product.setText("企业总机");
		}else if("cc".equals(contact.product)){
			contact_detail_tv_product.setText("联络中心");
		}

		contact_detail_image = (RoundImageView) findViewById(R.id.contact_detail_image);
		String im_icon = contact.im_icon;
		if(im_icon != null && !"".equals(im_icon)) {
			Glide.with(this).load(im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(contact_detail_image);
		}else {
			Glide.with(this).load(R.drawable.head_default_local).asBitmap().into(contact_detail_image);
		}

		title_btn_back = (ImageView) this.findViewById(R.id.title_btn_back);

		
	}

	// 注册监听方法
	public void registerListener() {
		mSendMessage.setOnClickListener(this);
		mCallPhone.setOnClickListener(this);
		title_btn_back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.send_message:

			Intent chat = new Intent(ContactDetailActivity.this,
					ChatActivity.class);
			chat.putExtra("otherName", otherName+"");
			chat.putExtra("type", "User");
			chat.putExtra("_id", _id);
			
			startActivity(chat);
			break;

		case R.id.call_phone:
			callingDialog();
			break;
		case R.id.title_btn_back:
			finish();
			break;
		default:
			break;
		}
	}
	
	public void callingDialog() {
		LayoutInflater myInflater = LayoutInflater.from(ContactDetailActivity.this);
		final View myDialogView = myInflater.inflate(R.layout.calling_dialog,
				null);
		final Builder dialog = new AlertDialog.Builder(ContactDetailActivity.this)
				.setView(myDialogView);
		final AlertDialog alert = dialog.show();
		alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
		alert.getWindow().setGravity(Gravity.BOTTOM);

		// 直播
		LinearLayout mDirectSeeding = (LinearLayout) myDialogView
				.findViewById(R.id.direct_seeding_linear);
		mDirectSeeding.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					if (Utils.isNetWorkConnected(ContactDetailActivity.this)) {
						makeCall(contact.mobile);
					} else {
						Toast.makeText(ContactDetailActivity.this, "网络错误，请重试！",
								Toast.LENGTH_LONG).show();
					}
					alert.dismiss();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		// 回拨
		LinearLayout mCallReturn = (LinearLayout) myDialogView
				.findViewById(R.id.call_return_linear);
		if("zj".equals(user.product)) {
			mCallReturn.setVisibility(View.GONE);
		}
		mCallReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
					// TODO Auto-generated method stub
					if (Utils.isNetWorkConnected(ContactDetailActivity.this)) {
						// 跳转到正在通话页面
						Intent calling = new Intent(ContactDetailActivity.this,
								CallingActivity.class);
						calling.putExtra("phone_number", contact.mobile);
						startActivity(calling);
					} else {
						Toast.makeText(ContactDetailActivity.this, "网络错误，请重试！",
								Toast.LENGTH_LONG).show();
					}
					alert.dismiss();
				}

			
		});
	
		// 普通电话
		LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
				.findViewById(R.id.ordinary_call_linear);
		mOrdinaryCall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel://"
								+ contact.mobile));
				startActivity(intent);
				alert.dismiss();
			}
		});
		// 取消
		LinearLayout mCancelLinear = (LinearLayout) myDialogView
				.findViewById(R.id.cancel_linear);
		mCancelLinear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert.dismiss();
			}
		});
	}
	
	/**
	 * 拨打直拨电话
	 * @param callee
	 */
	public void makeCall(String callee) {
		//TODO 获取id 
		Long id = -1L;
		Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
		if(c != null) {
			while(c.moveToNext()) {
				id = c.getLong(c.getColumnIndex("id"));
			}
		}
//		System.out.println("sip账户ID是："+id);
		try {
			service.makeCall(callee, id.intValue());
		} catch (RemoteException e) {
			Toast.makeText(ContactDetailActivity.this, "拨打电话失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(connection);
	}
}
