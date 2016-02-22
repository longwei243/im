package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAQueue;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.QueryData;
import com.moor.im.ui.adapter.SPAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by longwei on 2016/2/19.
 */
public class MYCallHighQueryActivity extends Activity implements View.OnClickListener{

    private EditText mycall_high_query_et_CALL_NO, mycall_high_query_et_CALLED_NO,
            mycall_high_query_et_CALL_TIME_LENGTH_BEGIN, mycall_high_query_et_CALL_TIME_LENGTH_END;

    private Button mycall_high_query_btn_reset, mycall_high_query_btn_confirm;

    private Spinner mycall_high_query_sp_CONNECT_TYPE, mycall_high_query_sp_STATUS,mycall_high_query_sp_CUSTOMER_NAME,
            mycall_high_query_sp_DISPOSAL_AGENT, mycall_high_query_sp_ERROR_MEMO, mycall_high_query_sp_INVESTIGATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mycall_highquery);
        initViews();
    }

    private void initViews() {
        mycall_high_query_et_CALL_NO = (EditText) findViewById(R.id.mycall_high_query_et_CALL_NO);
        mycall_high_query_et_CALLED_NO = (EditText) findViewById(R.id.mycall_high_query_et_CALLED_NO);
        mycall_high_query_et_CALL_TIME_LENGTH_BEGIN = (EditText) findViewById(R.id.mycall_high_query_et_CALL_TIME_LENGTH_BEGIN);
        mycall_high_query_et_CALL_TIME_LENGTH_END = (EditText) findViewById(R.id.mycall_high_query_et_CALL_TIME_LENGTH_END);

        mycall_high_query_btn_reset = (Button) findViewById(R.id.mycall_high_query_btn_reset);
        mycall_high_query_btn_reset.setOnClickListener(this);
        mycall_high_query_btn_confirm = (Button) findViewById(R.id.mycall_high_query_btn_confirm);
        mycall_high_query_btn_confirm.setOnClickListener(this);

        mycall_high_query_sp_CONNECT_TYPE = (Spinner) findViewById(R.id.mycall_high_query_sp_CONNECT_TYPE);
        List<QueryData> connectTypeDatas = new ArrayList<>();
        initConnectTypeDatas(connectTypeDatas);
        SPAdapter connectTypeAdapter = new SPAdapter(MYCallHighQueryActivity.this, connectTypeDatas);
        mycall_high_query_sp_CONNECT_TYPE.setAdapter(connectTypeAdapter);

        mycall_high_query_sp_STATUS = (Spinner) findViewById(R.id.mycall_high_query_sp_STATUS);
        List<QueryData> statusDatas = new ArrayList<>();
        initStatusDatas(statusDatas);
        SPAdapter statusAdapter = new SPAdapter(MYCallHighQueryActivity.this, statusDatas);
        mycall_high_query_sp_STATUS.setAdapter(statusAdapter);

        mycall_high_query_sp_CUSTOMER_NAME = (Spinner) findViewById(R.id.mycall_high_query_sp_CUSTOMER_NAME);
        List<QueryData> customerDatas = new ArrayList<>();
        initCustomerDatas(customerDatas);
        SPAdapter customerAdapter = new SPAdapter(MYCallHighQueryActivity.this, customerDatas);
        mycall_high_query_sp_CUSTOMER_NAME.setAdapter(customerAdapter);

        //隐藏坐席选择
        findViewById(R.id.mycall_ll_agent).setVisibility(View.GONE);
        findViewById(R.id.mycall_view_agent).setVisibility(View.GONE);
//        mycall_high_query_sp_DISPOSAL_AGENT = (Spinner) findViewById(R.id.mycall_high_query_sp_DISPOSAL_AGENT);
//        List<QueryData> agentDatas = new ArrayList<>();
//        QueryData qd_anull = new QueryData();
//        qd_anull.setName("请选择");
//        qd_anull.setValue("");
//        agentDatas.add(qd_anull);
//        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) != null) {
//            HashMap<String, MAAgent> agentMap = (HashMap<String, MAAgent>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent);
//            for(String key : agentMap.keySet()) {
//                QueryData qd = new QueryData();
//                qd.setName(agentMap.get(key).displayName);
//                qd.setValue(key);
//                agentDatas.add(qd);
//            }
//        }
//        SPAdapter agentAdapter = new SPAdapter(MYCallHighQueryActivity.this, agentDatas);
//        mycall_high_query_sp_DISPOSAL_AGENT.setAdapter(agentAdapter);

        mycall_high_query_sp_ERROR_MEMO = (Spinner) findViewById(R.id.mycall_high_query_sp_ERROR_MEMO);
        List<QueryData> queueDatas = new ArrayList<>();
        QueryData qd_qnull = new QueryData();
        qd_qnull.setName("请选择");
        qd_qnull.setValue("");
        queueDatas.add(qd_qnull);
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue) != null) {
            HashMap<String, MAQueue> queueMap = (HashMap<String, MAQueue>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue);
            for(String key : queueMap.keySet()) {
                QueryData qd = new QueryData();
                qd.setName(queueMap.get(key).DisplayName);
                qd.setValue(queueMap.get(key).Exten);
                queueDatas.add(qd);
            }
        }
        SPAdapter queueAdapter = new SPAdapter(MYCallHighQueryActivity.this, queueDatas);
        mycall_high_query_sp_ERROR_MEMO.setAdapter(queueAdapter);

        mycall_high_query_sp_INVESTIGATE = (Spinner) findViewById(R.id.mycall_high_query_sp_INVESTIGATE);
        List<QueryData> investigateDatas = new ArrayList<>();
        QueryData qd_null = new QueryData();
        qd_null.setName("请选择");
        qd_null.setValue("");
        investigateDatas.add(qd_null);
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
            HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
            for(String key : optionMap.keySet()) {
                if("满意度调查选项".equals(key)) {
                    List<Option> investigates = optionMap.get(key).options;
                    for(int i=0; i<investigates.size(); i++) {
                        QueryData qd = new QueryData();
                        qd.setName(investigates.get(i).name);
                        qd.setValue(investigates.get(i).options.get(0).name);
                        investigateDatas.add(qd);
                    }
                }

            }
        }
        SPAdapter investigateAdapter = new SPAdapter(MYCallHighQueryActivity.this, investigateDatas);
        mycall_high_query_sp_INVESTIGATE.setAdapter(investigateAdapter);
    }

    private void initConnectTypeDatas(List<QueryData> connectTypeDatas) {
        QueryData connectTypequeryData_null = new QueryData();
        connectTypequeryData_null.setName("请选择");
        connectTypequeryData_null.setValue("");
        connectTypeDatas.add(connectTypequeryData_null);
        QueryData connectTypequeryData = new QueryData();
        connectTypequeryData.setName("普通来电");
        connectTypequeryData.setValue("normal");
        connectTypeDatas.add(connectTypequeryData);
        QueryData connectTypequeryData1 = new QueryData();
        connectTypequeryData1.setName("外呼去电");
        connectTypequeryData1.setValue("dialout");
        connectTypeDatas.add(connectTypequeryData1);
        QueryData connectTypequeryData2 = new QueryData();
        connectTypequeryData2.setName("来电转接");
        connectTypequeryData2.setValue("transfer");
        connectTypeDatas.add(connectTypequeryData2);
        QueryData connectTypequeryData3 = new QueryData();
        connectTypequeryData3.setName("外呼转接");
        connectTypequeryData3.setValue("dialTransfer");
        connectTypeDatas.add(connectTypequeryData3);
    }
    private void initStatusDatas(List<QueryData> statusDatas) {
        QueryData statusqueryData_null = new QueryData();
        statusqueryData_null.setName("请选择");
        statusqueryData_null.setValue("");
        statusDatas.add(statusqueryData_null);
        QueryData statusqueryData = new QueryData();
        statusqueryData.setName("全部");
        statusqueryData.setValue("");
        statusDatas.add(statusqueryData);
        QueryData statusqueryData1 = new QueryData();
        statusqueryData1.setName("已接听");
        statusqueryData1.setValue("dealing");
        statusDatas.add(statusqueryData1);
        QueryData statusqueryData2 = new QueryData();
        statusqueryData2.setName("振铃未接听");
        statusqueryData2.setValue("notDeal");
        statusDatas.add(statusqueryData2);
        QueryData statusqueryData3 = new QueryData();
        statusqueryData3.setName("排队放弃");
        statusqueryData3.setValue("queueLeak");
        statusDatas.add(statusqueryData3);
        QueryData statusqueryData4 = new QueryData();
        statusqueryData4.setName("已留言");
        statusqueryData4.setValue("voicemail");
        statusDatas.add(statusqueryData4);
        QueryData statusqueryData5 = new QueryData();
        statusqueryData5.setName("IVR");
        statusqueryData5.setValue("leak");
        statusDatas.add(statusqueryData5);
        QueryData statusqueryData6 = new QueryData();
        statusqueryData6.setName("黑名单");
        statusqueryData6.setValue("blackList");
        statusDatas.add(statusqueryData6);
    }

    private void initCustomerDatas(List<QueryData> customerDatas) {
        QueryData customerData_null = new QueryData();
        customerData_null.setName("请选择");
        customerData_null.setValue("");
        customerDatas.add(customerData_null);
        QueryData customerData = new QueryData();
        customerData.setName("已定位");
        customerData.setValue("已定位");
        customerDatas.add(customerData);
        QueryData customerData1 = new QueryData();
        customerData1.setName("未知客户");
        customerData1.setValue("未知客户");
        customerDatas.add(customerData1);
        QueryData customerData2 = new QueryData();
        customerData2.setName("多个匹配客户");
        customerData2.setValue("多个匹配客户");
        customerDatas.add(customerData2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mycall_high_query_btn_reset:
                resetAllView();
                break;
            case R.id.mycall_high_query_btn_confirm:
                submitQuery();
                break;
        }
    }

    private void resetAllView() {
        mycall_high_query_et_CALL_NO.setText("");
        mycall_high_query_et_CALLED_NO.setText("");
        mycall_high_query_et_CALL_TIME_LENGTH_BEGIN.setText("");
        mycall_high_query_et_CALL_TIME_LENGTH_END.setText("");
    }

    private void submitQuery() {
        HashMap<String, String> datas = new HashMap<String, String>();
        String call_no = mycall_high_query_et_CALL_NO.getText().toString().trim();
        if(!"".equals(call_no)) {
            datas.put("CALL_NO", call_no);
        }
        String called_no = mycall_high_query_et_CALLED_NO.getText().toString().trim();
        if(!"".equals(called_no)) {
            datas.put("CALLED_NO", called_no);
        }
        String call_time_begin = mycall_high_query_et_CALL_TIME_LENGTH_BEGIN.getText().toString().trim();
        if(!"".equals(call_time_begin)) {
            datas.put("CALL_TIME_LENGTH_BEGIN", call_time_begin);
        }
        String call_time_end = mycall_high_query_et_CALL_TIME_LENGTH_END.getText().toString().trim();
        if(!"".equals(call_time_end)) {
            datas.put("CALL_TIME_LENGTH_END", call_time_end);
        }


        QueryData connectTypequeryData = (QueryData) mycall_high_query_sp_CONNECT_TYPE.getSelectedItem();
        if(connectTypequeryData != null) {
            if(!"".equals(connectTypequeryData.getValue())) {
                datas.put("CONNECT_TYPE", connectTypequeryData.getValue());
            }
        }

        QueryData statusqueryData = (QueryData) mycall_high_query_sp_STATUS.getSelectedItem();
        if(statusqueryData != null) {
            if(!"".equals(statusqueryData.getValue())) {
                datas.put("STATUS", statusqueryData.getValue());
            }
        }

//        QueryData agentqueryData = (QueryData) mycall_high_query_sp_DISPOSAL_AGENT.getSelectedItem();
//        if(agentqueryData != null) {
//            if(!"".equals(agentqueryData.getValue())) {
//                datas.put("DISPOSAL_AGENT", agentqueryData.getValue());
//            }
//        }

        QueryData queuequeryData = (QueryData) mycall_high_query_sp_ERROR_MEMO.getSelectedItem();
        if(queuequeryData != null) {
            if(!"".equals(queuequeryData.getValue())) {
                datas.put("ERROR_MEMO", queuequeryData.getValue());
            }
        }

        QueryData investigatequeryData = (QueryData) mycall_high_query_sp_INVESTIGATE.getSelectedItem();
        if(investigatequeryData != null) {
            if(!"".equals(investigatequeryData.getValue())) {
                datas.put("INVESTIGATE", investigatequeryData.getValue());
            }
        }

        QueryData customerqueryData = (QueryData) mycall_high_query_sp_CUSTOMER_NAME.getSelectedItem();
        if(customerqueryData != null) {
            if(!"".equals(customerqueryData.getValue())) {
                datas.put("CUSTOMER_NAME", customerqueryData.getValue());
            }
        }

        Intent dataIntent = new Intent();
        dataIntent.putExtra("highQueryData", datas);
        setResult(Activity.RESULT_OK, dataIntent);
        finish();
    }



}
