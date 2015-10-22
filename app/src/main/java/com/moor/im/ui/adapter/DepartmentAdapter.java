package com.moor.im.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Department;

public class DepartmentAdapter extends BaseAdapter{

	Context context;
	List<Department> departments;
	
	public DepartmentAdapter(Context context, List<Department> departments) {
		this.context = context;
		this.departments = departments;
	}
	
	@Override
	public int getCount() {
		return departments.size();
	}

	@Override
	public Object getItem(int position) {
		return departments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.department_list_item, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.department_item_tv_name);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tv_name.setText(departments.get(position).Name);
		return convertView;
	}

	public static class ViewHolder{
		public TextView tv_name;
	}
	
}
