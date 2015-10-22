package com.moor.im.ui.activity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.dialog.ConfirmDialog;

public class UserInfoActivity extends Activity{

	EditText user_edit_name, user_edit_email, user_edit_dianhua;
	Button user_btn_save;
	ProgressBar pb;
	private SharedPreferences sp;
	private Editor editor;
	
	private String name;
	private String email;
	private String dianhua;

	private ImageView title_btn_back;
	ConfirmDialog confirmDialog;

	User user = UserDao.getInstance().getUser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_userinfo);
		
		sp = getSharedPreferences("SP", 4);
		editor = sp.edit();

		title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
		title_btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		user_edit_name = (EditText) findViewById(R.id.user_edit_name);
		user_edit_email = (EditText) findViewById(R.id.user_edit_email);
		user_edit_dianhua = (EditText) findViewById(R.id.user_edit_dianhua);
		user_btn_save = (Button) findViewById(R.id.user_btn_save);
		pb = (ProgressBar) findViewById(R.id.edituser_progress);
		
		user_edit_name.setText(user.displayName);
		user_edit_email.setText(user.email);
		user_edit_dianhua.setText(user.mobile);
		
		user_btn_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				name = user_edit_name.getText().toString();
				email = user_edit_email.getText().toString();
				dianhua = user_edit_dianhua.getText().toString();
				if(!"".equals(name)) {

					confirmDialog = new ConfirmDialog(UserInfoActivity.this, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							//发送网络请求，修改信息
							confirmDialog.dismiss();
							HttpManager.editUserInfo(sp.getString("connecTionId", ""),
									user._id, name, email, dianhua,
									user.product, new EditUserResponseHandler());
							pb.setVisibility(View.VISIBLE);
							user_btn_save.setVisibility(View.GONE);
						}
					});
					confirmDialog.show();

				}else {
					Toast.makeText(UserInfoActivity.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}

	
	class EditUserResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(UserInfoActivity.this, "请检查您的网络问题！！！", Toast.LENGTH_SHORT).show();
			pb.setVisibility(View.GONE);
			user_btn_save.setVisibility(View.VISIBLE);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
//				editor.putString("mobile", dianhua);
//				editor.putString("displayName", name);
//				editor.putString("email", email);
//				editor.commit();

				user.mobile = dianhua;
				user.displayName = name;
				user.email = email;

				UserDao.getInstance().updateUser(user);

				pb.setVisibility(View.GONE);
				user_btn_save.setVisibility(View.VISIBLE);
				Toast.makeText(UserInfoActivity.this, "信息修改成功", Toast.LENGTH_SHORT)
				.show();
				finish();
			} else {
				if("408".equals(message)) {
					JSONObject o;
					try {
						o = new JSONObject(responseString);
						JSONArray ja = o.getJSONArray("RepeatList");
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < ja.length(); i++) {
							if("email".equals(ja.get(i))) {
								sb.append("您的邮箱与别人重复");
							}
							if("mobile".equals(ja.get(i))) {
								sb.append("您的电话与别人重复");
							}
							if("displayName".equals(ja.get(i))) {
								sb.append("您的姓名与别人重复");
							}
						}
						Toast.makeText(UserInfoActivity.this, sb.toString(), Toast.LENGTH_SHORT)
						.show();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}else {
					Toast.makeText(UserInfoActivity.this, "网络不稳定，请稍后重试", Toast.LENGTH_SHORT)
					.show();
				}
				
				pb.setVisibility(View.GONE);
				user_btn_save.setVisibility(View.VISIBLE);
			}
		}
	}
}
