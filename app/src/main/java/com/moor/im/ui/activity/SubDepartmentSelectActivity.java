package com.moor.im.ui.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.Department;
import com.moor.im.model.parser.DepartmentParser;
import com.moor.im.ui.adapter.SubDepartmentSelectAdapter;
/**
 * 子部门选择界面,目前没用
 * @author LongWei
 *
 */
public class SubDepartmentSelectActivity extends Activity{
	
	private ListView mListView;
	
	private SubDepartmentSelectAdapter adapter;
	
	private List<Department> subDepartments;

	private List<Department> tempList = new ArrayList<Department>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subdepartment_select);
		mListView = (ListView) findViewById(R.id.department_select_list);
		
		ActionBar ab = getActionBar();
		ab.setDisplayShowHomeEnabled(false);
		ab.setTitle("选择子部门");
		
//		subDepartments = DepartmentParser.getInstance(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_DEPARTMENT)).getAllSubDepartments();
		
		adapter = new SubDepartmentSelectAdapter(SubDepartmentSelectActivity.this, subDepartments);
		
		mListView.setAdapter(adapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SubDepartmentSelectAdapter.ViewHolder holder = (SubDepartmentSelectAdapter.ViewHolder) view.getTag();
				holder.select.toggle();
				if(holder.select.isChecked()) {
					SubDepartmentSelectAdapter.getIsSelected().put(position, true);
				}
			}
		});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.subdepartmentselect, menu);
		return true;
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item) {  
	    switch (item.getItemId()) {  
	    case R.id.action_done:  
	        //选择完成了，将数据返回给上个页面
	    	
	    	Intent data = new Intent();
			HashMap<Integer,Boolean> isSelected = SubDepartmentSelectAdapter.getIsSelected();
			for (int i = 0; i < isSelected.size(); i++) {
				if(isSelected.get(i)) {
					Department cb = subDepartments.get(i);
					tempList .add(cb);
				}
			}
			data.putExtra("departmentlist", (Serializable)tempList);
			setResult(0x111, data);
			finish();
	        return true;  
	    }  
	    return super.onOptionsItemSelected(item);  
	}  
	
}
