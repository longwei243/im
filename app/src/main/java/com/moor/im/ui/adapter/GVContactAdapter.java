package com.moor.im.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Contacts;
import com.moor.im.model.entity.Department;

public class GVContactAdapter extends BaseAdapter{

	List<Contacts> contacts;
	Context context;
	
	public GVContactAdapter(Context context, List<Contacts> contacts) {
		this.contacts = contacts;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Object getItem(int position) {
		return contacts.get(position);
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
			convertView = LayoutInflater.from(context).inflate(R.layout.gv_department, null);
			holder.tv = (TextView) convertView.findViewById(R.id.gv_department_tv_name);
			
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tv.setText(contacts.get(position).displayName);
		return convertView;
	}
	
	public static class ViewHolder{
		TextView tv;
	}

}
