package com.moor.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.moor.im.R;
import com.moor.im.model.entity.Option;

import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/3/21.
 */
public class ErpCBAdapter extends BaseAdapter{

    private Context context;
    private List<Option> options;
    private HashMap<Integer,Boolean> isSelected = new HashMap<Integer, Boolean>();;
    public ErpCBAdapter(Context context, List<Option> options) {
        this.context = context;
        this.options = options;
        for (int i = 0; i < options.size(); i++) {
            getIsSelected().put(i, false);
        }
    }

    public List<Option> getOptions() {
        return options;
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return options.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.erp_field_checkbox_item, null);
            holder = new ViewHolder();
            holder.cb = (CheckBox) convertView.findViewById(R.id.erp_field_checkbox_item_cb);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.cb.setText(options.get(position).name);
        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }

    public class ViewHolder {
        public CheckBox cb;
    }

    public HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(HashMap<Integer,Boolean> isSelected) {
        this.isSelected = isSelected;
    }
}
