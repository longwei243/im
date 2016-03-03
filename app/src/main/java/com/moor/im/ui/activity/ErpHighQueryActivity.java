package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.moor.im.R;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.QueryData;
import com.moor.im.ui.adapter.SPAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/3/2.
 */
public class ErpHighQueryActivity extends Activity{


    private EditText erp_high_query_et_query;
    private Spinner erp_high_query_sp_flow, erp_high_query_sp_step, erp_high_query_sp_createTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_erp_highquery);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.title_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        erp_high_query_et_query = (EditText) findViewById(R.id.erp_high_query_et_query);
        erp_high_query_sp_flow = (Spinner) findViewById(R.id.erp_high_query_sp_flow);
        erp_high_query_sp_step = (Spinner) findViewById(R.id.erp_high_query_sp_step);
        erp_high_query_sp_createTime = (Spinner) findViewById(R.id.erp_high_query_sp_createTime);

        List<QueryData> createTimeDatas = new ArrayList<>();
        initcreateTimeDatas(createTimeDatas);
        SPAdapter statusAdapter = new SPAdapter(ErpHighQueryActivity.this, createTimeDatas);
        erp_high_query_sp_createTime.setAdapter(statusAdapter);

        List<QueryData> flowDatas = new ArrayList<>();
        QueryData qd_qnull = new QueryData();
        qd_qnull.setName("请选择");
        qd_qnull.setValue("");
        flowDatas.add(qd_qnull);
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) != null) {
            HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
            for(String key : flowMap.keySet()) {
                QueryData qd = new QueryData();
                qd.setName(flowMap.get(key).name);
                qd.setValue(flowMap.get(key)._id);
                flowDatas.add(qd);
            }
        }
        SPAdapter flowAdapter = new SPAdapter(ErpHighQueryActivity.this, flowDatas);
        erp_high_query_sp_flow.setAdapter(flowAdapter);
        erp_high_query_sp_flow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                QueryData qd = (QueryData) parent.getAdapter().getItem(position);
                if ("".equals(qd.getValue())) {
                    List<QueryData> stepDatas = new ArrayList<>();
                    QueryData qd_stepnull = new QueryData();
                    qd_stepnull.setName("请选择");
                    qd_stepnull.setValue("");
                    stepDatas.add(qd_stepnull);
                    SPAdapter stepAdapter = new SPAdapter(ErpHighQueryActivity.this, stepDatas);
                    erp_high_query_sp_step.setAdapter(stepAdapter);
                } else {
                    List<QueryData> stepDatas = new ArrayList<>();
                    QueryData qd_stepnull = new QueryData();
                    qd_stepnull.setName("请选择");
                    qd_stepnull.setValue("");
                    stepDatas.add(qd_stepnull);

                    if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) != null) {
                        HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
                        List<MABusinessStep> steps = flowMap.get(qd.getValue()).steps;
                        for (int i = 0; i < steps.size(); i++) {
                            QueryData qd_step = new QueryData();
                            qd_step.setName(steps.get(i).name);
                            qd_step.setValue(steps.get(i)._id);
                            stepDatas.add(qd_step);
                        }
                    }
                    SPAdapter stepAdapter = new SPAdapter(ErpHighQueryActivity.this, stepDatas);
                    erp_high_query_sp_step.setAdapter(stepAdapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.erp_high_query_btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllView();
            }
        });
        findViewById(R.id.erp_high_query_btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitQuery();
            }
        });
    }

    private void initcreateTimeDatas(List<QueryData> createTimeDatas) {
        QueryData createTimeData_null = new QueryData();
        createTimeData_null.setName("请选择");
        createTimeData_null.setValue("");
        createTimeDatas.add(createTimeData_null);
        QueryData createTimeData_today = new QueryData();
        createTimeData_today.setName("今天");
        createTimeData_today.setValue("today");
        createTimeDatas.add(createTimeData_today);
        QueryData createTimeData_threeDay = new QueryData();
        createTimeData_threeDay.setName("近三天");
        createTimeData_threeDay.setValue("threeDay");
        createTimeDatas.add(createTimeData_threeDay);
        QueryData createTimeData_week = new QueryData();
        createTimeData_week.setName("近一周");
        createTimeData_week.setValue("week");
        createTimeDatas.add(createTimeData_week);
        QueryData createTimeData_month = new QueryData();
        createTimeData_month.setName("近一月");
        createTimeData_month.setValue("month");
        createTimeDatas.add(createTimeData_month);
    }

    private void resetAllView() {
        erp_high_query_et_query.setText("");

        erp_high_query_sp_flow.setSelection(0);
        erp_high_query_sp_step.setSelection(0);
        erp_high_query_sp_createTime.setSelection(0);
    }

    private void submitQuery() {
        HashMap<String, String> datas = new HashMap<String, String>();
        String name = erp_high_query_et_query.getText().toString().trim();
        if(!"".equals(name)) {
            datas.put("query", name);
        }

        QueryData flowData = (QueryData) erp_high_query_sp_flow.getSelectedItem();
        if(flowData != null) {
            if(!"".equals(flowData.getValue())) {
                datas.put("flow", flowData.getValue());
            }
        }

        QueryData stepData = (QueryData) erp_high_query_sp_step.getSelectedItem();
        if(stepData != null) {
            if(!"".equals(stepData.getValue())) {
                datas.put("step", stepData.getValue());
            }
        }

        QueryData createTimeData = (QueryData) erp_high_query_sp_createTime.getSelectedItem();
        if(createTimeData != null) {
            if(!"".equals(createTimeData.getValue())) {
                datas.put("createTime", createTimeData.getValue());
            }
        }

        Intent dataIntent = new Intent();
        dataIntent.putExtra("highQueryData", datas);
        setResult(Activity.RESULT_OK, dataIntent);
        finish();
    }

}
