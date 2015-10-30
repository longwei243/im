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
import com.moor.im.utils.NullUtil;

/**
 * 部门增加界面
 * @author LongWei
 *
 */
public class DepartmentAddActivity extends Activity implements OnClickListener{
	
	Button department_add_btn_select_subdepartment, department_add_btn_select_members, department_add_btn_save;
	ProgressBar pb;
	private List<Department> departmentTempList = new ArrayList<Department>();
	private List<Contacts> membersTempList = new ArrayList<Contacts>();
	
	private GridViewInScrollView department_add_gv_members;
	
	private EditText department_add_et_name, department_add_et_desc;

	private ImageView title_btn_back;

	private SharedPreferences sp;
	
	boolean isRoot;
	private Department department;
	private String rootId;
	private List<Contacts> contacts = new ArrayList<Contacts>();
	GVContactAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_department_add);

		title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
		title_btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		contacts.clear();
		Intent intent = getIntent();
		if(intent.getStringExtra("departmentId") == null) {
//			System.out.println("添加根部门");
			isRoot = true;
		}else {
//			System.out.println("添加子部门");
			isRoot = false;
			rootId = intent.getStringExtra("departmentId");
//			System.out.println("传过来的部门id是："+rootId);
			DepartmentParser dp = new DepartmentParser(HttpParser.getDepartments(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)));
			department = dp.getDepartmentById(rootId);
		}
		
		sp = getSharedPreferences("SP", 4);
		department_add_btn_select_members = (Button) findViewById(R.id.department_add_btn_select_members);
		department_add_btn_select_members.setOnClickListener(this);
		department_add_btn_save = (Button) findViewById(R.id.department_add_btn_save);
		department_add_btn_save.setOnClickListener(this);
		
		department_add_gv_members = (GridViewInScrollView) findViewById(R.id.department_add_gv_members);
		
		department_add_et_name = (EditText) findViewById(R.id.department_add_et_name);
		department_add_et_desc = (EditText) findViewById(R.id.department_add_et_desc);
		
		pb = (ProgressBar) findViewById(R.id.department_add_progress);
		

		
	}
	

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.department_add_btn_select_members:
			//添加成员
			Intent memberSelectIntent = new Intent(DepartmentAddActivity.this, MembersSelectActivity.class);
			memberSelectIntent.putExtra("membersIdList", (Serializable)contacts);
			startActivityForResult(memberSelectIntent, 0x222);
			break;
		case R.id.department_add_btn_save:
			//添加部门，请求网络
			String name = department_add_et_name.getText().toString();
			String desc = department_add_et_desc.getText().toString();
			
			ArrayList members = new ArrayList();
			for (int i = 0; i < membersTempList.size(); i++) {
				members.add(membersTempList.get(i)._id);
			}
			ArrayList subDept = new ArrayList();
			for (int i = 0; i < departmentTempList.size(); i++) {
				subDept.add(departmentTempList.get(i)._id);
			}
			if(!"".equals(name)) {
				HttpManager.addDepartment(sp.getString("connecTionId", ""), members, subDept, name, desc, isRoot, new AddDepartmentResponseHandler());
				department_add_btn_save.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
			}else {
				Toast.makeText(DepartmentAddActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
			}
			
			
			break;
		}
	}
	
	class AddDepartmentResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(DepartmentAddActivity.this, "请检查您的网络问题！！！", 3000).show();
			department_add_btn_save.setVisibility(View.VISIBLE);
			pb.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				if(isRoot){
					//若添加根部门成功
					department_add_btn_save.setVisibility(View.VISIBLE);
					pb.setVisibility(View.GONE);
					//通知获取最新部门信息数据，刷新界面
					finish();
					Intent intent = new Intent("department_update");
					sendBroadcast(intent);
				}else {
					//添加子部门成功，先将该部门信息获得
					Department dept = HttpParser.getDepartmentInfo(responseString);
					//更新它的父部门信息
					List<Department> departments = HttpParser.getDepartments((MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)));
					DepartmentParser dp = new DepartmentParser(departments);
					Department rootDept = dp.getDepartmentById(rootId);
					String id = NullUtil.checkNull(rootDept._id);
					String name = NullUtil.checkNull(rootDept.Name);
					String desc = NullUtil.checkNull(rootDept.Description);
					boolean isRoot = rootDept.Root;
					ArrayList members = (ArrayList) rootDept.Members;
					ArrayList subDept = (ArrayList) rootDept.Subdepartments;
					subDept.add(dept._id);
					//发送网络请求，更新根部门信息
					HttpManager.updateDepartment(sp.getString("connecTionId", ""), id, members, subDept, name, desc, isRoot, new UpdateDepartmentResponseHandler());
				}
				
//				System.out.println("添加部门返回的数据是："+responseString);
				
			} else {
				Toast.makeText(DepartmentAddActivity.this, message, Toast.LENGTH_SHORT)
						.show();
				department_add_btn_save.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		}
	}

	
	class UpdateDepartmentResponseHandler extends TextHttpResponseHandler {
		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			Toast.makeText(DepartmentAddActivity.this, "请检查您的网络问题！！！", 3000).show();
			department_add_btn_save.setVisibility(View.VISIBLE);
			pb.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			if ("true".equals(succeed)) {
				department_add_btn_save.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
//				System.out.println("更新部门信息返回结果："+responseString);
				finish();
				//更新成功了，通知获取最新部门信息数据，刷新界面
				Intent intent = new Intent("department_update");
				sendBroadcast(intent);
			} else {
				Toast.makeText(DepartmentAddActivity.this, message, Toast.LENGTH_SHORT)
						.show();
				department_add_btn_save.setVisibility(View.VISIBLE);
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
//			System.out.println("size:"+membersTempList.size());
			adapter = new GVContactAdapter(DepartmentAddActivity.this, membersTempList);
			department_add_gv_members.setAdapter(adapter);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
