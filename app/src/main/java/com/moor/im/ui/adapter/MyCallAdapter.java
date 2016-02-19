package com.moor.im.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.model.entity.Department;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.utils.MobileAssitantCache;
import com.moor.im.utils.NullUtil;

import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class MyCallAdapter extends BaseAdapter{

    private List<MACallLogData> maCallLogs;
    private Context context;

    public MyCallAdapter() {}

    public MyCallAdapter(Context context, List<MACallLogData> maCallLogs) {
        this.context = context;
        this.maCallLogs = maCallLogs;
    }

    @Override
    public int getCount() {
        return maCallLogs.size();
    }

    @Override
    public Object getItem(int position) {
        return maCallLogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.mycall_listview_item, null);
            viewHolder.tv_callNo = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_callNo);
            viewHolder.tv_callNoDesc = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_callNoDesc);
            viewHolder.tv_shortTime = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_shortTime);
            viewHolder.tv_customName = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_customName);
            viewHolder.tv_shortCallTimeLength = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_shortCallTimeLength);
            viewHolder.tv_agentDesc = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_agentDesc);
            viewHolder.tv_queueDesc = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_queueDesc);
            viewHolder.tv_statusDesc = (TextView) convertView.findViewById(R.id.mycall_listview_item_tv_statusDesc);
            viewHolder.iv_dialTypeDesc = (ImageView) convertView.findViewById(R.id.mycall_listview_item_iv_dialTypeDesc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MACallLogData maCallLog = maCallLogs.get(position);

        viewHolder.tv_callNo.setText(NullUtil.checkNull(maCallLog.callNo));

        viewHolder.tv_callNoDesc.setText(NullUtil.checkNull(maCallLog.city));

        viewHolder.tv_shortTime.setText(NullUtil.checkNull(maCallLog.shortTime));

        viewHolder.tv_customName.setText(NullUtil.checkNull(maCallLog.customName));

        if("0秒".equals(NullUtil.checkNull(maCallLog.shortCallTimeLength))) {
            viewHolder.tv_shortCallTimeLength.setVisibility(View.GONE);
        }else {
            viewHolder.tv_shortCallTimeLength.setText(NullUtil.checkNull(maCallLog.shortCallTimeLength));
        }
        viewHolder.tv_agentDesc.setText(NullUtil.checkNull(maCallLog.agent));
        viewHolder.tv_queueDesc.setText(NullUtil.checkNull(maCallLog.queue));

        viewHolder.tv_statusDesc.setText(NullUtil.checkNull(maCallLog.status));
        if("success".equals(maCallLog.statusClass)) {
            viewHolder.tv_statusDesc.setBackgroundColor(context.getResources().getColor(R.color.call_green));
        }else {
            viewHolder.tv_statusDesc.setBackgroundColor(context.getResources().getColor(R.color.call_red));
        }

        if("outbound".equals(maCallLog.dialType)) {
            viewHolder.iv_dialTypeDesc.setBackgroundResource(R.drawable.outbound);
        }else if("inbound".equals(maCallLog.dialType)){
            viewHolder.iv_dialTypeDesc.setBackgroundResource(R.drawable.inbound);
        }

        return convertView;
    }

    final static class ViewHolder{
        TextView tv_callNo;
        TextView tv_callNoDesc;
        TextView tv_shortTime;
        TextView tv_customName;
        TextView tv_shortCallTimeLength;
        ImageView iv_dialTypeDesc;
        TextView tv_agentDesc;
        TextView tv_queueDesc;
        TextView tv_statusDesc;
    }
}
