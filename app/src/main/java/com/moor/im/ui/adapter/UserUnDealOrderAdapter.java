package com.moor.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MABusiness;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.utils.NullUtil;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/29.
 */
public class UserUnDealOrderAdapter extends BaseAdapter{

    private List<MABusiness> maBusinesses;
    private Context context;
    private String userId;

    public UserUnDealOrderAdapter() {}

    public UserUnDealOrderAdapter(Context context, List<MABusiness> maBusinesses, String userId) {
        this.context = context;
        this.maBusinesses = maBusinesses;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return maBusinesses.size();
    }

    @Override
    public Object getItem(int position) {
        return maBusinesses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.erp_dcl_list_item, null);
            viewHolder.tv_customername = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_customername);
            viewHolder.tv_shorttime = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_shorttime);
            viewHolder.tv_flow = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_flow);
            viewHolder.tv_step = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_step);
            viewHolder.tv_createuser = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_createuser);
            viewHolder.tv_master = (TextView) convertView.findViewById(R.id.erp_dcl_item_tv_master);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MABusiness maBusiness = maBusinesses.get(position);
        viewHolder.tv_customername.setText(NullUtil.checkNull(maBusiness.name));
        viewHolder.tv_shorttime.setText(NullUtil.checkNull(maBusiness.lastUpdateTime));
        viewHolder.tv_flow.setText(NullUtil.checkNull(maBusiness.flow));
        viewHolder.tv_step.setText(NullUtil.checkNull(maBusiness.step));
        viewHolder.tv_createuser.setText(NullUtil.checkNull(maBusiness.createUser));
        viewHolder.tv_master.setText(NullUtil.checkNull(maBusiness.master));
        return convertView;
    }

    final static class ViewHolder{
        TextView tv_customername;
        TextView tv_shorttime;
        TextView tv_flow;
        TextView tv_step;
        TextView tv_createuser;
        TextView tv_master;
    }

}
