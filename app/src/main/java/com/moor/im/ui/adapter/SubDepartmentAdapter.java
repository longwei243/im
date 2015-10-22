package com.moor.im.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.db.dao.ContactsDao;
import com.moor.im.model.entity.DeptAndMember;
import com.moor.im.ui.view.RoundImageView;

public class SubDepartmentAdapter extends BaseAdapter{

	private Context context;
	private List<DeptAndMember> deptAndMembers;
	
	public SubDepartmentAdapter(Context context, List<DeptAndMember> deptAndMembers) {
		this.context = context;
		this.deptAndMembers = deptAndMembers;
	}
	
	@Override
	public int getCount() {
		return deptAndMembers.size();
	}

	@Override
	public Object getItem(int position) {
		return deptAndMembers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.subdepartment_list_item, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.subdeparment_item_tv_name);
			holder.subdepartment_tv_catalog = (TextView) convertView.findViewById(R.id.subdepartment_tv_catalog);
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.subdepartment_iv_icon_group);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		System.out.println("deptAndMembers.get(position).getName():"+deptAndMembers.get(position).getName());
		holder.tv_name.setText(deptAndMembers.get(position).getName());
		if(position == getDepartmentFirstPosition()) {
			holder.subdepartment_tv_catalog.setVisibility(View.VISIBLE);
			holder.subdepartment_tv_catalog.setText("部门");
		}
		if(position == getMemberFirstPosition()) {
			holder.subdepartment_tv_catalog.setVisibility(View.VISIBLE);
			holder.subdepartment_tv_catalog.setText("成员");
		}
		if(deptAndMembers.get(position).getType()=="member") {

			String im_icon = ContactsDao.getInstance().getContactsIcon(deptAndMembers.get(position).getId());
			if(!"".equals(im_icon) && im_icon != null) {
				Glide.with(context).load(im_icon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.iv_icon);
			}else {
				Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.iv_icon);
			}

		}else {
			holder.iv_icon.setBackgroundResource(R.drawable.ic_group_grey600_48dp);
		}
		return convertView;
	}

	private int getDepartmentFirstPosition() {
		for (int i = 0; i < deptAndMembers.size(); i++) {
			if(deptAndMembers.get(i).getType() == "dept") {
				return i;
			}
		}
		return -1;
	}
	private int getMemberFirstPosition() {
		for (int i = 0; i < deptAndMembers.size(); i++) {
			if(deptAndMembers.get(i).getType() == "member") {
				return i;
			}
		}
		return -1;
	}
	
	
	static class ViewHolder{
		TextView tv_name;
		TextView subdepartment_tv_catalog;
		ImageView iv_icon;
	}
}
