package com.moor.im.ui.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Department;
import com.moor.im.ui.adapter.MembersSelectAdapter;
import com.moor.im.ui.adapter.SubDepartmentSelectAdapter;
/**
 * 成员选择界面
 * @author LongWei
 *
 */
public class MembersSelectActivity extends Activity{
	
	private ListView mListView;

	private ImageView title_btn_back, title_btn_ok;
	
	private MembersSelectAdapter adapter;
	
	private List<Contacts> members;

	private List<Contacts> tempList = new ArrayList<Contacts>();
	private List<Contacts> intentContacts = new ArrayList<Contacts>();

	private String selectType;
	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_subdepartment_select);
		sp = getSharedPreferences("SP", 4);
		mListView = (ListView) findViewById(R.id.department_select_list);
		tempList.clear();

		Intent intent = getIntent();
		if(intent.getSerializableExtra("membersIdList") != null) {
			intentContacts = (List<Contacts>) intent.getSerializableExtra("membersIdList");
		}

		if(intent.getStringExtra("selectType") != null) {
			selectType = intent.getStringExtra("selectType");
		}
		
		
		//从数据库中查询出所有的联系人
		members = ContactsDao.getInstance().getContacts();
		if("Discussion".equals(selectType)) {
			for (int i = 0; i < members.size(); i++) {
				if (members.get(i)._id.equals(sp.getString("_id", ""))) {
					members.remove(i);
				}
			}
		}

		adapter = new MembersSelectAdapter(MembersSelectActivity.this, members);
		
		for (int i = 0; i < intentContacts.size(); i++) {
			for (int j = 0; j < members.size(); j++) {
				if(intentContacts.get(i)._id.equals(members.get(j)._id)){
					MembersSelectAdapter.getIsSelected().put(j, true);
				}
			}
		}
		
		
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				MembersSelectAdapter.ViewHolder holder = (MembersSelectAdapter.ViewHolder) view.getTag();
				holder.select.toggle();
				if (holder.select.isChecked()) {
					MembersSelectAdapter.getIsSelected().put(position, true);
				} else {
					MembersSelectAdapter.getIsSelected().put(position, false);
				}
			}
		});

		title_btn_back = (ImageView) findViewById(R.id.title_btn_back);
		title_btn_ok = (ImageView) findViewById(R.id.title_btn_ok);

		title_btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		title_btn_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				HashMap<Integer,Boolean> isSelected = MembersSelectAdapter.getIsSelected();
				for (int i = 0; i < isSelected.size(); i++) {
					if(isSelected.get(i)) {
						Contacts cb = members.get(i);
						tempList.add(cb);
					}
				}
				data.putExtra("memberslist", (Serializable) tempList);
				if("groupAdmin".equals(selectType)) {
					setResult(0x221, data);
				}else {
					setResult(0x222, data);
				}

				finish();
			}
		});

	}

}
