package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.FieldData;
import com.moor.im.model.entity.MAAction;
import com.moor.im.model.entity.MAActionFields;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.MAErpDetail;
import com.moor.im.model.entity.MAErpHistory;
import com.moor.im.model.entity.QueryData;
import com.moor.im.model.entity.User;
import com.moor.im.model.entity.UserRole;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.ui.adapter.SPAdapter;
import com.moor.im.ui.dialog.LoadingFragmentDialog;
import com.moor.im.utils.MobileAssitantCache;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by longwei on 2016/3/2.
 */
public class ErpDetailActivity extends Activity{

    private User user = UserDao.getInstance().getUser();

    private TextView erpdetail_tv_customerName, erpdetail_tv_flow, erpdetail_tv_step,
            erpdetail_tv_lastUpdateUser, erpdetail_tv_lastUpdateTime;

    private LinearLayout erpdetail_ll_fields, erpdetail_ll_history;

    private LoadingFragmentDialog loadingFragmentDialog;

    private Spinner erpdetail_sp_action;

    private ScrollView erpdetail_sv;

    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_erpdetail);

        Intent intent = getIntent();
        String busId = intent.getStringExtra("busId");
        String customerName = intent.getStringExtra("customerName");
        type = intent.getStringExtra("type");
        erpdetail_sv = (ScrollView) findViewById(R.id.erpdetail_sv);
        erpdetail_tv_customerName = (TextView) findViewById(R.id.erpdetail_tv_customerName);
        erpdetail_tv_customerName.setText(customerName);

        erpdetail_tv_flow = (TextView) findViewById(R.id.erpdetail_tv_flow);
        erpdetail_tv_step = (TextView) findViewById(R.id.erpdetail_tv_step);
        erpdetail_tv_lastUpdateUser = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateUser);
        erpdetail_tv_lastUpdateTime = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateTime);

        erpdetail_ll_fields = (LinearLayout) findViewById(R.id.erpdetail_ll_fields);
        erpdetail_ll_history = (LinearLayout) findViewById(R.id.erpdetail_ll_history);

        erpdetail_sp_action = (Spinner) findViewById(R.id.erpdetail_sp_action);

        loadingFragmentDialog = new LoadingFragmentDialog();
        loadingFragmentDialog.show(getFragmentManager(), "");

        MobileHttpManager.getBusinessDetailById(user._id, busId, new GetBusinessDetailResponseHandler());
    }

    class GetBusinessDetailResponseHandler extends TextHttpResponseHandler {

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
            loadingFragmentDialog.dismiss();
        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            String succeed = HttpParser.getSucceed(s);
            if ("true".equals(succeed)) {
                BackTask backTask = new BackTask();
                backTask.execute(s);
            }
        }
    }

    class BackTask extends AsyncTask<String, Void, MAErpDetail> {

        @Override
        protected MAErpDetail doInBackground(String[] params) {
            return initDatas(params[0]);
        }

        @Override
        protected void onPostExecute(MAErpDetail detail) {
            super.onPostExecute(detail);
            initDetailViews(detail);

        }
    }

    /**
     * 填充数据视图
     * @param detail
     */
    private void initDetailViews(final MAErpDetail detail) {
        erpdetail_tv_flow.setText(detail.flow);
        erpdetail_tv_step.setText(detail.step);
        erpdetail_tv_lastUpdateUser.setText(detail.lastUpdateUser);
        erpdetail_tv_lastUpdateTime.setText(detail.lastUpdateTime);

        //上面字段信息
        List<FieldData> fdList = detail.fieldDatas;
        LinearLayout parentLinearLayout = new LinearLayout(ErpDetailActivity.this);
        parentLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        for(int i=0; i<fdList.size(); i++) {
            //填充布局
            FieldData fd = fdList.get(i);
            if("file".equals(fd.getType())) {
                //附件
                TextView tv_field_name = new TextView(ErpDetailActivity.this);
                tv_field_name.setText(fd.getName());
                tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                tv_field_name.setPadding(32, 0, 0, 0);
                LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                parentLinearLayout.addView(tv_field_name, mLayoutParams);
                TextView tv_field_value = new TextView(ErpDetailActivity.this);
                tv_field_value.setText(fdList.get(i).getValue());
                tv_field_value.setTextColor(getResources().getColor(R.color.abs__holo_blue_light));
                tv_field_value.setPadding(32, 0, 0, 0);
                parentLinearLayout.addView(tv_field_value, mLayoutParams);
            }else {
                TextView tv_field_name = new TextView(ErpDetailActivity.this);
                tv_field_name.setText(fdList.get(i).getName());
                tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                tv_field_name.setPadding(32, 0, 0, 0);
                LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                parentLinearLayout.addView(tv_field_name, mLayoutParams);
                TextView tv_field_value = new TextView(ErpDetailActivity.this);
                tv_field_value.setText(fdList.get(i).getValue());
                tv_field_value.setTextColor(getResources().getColor(R.color.grey));
                tv_field_value.setPadding(32, 0, 0, 0);
                parentLinearLayout.addView(tv_field_value, mLayoutParams);
            }
        }
        erpdetail_ll_fields.addView(parentLinearLayout);

        //历史
        List<MAErpHistory> historyList = detail.historyList;
        for (int h=0; h<historyList.size(); h++) {
            MAErpHistory historyData = historyList.get(h);
            View infoView = LayoutInflater.from(ErpDetailActivity.this).inflate(R.layout.erp_history_info, null);
            TextView erp_history_tv_name = (TextView) infoView.findViewById(R.id.erp_history_tv_name);
            erp_history_tv_name.setText(historyData.name);
            TextView erp_history_tv_time = (TextView) infoView.findViewById(R.id.erp_history_tv_time);
            erp_history_tv_time.setText(historyData.time);
            TextView erp_history_tv_info = (TextView) infoView.findViewById(R.id.erp_history_tv_info);
            erp_history_tv_info.setText(historyData.info);
            erpdetail_ll_history.addView(infoView);

            LinearLayout history_field_ll = new LinearLayout(ErpDetailActivity.this);
            history_field_ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            history_field_ll.setOrientation(LinearLayout.VERTICAL);
            history_field_ll.setPadding(80, 0, 0, 0);
            List<FieldData> historyFieldDatas = historyData.historyData;
            for(int f=0; f<historyFieldDatas.size(); f++) {
                FieldData fd = historyFieldDatas.get(f);
                if("file".equals(fd.getType())) {
                    //附件
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fd.getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    tv_field_name.setPadding(32, 0, 0, 0);
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    history_field_ll.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fd.getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.abs__holo_blue_light));
                    tv_field_value.setPadding(32, 0, 0, 0);
                    history_field_ll.addView(tv_field_value, mLayoutParams);
                }else {
                    TextView tv_field_name = new TextView(ErpDetailActivity.this);
                    tv_field_name.setText(fd.getName());
                    tv_field_name.setTextColor(getResources().getColor(R.color.all_black));
                    tv_field_name.setPadding(32, 0, 0, 0);
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    history_field_ll.addView(tv_field_name, mLayoutParams);
                    TextView tv_field_value = new TextView(ErpDetailActivity.this);
                    tv_field_value.setText(fd.getValue());
                    tv_field_value.setTextColor(getResources().getColor(R.color.grey));
                    tv_field_value.setPadding(32, 0, 0, 0);
                    history_field_ll.addView(tv_field_value, mLayoutParams);
                }
            }
            erpdetail_ll_history.addView(history_field_ll);

            if(h != historyList.size()-1) {
                View v = new View(ErpDetailActivity.this);
                LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2);
                v.setBackgroundColor(getResources().getColor(R.color.grey));
                v.setPadding(0,2,0,2);
                erpdetail_ll_history.addView(v, vlp);
            }
        }

        //动作,注意角色权限控制
        List<MAAction> actionList = detail.actions;
        List<QueryData> actionDatas = new ArrayList<>();
        QueryData qd = new QueryData();
        qd.setName("开始操作");
        qd.setValue("");
        actionDatas.add(qd);
        Collection<UserRole> userRoles = user.userRoles;
        List<String> roles = new ArrayList<>();
        if(userRoles != null && userRoles.size() > 0) {
            for (UserRole ur : userRoles) {
                roles.add(ur.role);
            }
        }

        for (int c=0; c<actionList.size(); c++) {
            MAAction action = actionList.get(c);

            if(arrayContainsStr(roles, action.actionRole)) {
                QueryData qd1 = new QueryData();
                qd1.setName(action.name);
                qd1.setValue(action._id);
                actionDatas.add(qd1);
            }
        }
        SPAdapter actionAdapter = new SPAdapter(ErpDetailActivity.this, actionDatas);
        erpdetail_sp_action.setAdapter(actionAdapter);
        erpdetail_sp_action.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                QueryData queryData = (QueryData) parent.getAdapter().getItem(position);

                String actionId = queryData.getValue();
                MAErpDetail business = detail;
                String stepId = business.stepId;
                MAAction action = MobileAssitantCache.getInstance().getBusinessStepAction(stepId, actionId);

                if (action != null) {
                    String nextStepId = action.jumpTo;
                    MABusinessStep nextStep = MobileAssitantCache.getInstance().getBusinessStep(nextStepId);
                    if ("sys".equals(nextStep.type)) {
                        //下一步是系统步骤且没有配置界面，直接执行
                        MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(stepId);
                        MAAction act = getFlowStepActionById(step.actions, actionId);
                        if (act != null) {
                            List<MAActionFields> actionFields = act.actionFields;
                            if (actionFields.size() == 0) {
                                //执行操作
                                HashMap<String, String> datas = new HashMap<String, String>();
                                datas.put("_id", business._id);
                                datas.put("actionId", act._id);
                                datas.put("master", "sys");

//                                MobileHttpManager.excuteBusinessStepAction(user._id, datas, new ExcuteBusinessStepActionHandler());
                            } else {
                                Intent intent = new Intent(ErpDetailActivity.this, ErpActionProcessActivity.class);
                                intent.putExtra("actionId", actionId);
                                intent.putExtra("business", business);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Intent intent = new Intent(ErpDetailActivity.this, ErpActionProcessActivity.class);
                        intent.putExtra("actionId", actionId);
                        intent.putExtra("business", business);
                        startActivity(intent);
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //判断是否需要显示动作操作
        if("roalundeal".equals(type)) {
            //隐藏
            erpdetail_sp_action.setVisibility(View.GONE);
        }else if("userundeal".equals(type)) {
            erpdetail_sp_action.setVisibility(View.VISIBLE);
            //已完成的也隐藏
            if("complete".equals(detail.status) || "cancel".equals(detail.status)) {
                erpdetail_sp_action.setVisibility(View.GONE);
            }
        }
        erpdetail_sv.setVisibility(View.VISIBLE);
        loadingFragmentDialog.dismiss();
    }

    private MAAction getFlowStepActionById(List<MAAction> actions, String actionId) {
        if(actions != null && actionId != null) {
            for(int i=0; i<actions.size(); i++) {
                MAAction a = actions.get(i);
                if(actionId.equals(a._id)) {
                    return a;
                }
            }
        }
        return null;
    }

    private boolean arrayContainsStr(List<String> arr, String str) {

        if(arr != null && arr.size() > 0 && str != null) {
            for (int i=0; i<arr.size(); i++) {
                if(arr.get(i).equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    private MAErpDetail initDatas(String data) {
        MAErpDetail detail = new MAErpDetail();
        try {
            JSONObject jo = new JSONObject(data);
            JSONObject jsonObject = jo.getJSONObject("data");
            String _id = jsonObject.getString("_id");
            String flowId = jsonObject.getString("flow");
            String stepId = jsonObject.getString("step");
            String lastUpdateUserId = jsonObject.getString("lastUpdateUser");
            String lastUpdateTime = jsonObject.getString("lastUpdateTime");
            String status = jsonObject.getString("status");
            MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(flowId);
            if(flow != null) {
                detail.flow = flow.name;
            }
            MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(stepId);
            if(step != null) {
                detail.step = step.name;
            }
            detail.flowId = flowId;
            detail.stepId = stepId;
            detail.status = status;

            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(lastUpdateUserId);
            if(agent != null) {
                detail.lastUpdateUser = agent.displayName;
            }
            detail._id = _id;
            detail.lastUpdateTime = lastUpdateTime;

            //填充字段
            List<FieldData> fdList = initFieldData(jsonObject);
            detail.fieldDatas = fdList;

            List<MAAction> actionsList = step.actions;
            detail.actions = actionsList;

            //填充历史
            List<MAErpHistory> historyList = new ArrayList<>();
            JSONArray historyArray = jsonObject.getJSONArray("history");
            for (int j=0; j<historyArray.length(); j++) {
                MAErpHistory history = new MAErpHistory();
                JSONObject historyItem = historyArray.getJSONObject(j);
                String action = historyItem.getString("action");
                if(action != null && "complete".equals(action)) {
                    continue;
                }
                String historyMaster = historyItem.getString("master");
                if(action != null && "backIn".equals(action)){
                    historyMaster = historyItem.getString("excuteUser");
                }
                String time = historyItem.getString("time");
                String username = MobileAssitantCache.getInstance().getAgentById(historyMaster).displayName;

                history.name = username;
                history.time = time;
                //历史信息
                String infoResult = initHistoryInfo(action, historyItem);
                System.out.println("history info is:"+infoResult);
                history.info = infoResult;
                //历史字段
                JSONObject excuteData = historyItem.getJSONObject("excuteData");
                List<FieldData> historyDataList = initHistoryFieldData(excuteData, action);
                history.historyData = historyDataList;
                historyList.add(history);
                for(int i=0; i<historyDataList.size(); i++) {
                    System.out.println("history data type is:"+historyDataList.get(i).getType()+",history data name is:"+historyDataList.get(i).getName()+",history data value is:"+historyDataList.get(i).getValue());
                }
            }
            detail.historyList = historyList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return detail;
    }

    /**
     * 显示自定义字段
     * @return
     */
    private List<FieldData> initFieldData(JSONObject jsonObject) {
        List<FieldData> fdList = new ArrayList<>();
        try{
            Iterator<String> iterator = jsonObject.keys();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String fieldKey = key;
                if("_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                    fieldKey = key.substring(0, key.length() - 2);
                }
                MABusinessField field = MobileAssitantCache.getInstance().getBusinessField(fieldKey);
                if(field != null) {
                    if ("dropdown".equals(field.type)) {
                        //后面_1,_2的
                        String value = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }else {
                            String defaultValue = jsonObject.getString(key+"_default");
                            if(defaultValue != null && !"".equals(defaultValue)) {
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(defaultValue);
                                fdList.add(fd);
                            }
                        }

                    }else if ("checkbox".equals(field.type)) {
                        //数组
                        String value = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }else {
                            JSONArray defaultValue = jsonObject.getJSONArray(key + "_default");
                            if(defaultValue != null && defaultValue.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i=0; i<defaultValue.length(); i++) {
                                    sb.append(defaultValue.getString(i)+" ");
                                }
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(sb.toString());
                                fdList.add(fd);
                            }
                        }
                    }else if ("radio".equals(field.type)) {
                        //只有一个值
                        String value = MobileAssitantCache.getInstance().getDicById(jsonObject.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }
                    }else if ("file".equals(field.type)) {
                        //数组
                        JSONArray attachArray = jsonObject.getJSONArray(key);
                        for(int i=0; i< attachArray.length(); i++) {
                            JSONObject attach = attachArray.getJSONObject(i);
                            FieldData fd = new FieldData();
                            fd.setType("file");
                            fd.setName(field.name);
                            fd.setValue(attach.getString("name"));
                            fd.setId(attach.getString("id"));
                            fdList.add(fd);
                        }
                    } else {
                        String type = "normal";
                        String name = field.name;
                        String value = jsonObject.getString(key);
                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            fdList.add(fd);
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fdList;
    }


    /**
     * 历史操作信息
     * @param action
     * @param historyItem
     * @return
     */
    private String initHistoryInfo(String action, JSONObject historyItem) {
        String infoResult = "";
        try{
            if (action != null && "create".equals(action)) {
                infoResult = "创建 工单！";
            }else if(action != null && "transformIn".equals(action)) {
                String stepStr = MobileAssitantCache.getInstance().getBusinessStep(historyItem.getString("step")).name;
                String actionName = MobileAssitantCache.getInstance().getBusinessStepAction(historyItem.getString("fromStep"), historyItem.getString("excuteAction")).name;
                infoResult = "执行动作【" + actionName + "】状态变更为【" + stepStr + "】";
            }else if(action != null && "backIn".equals(action)) {
                String stepStr = MobileAssitantCache.getInstance().getBusinessStep(historyItem.getString("step")).name;
                infoResult = "执行动作【退回工单】状态变更为【" + stepStr + "】";
            }else if(action != null && "recreate".equals(action)) {
                infoResult = "重新提交 工单！";
            }else if(action != null && "comment".equals(action)) {
                infoResult = "添加 新的备注！";
            }else if(action != null && "assign".equals(action)) {

                String mastername = MobileAssitantCache.getInstance().getAgentById(historyItem.getJSONObject("excuteData").getString("master")).displayName;
                if (mastername == null || "".equals(mastername)) {
                    mastername = "自动分配";
                }
                infoResult = "变更 工单处理人为【" + mastername + "】";
            }else {
                infoResult = "未知的动作！";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infoResult;
    }

    /**
     * 历史字段
     * @param excuteData
     * @param action
     * @return
     */
    private List<FieldData> initHistoryFieldData(JSONObject excuteData, String action) {
        List<FieldData> historyDataList = new ArrayList<>();
        try{
            Iterator<String> historyDataIterator = excuteData.keys();
            while(historyDataIterator.hasNext()) {
                String key = historyDataIterator.next();
                String fieldKey = key;
                if("_".equals(key.substring(key.length() - 2, key.length() - 1))) {
                    fieldKey = key.substring(0, key.length() - 2);
                }
                MABusinessField field = MobileAssitantCache.getInstance().getBusinessField(fieldKey);
                if(field != null) {
                    if ("dropdown".equals(field.type)) {
                        //后面_1,_2的
                        String value = MobileAssitantCache.getInstance().getDicById(excuteData.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }else {
                            String defaultValue = excuteData.getString(key+"_default");
                            if(defaultValue != null && !"".equals(defaultValue)) {
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(defaultValue);
                                historyDataList.add(fd);
                            }
                        }

                    }else if ("checkbox".equals(field.type)) {
                        //数组
                        String value = MobileAssitantCache.getInstance().getDicById(excuteData.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }else {
                            JSONArray defaultValue = excuteData.getJSONArray(key + "_default");
                            if(defaultValue != null && defaultValue.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (int i=0; i<defaultValue.length(); i++) {
                                    sb.append(defaultValue.getString(i)+" ");
                                }
                                FieldData fd = new FieldData();
                                fd.setType(type);
                                fd.setName(name);
                                fd.setValue(sb.toString());
                                historyDataList.add(fd);
                            }
                        }
                    }else if ("radio".equals(field.type)) {
                        //只有一个值
                        String value = MobileAssitantCache.getInstance().getDicById(excuteData.getString(key));
                        String type = "normal";
                        String name = field.name;

                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }
                    }else if ("file".equals(field.type)) {
                        JSONArray attachArray = excuteData.getJSONArray(key);
                        for(int i=0; i< attachArray.length(); i++) {
                            JSONObject attach = attachArray.getJSONObject(i);
                            FieldData fd = new FieldData();
                            fd.setType("file");
                            fd.setName(field.name);
                            fd.setValue(attach.getString("name"));
                            fd.setId(attach.getString("id"));
                            historyDataList.add(fd);
                        }
                    } else {
                        String type = "normal";
                        String name = field.name;
                        String value = excuteData.getString(key);
                        if(value != null && !"".equals(value)) {
                            FieldData fd = new FieldData();
                            fd.setType(type);
                            fd.setName(name);
                            fd.setValue(value);
                            historyDataList.add(fd);
                        }
                    }

                }else if("number".equals(fieldKey)){
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("工单编号");
                    fd.setValue(excuteData.getString(key));
                    historyDataList.add(fd);
                }

                if (action.equals("backIn")) {
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("退回原因");
                    fd.setValue(excuteData.getString("backInfo"));
                    historyDataList.add(fd);
                } else if (action.equals("comment")) {
                    FieldData fd = new FieldData();
                    fd.setType("normal");
                    fd.setName("备注内容");
                    fd.setValue(excuteData.getString("backInfo"));
                    historyDataList.add(fd);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return historyDataList;
    }
}
