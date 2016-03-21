package com.moor.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.QueryData;

import java.util.List;

/**
 * Created by longwei on 2016/2/22.
 */
public class ErpSpAdapter extends BaseAdapter{

    private Context context;

    private List<Option> datas;

    public ErpSpAdapter(Context context, List<Option> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.spinner_simple_layout,
                parent, false);
        TextView tv_name = (TextView) convertView.findViewById(R.id.spinner_simple_label);
        tv_name.setText(datas.get(position).name);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.spinner_item_label);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_name.setText(datas.get(position).name);
        return convertView;
    }

    static class ViewHolder {
        TextView tv_name;
    }
}
