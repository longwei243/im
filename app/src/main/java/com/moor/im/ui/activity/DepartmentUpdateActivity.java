package com.moor.im.ui.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.http.HttpManager;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Department;
import com.moor.im.model.parser.DepartmentParser;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.GVContactAdapter;
import com.moor.im.ui.adapter.MembersSelectAdapter;
import com.moor.im.ui.view.GridViewInScrollView;
import com.moor.im.utils.DepartmentActivityUtil;

public class DepartmentUpdateActivity extends Activity implements OnClickListener{
	Button department_update_btn_select_subdepartment, department_update_btn_select_members, department_update_btn_save;
	ProgressBar pb;
	private List<Contacts> membersTempList = new ArrayList<Contacts>();
	
	private GridViewInScrollView department_update_gv_members;
	
	private EditText department_update_et_name, department_update_et_desc;
	
	private SharedPreferences sp;
		
	private String departmentId;

	private ImageView title_btn_back;
	
	private GVContactAdapter adapter;
	private List<Contacts> contacts = new ArrayList<Contacts>();
	private Department department;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DepartmentActivityUtil.getInstance().add(this);
		setContentView(R.layout.activity_department_update);

		title_btn_back = (ImageView) findViewById(R.id.title_btn_back);

		title_btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		contacts.clear();
		Intent intent = getIntent();
		departmentId = intent.getStringExtra("departmentId");
		
		DepartmentParser dp = new DepartmentParser(HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)));
		department = dp.getDepartmentById(departmentId);
		
		sp = getSharedPreferences("SP", 4);
		department_update_btn_select_members = (Button) findViewById(R.id.department_update_btn_select_members);
		department_update_btn_select_members.setOnClickListener(this);
		department_update_btn_save = (Button) findViewById(R.id.department_update_btn_save);
		department_update_btn_save.setOnClickListener(this);
		
		department_update_gv_members = (GridViewInScrollView) findViewById(R.id.department_update_gv_members);
		
		department_update_et_name = (EditText) findViewById(R.id.department_update_et_name);
		department_update_et_desc = (EditText) findViewById(R.id.department_update_et_desc);
		
		pb = (ProgressBar) findViewById(R.id.department_update_progress);
		
		department_update_et_name.setText(department.Name);
		department_update_et_desc.setText(department.Description);
		
		List<String> membersId = new ArrayList<String>();
		membersId = department.Members;
		contacts = new ArrayList<Contacts>();
		if(membersId.size() != 0) {
			for (int i = 0; i < membersId.size(); i++) {
				Contacts contact = ContactsDao.getInstance().getContactById(membersId.get(i));
				contacts.add(contact);
			}
		}
		
		adapter = new GVContactAdapter(DepartmentUpdateActivity.this, contacts);
		department_update_gv_members.setAdapter(adapter);

	}

	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.department_update_btn_select_members:
			//添加成员
			Intent memberSelectIntent = new Intent(DepartmentUpdateActivity.this, MembersSelectActivity.class);
			memberSelectIntent.putExtra("membersIdList", (Serializable)contacts);
			startActivityForResult(memberSelectIntent, 0x222);
			break;
		case R.id.department_update_btn_save:
			ArrayList members = new ArrayList();
			if(membersTempList.size() != 0) {
				for (int i = 0; i < membersTempList.size(); i++) {
					members.add(membersTempList.get(i)._id);
				}
			}else {
				for (int i = 0; i < contacts.size(); i++) {
					members.add(contacts.get(i)._id);
				}
			}
			String name = department_update_et_name.getText().toString().trim();
			String desc = department_update_et_desc.getText().toString().trim();
						
			if(!"".equals(name)) {
				HttpManager.updateDepartment(sp.getString("connecTionId", ""), departmentId, members, (ArrayList)department.Subdepartments, name, desc, department.Root, new UpdateDepartmentResponseHandler());
			}
			
			break;
		}
	}
	
	class UpdateDepartmentResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(DepartmentUpdateActivity.this, "请检查您的网络问题！！！", 3000).show();
			department_update_btn_save.setVisibility(View.VISIBLE);
			pb.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				department_update_btn_save.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
				System.out.println("更新部门信息返回结果："+responseString);
				Toast.makeText(DepartmentUpdateActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
				//更新成功了，通知获取最新部门信息数据，刷新界面
				Intent intent = new Intent("department_update");
				sendBroadcast(intent);
				
				DepartmentActivityUtil.getInstance().exit();
				
				Intent it = new Intent(DepartmentUpdateActivity.this, DepartmentActivity.class);
				startActivity(it);
			} else {
//				Toast.makeText(DepartmentUpdateActivity.this, message, Toast.LENGTH_SHORT)
//						.show();
				department_update_btn_save.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == 0x222 && resultCode == 0x222) {
			contacts.clear();
//			System.out.println("选择成员返回结果");
			membersTempList = (List<Contacts>) data.getSerializableExtra("memberslist");
			contacts = membersTempList;
			System.out.println("size:"+membersTempList.size());
			adapter = new GVContactAdapter(DepartmentUpdateActivity.this, membersTempList);
			department_update_gv_members.setAdapter(adapter);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
