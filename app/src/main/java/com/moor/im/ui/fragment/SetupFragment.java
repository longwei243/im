package com.moor.im.ui.fragment;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.csipsimple.api.SipProfile;
import com.loopj.android.http.TextHttpResponseHandler;
import com.m7.imkfsdk.chat.ChatActivity;
import com.m7.imkfsdk.chat.OfflineMessageDialog;
import com.m7.imkfsdk.utils.FaceConversionUtil;
import com.moor.im.R;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.db.dao.MessageDao;
import com.moor.im.db.dao.NewMessageDao;
import com.moor.im.db.dao.UserDao;
import com.moor.im.event.UserIconUpdate;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.tcpservice.manager.LoginManager;
import com.moor.im.tcpservice.manager.SocketManager;
import com.moor.im.tcpservice.service.IMService;
import com.moor.im.tcpservice.service.IMServiceConnector;
import com.moor.im.ui.activity.AboutMeActivity;
import com.moor.im.ui.activity.ClipImageViewActivity;
import com.moor.im.ui.activity.EditActivity;
import com.moor.im.ui.activity.LoginActivity;
import com.moor.im.ui.activity.UpdateActivity;
import com.moor.im.ui.activity.UserInfoActivity;
import com.moor.im.ui.dialog.LoginOffDialog;
import com.moor.im.ui.view.RoundImageView;
import com.moor.im.utils.LogUtil;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.InitListener;
import com.moor.imkf.OnSessionBeginListener;

import de.greenrobot.event.EventBus;

public class SetupFragment extends Fragment{
	
	RelativeLayout setup_ll_loginoff, setup_ll_update,
			setup_ll_aboutme, setup_ll_icon,
			setup_ll_edit_name, setup_ll_edit_email, setup_ll_edit_phone, setup_ll_kefu;
	
	TextView user_detail_tv_name, user_detail_tv_num, user_detail_tv_email, user_detail_tv_phone;

	ImageView contact_detail_image;

	CheckBox setup_cb_gongdan;

	private LoginOffDialog loginoffDialog;
	
	static String connectionId;
	
	private SharedPreferences sp;


	SharedPreferences myPreferences;
	SharedPreferences.Editor editor;

	private IMService imService;
	
	private LoginManager loginMgr;
	private SocketManager socketMgr;

	private IMServiceConnector imServiceConnector = new IMServiceConnector(){

		@Override
		public void onIMServiceConnected() {
			imService = imServiceConnector.getIMService();
			if(imService != null) {
				loginMgr = imService.getLoginMgr();
				socketMgr = imService.getSocketMgr();
			}
		}

		@Override
		public void onServiceDisconnected() {
			
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imServiceConnector.connect(getActivity());
		EventBus.getDefault().register(this);


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		View view = inflater.inflate(R.layout.fragment_setup, null);
		setup_ll_loginoff = (RelativeLayout) view.findViewById(R.id.setup_ll_loginoff);
		setup_ll_loginoff.setOnClickListener(clickListener);
		
		setup_ll_update = (RelativeLayout) view.findViewById(R.id.setup_ll_update);
		setup_ll_update.setOnClickListener(clickListener);

		setup_ll_aboutme = (RelativeLayout) view.findViewById(R.id.setup_ll_aboutme);
		setup_ll_aboutme.setOnClickListener(clickListener);

		setup_ll_icon = (RelativeLayout) view.findViewById(R.id.setup_ll_icon);
		setup_ll_icon.setOnClickListener(clickListener);

		setup_ll_edit_name = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_name);
		setup_ll_edit_name.setOnClickListener(clickListener);

		setup_ll_edit_phone = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_phone);
		setup_ll_edit_phone.setOnClickListener(clickListener);

		setup_ll_edit_email = (RelativeLayout) view.findViewById(R.id.setup_ll_edit_email);
		setup_ll_edit_email.setOnClickListener(clickListener);
		
		sp = getActivity().getSharedPreferences("SP", 4);
		connectionId = sp.getString("connecTionId", "");

		myPreferences = getActivity().getSharedPreferences(MobileApplication.getInstance()
						.getResources().getString(R.string.spname),
				Activity.MODE_PRIVATE);
		editor = myPreferences.edit();

		User user = UserDao.getInstance().getUser();

		user_detail_tv_name = (TextView) view.findViewById(R.id.user_detail_tv_name);
		user_detail_tv_num = (TextView) view.findViewById(R.id.user_detail_tv_num);
		user_detail_tv_email = (TextView) view.findViewById(R.id.user_detail_tv_email);
		user_detail_tv_phone = (TextView) view.findViewById(R.id.user_detail_tv_phone);
		user_detail_tv_name.setText(user.displayName);
		user_detail_tv_num.setText(user.exten);
		user_detail_tv_email.setText(user.email);
		user_detail_tv_phone.setText(user.mobile);


		contact_detail_image = (ImageView) view.findViewById(R.id.user_icon);

