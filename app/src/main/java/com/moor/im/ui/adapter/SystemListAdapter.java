package com.moor.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.FromToMessage;
import com.moor.im.utils.TimeUtil;

import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class SystemListAdapter extends BaseAdapter{

    private Context context;
    private List<FromToMessage> messages;

    public SystemListAdapter(Context context, List<FromToMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.system_list_item, null);
            holder.system_list_item_tv_content = (TextView) convertView.findViewById(R.id.system_list_item_tv_content);
            holder.system_list_item_tv_time = (TextView) convertView.findViewById(R.id.system_list_item_tv_time);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.system_list_item_tv_content.setText(messages.get(position).message);
        holder.system_list_item_tv_time.setText(TimeUtil.convertTimeToFriendly(messages.get(position).when));

        return convertView;
    }

    class ViewHolder {
        TextView system_list_item_tv_content, system_list_item_tv_time;
    }
}
