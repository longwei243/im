package com.moor.im.ui.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Group;

import java.util.List;

/**
 * 群组adapter
 * Created by long on 2015/7/16.
 */
public class GroupAdapter extends BaseAdapter {

    private Context context;
    private List<Group> groupList;

    public GroupAdapter(Context context, List<Group> groupList) {
        this.context = context;
        this.groupList = groupList;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.group_list_item, null);
            holder.group_item_tv_title = (TextView) convertView.findViewById(R.id.group_item_tv_name);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.group_item_tv_title.setText(groupList.get(position).title);
        return convertView;
    }

    class ViewHolder {
        TextView group_item_tv_title;
    }
}
