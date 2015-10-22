package com.moor.im.ui.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Department;

public class SubDepartmentSelectAdapter extends BaseAdapter{

	List<Department> subDepartments;
	Context context;
	
	private static HashMap<Integer,Boolean> isSelected;
	
	public SubDepartmentSelectAdapter(Context context, List<Department> subDepartments) {
		this.context = context;
		this.subDepartments = subDepartments;
		isSelected = new HashMap<Integer, Boolean>();
		
		for (int i = 0; i < subDepartments.size(); i++) {
			getIsSelected().put(i, false);
		}
	}
	
	@Override
	public int getCount() {
		return subDepartments.size();
	}

	@Override
	public Object getItem(int position) {
		return subDepartments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.department_select_listview_item, null);
			holder = new ViewHolder();
			
			holder.name = (TextView) convertView.findViewById(R.id.department_select_item_tv_name);
			holder.desc = (TextView) convertView.findViewById(R.id.department_select_item_tv_desc);
			holder.select = (CheckBox) convertView.findViewById(R.id.department_select_item_cb);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.name.setText(subDepartments.get(position).Name);
		holder.desc.setText(subDepartments.get(position).Description);
		
		holder.select.setChecked(getIsSelected().get(position));

		return convertView;
	}

	public static class ViewHolder {
		TextView name;
		TextView desc;
		public CheckBox select;
	}
	
	public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
    	SubDepartmentSelectAdapter.isSelected = isSelected;
    }
}