		if(user.im_icon != null && !"".equals(user.im_icon)) {
			Glide.with(this).load(user.im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(contact_detail_image);
		}else {
			Glide.with(this).load(R.drawable.head_default_local).asBitmap().into(contact_detail_image);
		}

		setup_cb_gongdan = (CheckBox) view.findViewById(R.id.setup_cb_gongdan);
		boolean isc = myPreferences.getBoolean("gongdan", true);
		if(isc) {
			setup_cb_gongdan.setChecked(true);
		}else {
			setup_cb_gongdan.setChecked(false);
		}

		setup_cb_gongdan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					editor.putBoolean("gongdan", true);
					editor.commit();
					MobileApplication.getHandler().sendEmptyMessage(0x99);
				}else {
					editor.putBoolean("gongdan", false);
					editor.commit();
					MobileApplication.getHandler().sendEmptyMessage(0x97);
				}
			}
		});

		setup_ll_kefu = (RelativeLayout) view.findViewById(R.id.setup_ll_kefu);
		setup_ll_kefu.setOnClickListener(clickListener);

		return view;
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0x1234 && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			if (uri != null) {
				String realPath = getRealPathFromURI(uri);
				LogUtil.d("SetupFragment", "图片的路径是:"+realPath);
				Intent intent = new Intent(getActivity(), ClipImageViewActivity.class);
				intent.putExtra("imagePath", realPath);
				startActivity(intent);
			}
		}
	}

	// 获取字符
	public String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			// Do not call Cursor.close() on a cursor obtained using this
			// method,
			// because the activity will do that for you at the appropriate time
			Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null,
					null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Exception e) {
			return contentUri.getPath();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		User user = UserDao.getInstance().getUser();
		user_detail_tv_name.setText(user.displayName);
		user_detail_tv_num.setText(user.exten);
		user_detail_tv_email.setText(user.email);
		user_detail_tv_phone.setText(user.mobile);
	}
	
	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.setup_ll_loginoff:
				loginoffDialog = new LoginOffDialog(getActivity(), onComfirmClickListener);
				loginoffDialog.show();
				break;
			case R.id.setup_ll_update:
				Intent updateIntent = new Intent(getActivity(), UpdateActivity.class);
				startActivity(updateIntent);
				break;
			case R.id.setup_ll_aboutme:
				Intent aboutIntent = new Intent(getActivity(), AboutMeActivity.class);
				startActivity(aboutIntent);
				break;
			case R.id.setup_ll_icon:
				Intent intent;
				if (Build.VERSION.SDK_INT < 19) {
					intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
				} else {
					intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				}
				startActivityForResult(intent, 0x1234);
				break;
			case R.id.setup_ll_edit_name:
				Intent editIntent = new Intent(getActivity(), EditActivity.class);
				editIntent.putExtra("edittype", "name");
				startActivity(editIntent);
				break;
			case R.id.setup_ll_edit_phone:
				Intent phoneIntent = new Intent(getActivity(), EditActivity.class);
				phoneIntent.putExtra("edittype", "phone");
				startActivity(phoneIntent);
				break;
			case R.id.setup_ll_edit_email:
				Intent emailIntent = new Intent(getActivity(), EditActivity.class);
				emailIntent.putExtra("edittype", "email");
				startActivity(emailIntent);
				break;
			case R.id.setup_ll_kefu:
				IMChatManager.getInstance().beginSession(new OnSessionBeginListener() {

					@Override
					public void onLeaveMessage() {
						//提交离线留言
						OfflineMessageDialog dialog = new OfflineMessageDialog();
						dialog.show(SetupFragment.this.getActivity().getFragmentManager(), "OfflineMessageDialog");
					}

					@Override
					public void onRobot() {
						Intent chatIntent = new Intent(SetupFragment.this.getActivity(), ChatActivity.class);
						chatIntent.putExtra("isRobot", true);
						startActivity(chatIntent);
					}

					@Override
					public void onPeople() {
						Intent chatIntent = new Intent(SetupFragment.this.getActivity(), ChatActivity.class);
						chatIntent.putExtra("isRobot", false);
						startActivity(chatIntent);
					}

					@Override
					public void onFailed() {
						Toast.makeText(SetupFragment.this.getActivity(), "由于网络原因等会话开始失败", Toast.LENGTH_SHORT).show();
					}
				});
				break;
			}
		}
	};
	
	private android.view.View.OnClickListener onComfirmClickListener = new android.view.View.OnClickListener() {
		
			@Override
			public void onClick(View view) {
				HttpManager.loginOff(connectionId,
						new loginOffResponseHandler());
				loginoffDialog.cancel();
			}
	};
	
	class loginOffResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			if ("true".equals(succeed)) {
//				loginMgr.loginOff();
				try {
					imServiceConnector.getBinder().logoff();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				//注销就清空原来保存的数据
				sp.edit().clear().commit();
				getActivity().getContentResolver().delete(SipProfile.ACCOUNT_URI, "1", null);
				MessageDao.getInstance().deleteAllMsgs();
				NewMessageDao.getInstance().deleteAllMsgs();
				UserDao.getInstance().deleteUser();
				ContactsDao.getInstance().clear();
				
				Intent intent = new Intent(getActivity(), LoginActivity.class);
				startActivity(intent);
				MobileApplication.getInstance().exit();
			} else{
				Toast.makeText(getActivity(), "网络不稳定，请稍后重试", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		imServiceConnector.disconnect(getActivity());
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(UserIconUpdate userIconUpdate) {
		//接收到头像更新的事件
		HttpManager.getUserInfo(sp.getString("connecTionId", ""),
				new GetUserInfoResponseHandler());

	}


	// 获取用户信息
	class GetUserInfoResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
							  String responseString, Throwable throwable) {

		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
							  String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			LogUtil.d("LoginActivity", "获取用户信息返回的数据是:"+responseString);
			if ("true".equals(succeed)) {
				User user = HttpParser.getUserInfo(responseString);
				// 用户信息存入数据库
				UserDao.getInstance().deleteUser();
				UserDao.getInstance().insertUser(user);

				Glide.with(SetupFragment.this).load(user.im_icon+"?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(contact_detail_image);

			}
		}
	}
}
