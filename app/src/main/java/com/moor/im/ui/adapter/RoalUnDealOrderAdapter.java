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
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.event.HaveOrderEvent;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MABusiness;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.utils.CacheUtils;
import com.moor.im.utils.NullUtil;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by longwei on 2016/2/29.
 */
public class RoalUnDealOrderAdapter extends BaseAdapter{

    private List<MABusiness> maBusinesses;
    private Context context;
    private String userId;

    public RoalUnDealOrderAdapter() {}

    public RoalUnDealOrderAdapter(Context context, List<MABusiness> maBusinesses, String userId) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.erp_dlq_list_item, null);
            viewHolder.tv_lq = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_lq);
            viewHolder.tv_customername = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_customername);
            viewHolder.tv_shorttime = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_shorttime);
            viewHolder.tv_flow = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_flow);
            viewHolder.tv_step = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_step);
            viewHolder.tv_createuser = (TextView) convertView.findViewById(R.id.erp_dlq_item_tv_createuser);
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

        viewHolder.tv_lq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> datas = new HashMap<>();
                datas.put("master", userId);
                datas.put("_id", maBusiness._id);
                MobileHttpManager.haveThisOrder(userId, datas, new HaveThisOrderResponseHandler(position));

            }
        });
        return convertView;
    }

    final static class ViewHolder{
        TextView tv_lq;
        TextView tv_customername;
        TextView tv_shorttime;
        TextView tv_flow;
        TextView tv_step;
        TextView tv_createuser;
    }

    class HaveThisOrderResponseHandler extends TextHttpResponseHandler {

        private int position;

        public HaveThisOrderResponseHandler() {

        }
        public HaveThisOrderResponseHandler(int postion) {
            this.position = postion;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {

            String succeed = HttpParser.getSucceed(responseString);
            String msg = HttpParser.getMessage(responseString);
            if(maBusinesses.size() > 0) {
                if ("true".equals(succeed)) {
                    maBusinesses.remove(position);
                    notifyDataSetChanged();
                    EventBus.getDefault().post(new HaveOrderEvent(1));
                    Toast.makeText(context, "领取成功", Toast.LENGTH_SHORT).show();
                }else if("此业务已被其他人领取。".equals(msg)){
                    maBusinesses.remove(position);
                    notifyDataSetChanged();
                    EventBus.getDefault().post(new HaveOrderEvent(0));
                    Toast.makeText(context, "此业务已被其他人领取", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "领取失败", Toast.LENGTH_SHORT).show();
                }
            }


        }
    }
}
