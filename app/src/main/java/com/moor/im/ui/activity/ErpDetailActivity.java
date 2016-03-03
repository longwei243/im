package com.moor.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.R;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.FieldData;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.utils.MobileAssitantCache;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by longwei on 2016/3/2.
 */
public class ErpDetailActivity extends Activity{

    private User user = UserDao.getInstance().getUser();

    private TextView erpdetail_tv_customerName, erpdetail_tv_flow, erpdetail_tv_step,
            erpdetail_tv_lastUpdateUser, erpdetail_tv_lastUpdateTime;

    private LinearLayout erpdetail_ll_fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erpdetail);

        Intent intent = getIntent();
        String busId = intent.getStringExtra("busId");
        String customerName = intent.getStringExtra("customerName");
        erpdetail_tv_customerName = (TextView) findViewById(R.id.erpdetail_tv_customerName);
        erpdetail_tv_customerName.setText(customerName);

        erpdetail_tv_flow = (TextView) findViewById(R.id.erpdetail_tv_flow);
        erpdetail_tv_step = (TextView) findViewById(R.id.erpdetail_tv_step);
        erpdetail_tv_lastUpdateUser = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateUser);
        erpdetail_tv_lastUpdateTime = (TextView) findViewById(R.id.erpdetail_tv_lastUpdateTime);

        erpdetail_ll_fields = (LinearLayout) findViewById(R.id.erpdetail_ll_fields);

        MobileHttpManager.getBusinessDetailById(user._id, busId, new GetBusinessDetailResponseHandler());
    }

    class GetBusinessDetailResponseHandler extends TextHttpResponseHandler {

        @Override
        public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

        }

        @Override
        public void onSuccess(int i, Header[] headers, String s) {
            String succeed = HttpParser.getSucceed(s);
            if ("true".equals(succeed)) {
                initDatas(s);
            }
        }
    }

    private void initDatas(String data) {
        try {
            JSONObject jo = new JSONObject(data);
            JSONObject jsonObject = jo.getJSONObject("data");
            String flowId = jsonObject.getString("flow");
            String stepId = jsonObject.getString("step");
            String lastUpdateUserId = jsonObject.getString("lastUpdateUser");
            String lastUpdateTime = jsonObject.getString("lastUpdateTime");
            MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(flowId);
            if(flow != null) {
                erpdetail_tv_flow.setText(flow.name);
            }
            MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(stepId);
            if(step != null) {
                erpdetail_tv_step.setText(step.name);
            }
            MAAgent agent = MobileAssitantCache.getInstance().getAgentById(lastUpdateUserId);
            if(agent != null) {
                erpdetail_tv_lastUpdateUser.setText(agent.displayName);
            }
            erpdetail_tv_lastUpdateTime.setText(lastUpdateTime);

            //填充字段界面
            List<FieldData> fdList = new ArrayList<>();
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
            for(int i=0; i<fdList.size(); i++) {
                System.out.println("type is:"+fdList.get(i).getType()+",name is:"+fdList.get(i).getName()+",value is:"+fdList.get(i).getValue());
            }

            //填充历史界面
            JSONArray historyArray = jsonObject.getJSONArray("history");
            for (int j=0; j<historyArray.length(); j++) {
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

                String infoResult = "";
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

                System.out.println("history info is:"+infoResult);

                JSONObject excuteData = historyItem.getJSONObject("excuteData");

                List<FieldData> historyDataList = new ArrayList<>();
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
                for(int i=0; i<historyDataList.size(); i++) {
                    System.out.println("history data type is:"+historyDataList.get(i).getType()+",history data name is:"+historyDataList.get(i).getName()+",history data value is:"+historyDataList.get(i).getValue());
                }


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
