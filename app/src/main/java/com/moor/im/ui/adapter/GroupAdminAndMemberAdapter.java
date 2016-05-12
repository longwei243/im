package com.moor.im.ui.adapter;

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
import com.moor.im.model.entity.GroupAdminAndMembers;
import com.moor.im.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by long on 2015/7/21.
 */
public class GroupAdminAndMemberAdapter extends BaseAdapter{

    private Context context;
    private List<GroupAdminAndMembers> groupAdminAndMembersList;

    public GroupAdminAndMemberAdapter(Context context, List<GroupAdminAndMembers> groupAdminAndMembersList) {
        this.context = context;
        this.groupAdminAndMembersList = groupAdminAndMembersList;
    }

    @Override
    public int getCount() {
        return groupAdminAndMembersList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupAdminAndMembersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.group_setting_list_item, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.group_setting_list_item_item_tv_name);
            holder.group_setting_list_item_tv_catalog = (TextView) convertView.findViewById(R.id.group_setting_list_item_tv_catalog);
            holder.group_setting_list_item_iv_icon_group = (ImageView) convertView.findViewById(R.id.group_setting_list_item_iv_icon_group);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imicon = groupAdminAndMembersList.get(position).getImicon();
        if(!"".equals(imicon)) {
            Glide.with(context).load(imicon + "?imageView2/0/w/100/h/100").asBitmap().placeholder(R.drawable.head_default_local).into(holder.group_setting_list_item_iv_icon_group);
        }else {
            Glide.with(context).load(R.drawable.head_default_local).asBitmap().into(holder.group_setting_list_item_iv_icon_group);
        }

        holder.tv_name.setText(groupAdminAndMembersList.get(position).getName());
        if(position == getDepartmentFirstPosition()) {
            holder.group_setting_list_item_tv_catalog.setVisibility(View.VISIBLE);
            holder.group_setting_list_item_tv_catalog.setText("管理员");
        }else {
            holder.group_setting_list_item_tv_catalog.setVisibility(View.GONE);
        }
        if(position == getMemberFirstPosition()) {
            holder.group_setting_list_item_tv_catalog.setVisibility(View.VISIBLE);
            holder.group_setting_list_item_tv_catalog.setText("成员");
        }

        return convertView;
    }

    private int getDepartmentFirstPosition() {
        for (int i = 0; i < groupAdminAndMembersList.size(); i++) {
            if(groupAdminAndMembersList.get(i).getType().equals("Admin")) {
                return i;
            }
        }
        return -1;
    }
    private int getMemberFirstPosition() {
        for (int i = 0; i < groupAdminAndMembersList.size(); i++) {
            if(groupAdminAndMembersList.get(i).getType().equals("Member")) {
                return i;
            }
        }
        return -1;
    }


    static class ViewHolder{
        TextView tv_name;
        TextView group_setting_list_item_tv_catalog;
        ImageView group_setting_list_item_iv_icon_group;
    }
}
