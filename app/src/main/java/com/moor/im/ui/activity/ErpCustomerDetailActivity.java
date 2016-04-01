package com.moor.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipProfile;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.User;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.utils.MobileAssitantCache;
import com.moor.im.utils.Utils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by longwei on 2016/3/31.
 */
public class ErpCustomerDetailActivity extends Activity{

    private User user = UserDao.getInstance().getUser();

    private TextView erp_customer_tv_name, erp_customer_tv_source, erp_customer_tv_status,
            erp_customer_tv_owner, erp_customer_tv_batchNo, erp_customer_tv_createTime,
            erp_customer_tv_lastUpdateTime;
    private LinearLayout erp_customer_ll_fields;
    private ScrollView erp_customer_sv;
    private String customerId;

    private String custCacheStr;
    private LoadingFragmentDialog loadingFragmentDialog;

    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_erp_customer_detail);

        customerId = getIntent().getStringExtra("customerId");
        loadingFragmentDialog = new LoadingFragmentDialog();
        erp_customer_tv_name = (TextView) findViewById(R.id.erp_customer_tv_name);
        erp_customer_tv_source = (TextView) findViewById(R.id.erp_customer_tv_source);
        erp_customer_tv_status = (TextView) findViewById(R.id.erp_customer_tv_status);
        erp_customer_tv_owner = (TextView) findViewById(R.id.erp_customer_tv_owner);
        erp_customer_tv_batchNo = (TextView) findViewById(R.id.erp_customer_tv_batchNo);
        erp_customer_tv_createTime = (TextView) findViewById(R.id.erp_customer_tv_createTime);
        erp_customer_tv_lastUpdateTime = (TextView) findViewById(R.id.erp_customer_tv_lastUpdateTime);

        erp_customer_ll_fields = (LinearLayout) findViewById(R.id.erp_customer_ll_fields);
        erp_customer_sv = (ScrollView) findViewById(R.id.erp_customer_sv);

        if (MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust) == null) {
            loadingFragmentDialog.show(getFragmentManager(), "");
            MobileHttpManager.getCustCache(user._id, new GetCustCacheResponseHandler());
        }else {
            loadingFragmentDialog.show(getFragmentManager(), "");
            MobileHttpManager.getCustomerDetails(user._id, customerId, new GetCustomerHandler());
        }

        bindService(new Intent().setComponent(new ComponentName("com.moor.im", "com.csipsimple.service.SipService"))
                , connection,
                Context.BIND_AUTO_CREATE);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class GetCustCacheResponseHandler extends TextHttpResponseHandler{

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            System.out.println("客户缓存数据："+s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                if("true".equals(jsonObject.getString("success"))) {
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MACust, s);
                    MobileHttpManager.getCustomerDetails(user._id, customerId, new GetCustomerHandler());
                }
            }catch (JSONException e) {

            }
        }
    }



    class GetCustomerHandler extends TextHttpResponseHandler{

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            System.out.println("客户详细信息数据："+s);
            try {
                JSONObject jsonObject1 = new JSONObject(s);
                if("true".equals(jsonObject1.getString("Succeed"))) {
                    JSONObject jsonObject = jsonObject1.getJSONObject("data");
                    String name = jsonObject.getString("name");
                    erp_customer_tv_name.setText(name);

                    String createTime = jsonObject.getString("createTime");
                    erp_customer_tv_createTime.setText(createTime);

                    String lastUpdateTime = jsonObject.getString("lastUpdateTime");
                    erp_customer_tv_lastUpdateTime.setText(lastUpdateTime);

                    String batchNo = jsonObject.getString("batchNo");
                    erp_customer_tv_batchNo.setText(batchNo);

                    String agentId = jsonObject.getString("owner");
                    MAAgent agent = MobileAssitantCache.getInstance().getAgentById(agentId);
                    erp_customer_tv_owner.setText(agent.displayName);


                    if(MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust) != null) {
                        custCacheStr = MobileApplication.cacheUtil.getAsString(CacheKey.CACHE_MACust);


                        String id = jsonObject.getString("dbType");
                        String statusStr = jsonObject.getString("status");
                        initStatus(custCacheStr, statusStr, id);
                        String sourceStr = jsonObject.getString("custsource1");
                        initSource(custCacheStr, sourceStr, id);

                        //固定字段
                        initStableFields(custCacheStr, id, jsonObject);

                        //自定义字段
                        initFields(custCacheStr, id, jsonObject);
                        loadingFragmentDialog.dismiss();
                        erp_customer_sv.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 自定义字段
     * @param custCacheStr
     * @param id
     * @param jsonObject
     */
    private void initFields(String custCacheStr, String id, JSONObject jsonObject) {
        try {
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i=0; i<fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                String t = field.getString("t");
                String k = field.getString("k");
                String name = field.getString("n");

                if("province".equals(k) || "city".equals(k) || "address".equals(k)) {
                    continue;
                }

                if("dropdown".equals(t) || "radio".equals(t) || "checkbox".equals(t)) {
                    JSONObject cacheJb = new JSONObject(custCacheStr);
                    JSONArray jsonArray = cacheJb.getJSONArray("data");

                    StringBuffer result = new StringBuffer();

                    for (int c=0; c<jsonArray.length(); c++) {
                        JSONObject cust = jsonArray.getJSONObject(c);
                        if (id.equals(cust.getString("_id"))) {
                            JSONArray custom_fields = cust.getJSONArray("custom_fields");
                            for(int m=custom_fields.length()-1; m>=0; m--) {
                                JSONObject cf = custom_fields.getJSONObject(m);
                                if(cf.getString("_id").equals(k)) {
                                    JSONObject choices = cf.getJSONObject("choices");
                                    Iterator<String> iterator = choices.keys();
                                    while (iterator.hasNext()) {
                                        String key = iterator.next();
                                        if("checkbox".equals(t)) {
                                            JSONArray v = field.getJSONArray("v");
                                            for(int p=0; p<v.length(); p++) {
                                                if(key.equals(v.getString(p))) {
                                                    result.append(choices.getString(key) + " ");
                                                }
                                            }
                                        }else {
                                            String v = field.getString("v");
                                            if(key.equals(v)) {
                                                result.append(choices.getString(key) + " ");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(result.toString());
                    erp_customer_ll_fields.addView(rl);
                }else if("sex".equals(k)) {
                    String v = field.getString("v");
                    String value = "";
                    if("0".equals(v)) {
                        value = "男";
                    }else if("1".equals(v)) {
                        value = "女";
                    }
                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(value);
                    erp_customer_ll_fields.addView(rl);

                }else {
                    String v = field.getString("v");
                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item, null);
                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                    erp_customer_field_tv_name.setText(name);
                    erp_customer_field_tv_value.setText(v);
                    erp_customer_ll_fields.addView(rl);
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 固定字段
     * @param custCacheStr
     * @param id
     */
    private void initStableFields(String custCacheStr, String id, JSONObject jb) {

        try {
            JSONObject jsonObject = new JSONObject(custCacheStr);
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject cust = jsonArray.getJSONObject(i);
                if(id.equals(cust.getString("_id"))) {
                    JSONArray stable_fields = cust.getJSONArray("stable_fields");
                    for(int j=0; j<stable_fields.length(); j++) {
                        JSONObject sf = stable_fields.getJSONObject(j);
                        if("name".equals(sf.getString("name"))) {
                            continue;
                        }
                        if("phone".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("phone") != null) {
                                JSONArray phoneArray = jb.getJSONArray("phone");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    final String tel = phone.getString("tel");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item_phone, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);
                                    ImageView call = (ImageView) rl.findViewById(R.id.erp_customer_field_iv_call);
                                    call.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            callDialog(tel);
                                        }
                                    });
                                    erp_customer_ll_fields.addView(rl);
                                }
                            }

                        }else if("email".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("email") != null) {
                                JSONArray phoneArray = jb.getJSONArray("email");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    String tel = phone.getString("email");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);
                                    erp_customer_ll_fields.addView(rl);
                                }
                            }

                        }else if("weixin".equals(sf.getString("name"))) {
                            if(jb.getJSONArray("weixin") != null) {
                                JSONArray phoneArray = jb.getJSONArray("weixin");
                                for(int p=0; p<phoneArray.length(); p++) {
                                    JSONObject phone = phoneArray.getJSONObject(p);
                                    String name = sf.getString("value");
                                    String tel = phone.getString("num");
                                    LinearLayout rl = (LinearLayout) LayoutInflater.from(ErpCustomerDetailActivity.this).inflate(R.layout.erp_customer_field_item, null);
                                    TextView erp_customer_field_tv_name = (TextView) rl.findViewById(R.id.erp_customer_field_tv_name);
                                    TextView erp_customer_field_tv_value = (TextView) rl.findViewById(R.id.erp_customer_field_tv_value);
                                    erp_customer_field_tv_name.setText(name);
                                    erp_customer_field_tv_value.setText(tel);
                                    erp_customer_ll_fields.addView(rl);
                                }
                            }
                        }

                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 状态
     * @param custCacheStr
     * @param statusStr
     */
    private void initStatus(String custCacheStr, String statusStr, String id) {
        try {
            JSONObject jsonObject = new JSONObject(custCacheStr);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject cust = jsonArray.getJSONObject(i);
                if(id.equals(cust.getString("_id"))) {
                    JSONObject status = cust.getJSONObject("status");
                    erp_customer_tv_status.setText(status.getString(statusStr));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 来源
     * @param custCacheStr
     * @param sourceStr
     */
    private void initSource(String custCacheStr, String sourceStr, String id) {
        try {
            JSONObject jsonObject = new JSONObject(custCacheStr);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject cust = jsonArray.getJSONObject(i);
                if(id.equals(cust.getString("_id"))) {
                    JSONArray source = cust.getJSONArray("source");
                    for (int j=0; j<source.length(); j++) {
                        JSONObject jb = source.getJSONObject(j);
                        if(sourceStr.equals(jb.getString("key"))) {
                            String value = jb.getString("name");
                            erp_customer_tv_source.setText(value);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void callDialog(final String number) {
        LayoutInflater myInflater = LayoutInflater.from(ErpCustomerDetailActivity.this);
        final View myDialogView = myInflater.inflate(R.layout.calling_dialog,
                null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ErpCustomerDetailActivity.this)
                .setView(myDialogView);
        final AlertDialog alert = dialog.show();
        alert.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
        alert.getWindow().setGravity(Gravity.BOTTOM);

        // 直播
        LinearLayout mDirectSeeding = (LinearLayout) myDialogView
                .findViewById(R.id.direct_seeding_linear);
        mDirectSeeding.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    if (Utils.isNetWorkConnected(ErpCustomerDetailActivity.this)) {
                        makeCall(number);
                    } else {
                        Toast.makeText(ErpCustomerDetailActivity.this, "网络错误，请重试！",
                                Toast.LENGTH_LONG).show();
                    }
                    alert.dismiss();
//					editText_phone_number.setText("");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        // 回拨
        LinearLayout mCallReturn = (LinearLayout) myDialogView
                .findViewById(R.id.call_return_linear);
        mCallReturn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String mobile = user.mobile;
                if(mobile == null || "".equals(mobile)) {
                    Toast.makeText(ErpCustomerDetailActivity.this, "未绑定手机，不能进行回拨", Toast.LENGTH_SHORT).show();

                }else {
                    // TODO Auto-generated method stub
                    if (Utils.isNetWorkConnected(ErpCustomerDetailActivity.this)) {
                        // 跳转到正在通话页面
                        Intent calling = new Intent(ErpCustomerDetailActivity.this,
                                CallingActivity.class);
                        calling.putExtra("phone_number", number);
                        startActivity(calling);
                    } else {
                        Toast.makeText(ErpCustomerDetailActivity.this, "网络错误，请重试！",
                                Toast.LENGTH_LONG).show();
                    }
                    alert.dismiss();
                }

            }
        });

        // 普通电话
        LinearLayout mOrdinaryCall = (LinearLayout) myDialogView
                .findViewById(R.id.ordinary_call_linear);
        mOrdinaryCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_CALL, Uri
                        .parse("tel://"
                                + number));
                startActivity(intent);
                alert.dismiss();
            }
        });
        // 取消
        LinearLayout mCancelLinear = (LinearLayout) myDialogView
                .findViewById(R.id.cancel_linear);
        mCancelLinear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                alert.dismiss();
            }
        });
    }

    /**
     * 拨打直拨电话
     * @param callee
     */
    public void makeCall(String callee) {
        //TODO 获取id
        Long id = -1L;
        Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, null, null, null, null);
        if(c != null) {
            while(c.moveToNext()) {
                id = c.getLong(c.getColumnIndex("id"));
            }
        }
        try {
            service.makeCall(callee, id.intValue());
        } catch (RemoteException e) {
            Toast.makeText(ErpCustomerDetailActivity.this, "拨打电话失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(connection);
    }

}
