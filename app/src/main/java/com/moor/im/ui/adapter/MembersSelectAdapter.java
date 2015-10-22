package com.moor.im.ui.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moor.im.R;
import com.moor.im.model.entity.Contacts;
import com.moor.im.ui.view.RoundImageView;

public class MembersSelectAdapter extends BaseAdapter{

	List<Contacts> members;
	Context context;
	
	private static HashMap<Integer,Boolean> isSelected = new HashMap<Integer, Boolean>();;
	
	public MembersSelectAdapter(Context context, List<Contacts> members) {
		this.context = context;
		this.members = members;
		
		for (int i = 0; i < members.size(); i++) {
			getIsSelected().put(i, false);
		}
	}
	
	@Override
	public int getCount() {
		return members.size();
	}

	@Override
	public Object getItem(int position) {
		return members.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.department_select_listview_item, null);
			
			holder.name = (TextView) convertView.findViewById(R.id.department_select_item_tv_name);
			holder.desc = (TextView) convertView.findViewById(R.id.department_select_item_tv_desc);
			holder.select = (CheckBox) convertView.findViewById(R.id.department_select_item_cb);
			holder.user_icon = (ImageView) convertView.findViewById(R.id.user_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String imicon = members.get(position).im_icon;
		if(!"".equals(imicon)) {
			Glide.with(context).load(imicon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.user_icon);
		}else {
			Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.user_icon);
		}

		holder.name.setText(members.get(position).displayName);
		holder.desc.setText(members.get(position).mobile);
		
		holder.select.setChecked(getIsSelected().get(position));

		return convertView;
	}

	public class ViewHolder {
		TextView name;
		TextView desc;
		ImageView user_icon;
		public CheckBox select;
	}
	
	public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
    	MembersSelectAdapter.isSelected = isSelected;
    }
}
