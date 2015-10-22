package com.moor.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Discussion;
import com.moor.im.model.entity.Group;

import java.util.List;

/**
 * Created by long on 2015/7/22.
 */
public class DiscussionAdapter extends BaseAdapter{

    private Context context;
    private List<Discussion> discussionList;

    public DiscussionAdapter(Context context, List<Discussion> discussionList) {
        this.context = context;
        this.discussionList = discussionList;
    }

    @Override
    public int getCount() {
        return discussionList.size();
    }

    @Override
    public Object getItem(int position) {
        return discussionList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.discussion_list_item, null);
            holder.discussion_item_tv_title = (TextView) convertView.findViewById(R.id.discuss_item_tv_name);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.discussion_item_tv_title.setText(discussionList.get(position).title);
        return convertView;
    }

    class ViewHolder {
        TextView discussion_item_tv_title;
    }
}
