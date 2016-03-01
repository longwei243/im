package com.moor.im.model.parser;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusiness;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MACallLogData;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAQueue;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.QueryData;
import com.moor.im.utils.MobileAssitantCache;
import com.moor.im.utils.NullUtil;
import com.moor.im.utils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/17.
 */
public class MobileAssitantParser {

    public static List<MACallLogData> getCdrs(String responseString) {
        List<MACallLogData> maCallLogDatas = new ArrayList<MACallLogData>();
        try {
            JSONObject o = new JSONObject(responseString);
            JSONArray o1 = o.getJSONArray("CallLogs");

            Gson gson = new Gson();
            // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
            List<MACallLog> maCallLogs = gson.fromJson(o1.toString(),
                    new TypeToken<List<MACallLog>>() {
                    }.getType());

            for(int i=0; i<maCallLogs.size(); i++) {
                //转化为列表项直接可以显示的数据
                MACallLog maCallLog = maCallLogs.get(i);
                MACallLogData maCallLogData = new MACallLogData();
                maCallLogData._id = NullUtil.checkNull(maCallLog._id);
                String connectType = NullUtil.checkNull(maCallLog.CONNECT_TYPE);
                String callNo = "";
                String dialType = "";
                if ("dialout".equals(connectType) || "dialTransfer".equals(connectType)) {
                    callNo = NullUtil.checkNull(maCallLog.CALLED_NO);
                    dialType="outbound";
                }else{
                    callNo = NullUtil.checkNull(maCallLog.CALL_NO);
                    dialType="inbound";
                }
                maCallLogData.callNo = callNo;
                maCallLogData.CALLED_NO = maCallLog.CALLED_NO;
                maCallLogData.CALL_NO = maCallLog.CALL_NO;
                maCallLogData.dialType = dialType;
                String city = (NullUtil.checkNull(maCallLog.PROVINCE).equals(NullUtil.checkNull(maCallLog.DISTRICT)))?NullUtil.checkNull(maCallLog.PROVINCE):NullUtil.checkNull(maCallLog.PROVINCE)+"-"+NullUtil.checkNull(maCallLog.DISTRICT);
                city = "["+city+"]";
                maCallLogData.city = city;
                maCallLogData.customName = NullUtil.checkNull(maCallLog.CUSTOMER_NAME);

                maCallLogData.shortTime = TimeUtil.getShortTime(NullUtil.checkNull(maCallLog.OFFERING_TIME));

                maCallLogData.shortCallTimeLength = TimeUtil.getContactsLogTime(NullUtil.checkNull(maCallLog.CALL_TIME_LENGTH)) + "秒";
                MAAgent agent = MobileAssitantCache.getInstance().getAgentById(NullUtil.checkNull(maCallLog.DISPOSAL_AGENT));
                if(agent != null) {
                    maCallLogData.agent = NullUtil.checkNull(agent.displayName)+"["+NullUtil.checkNull(agent.exten)+"]";
                }else {
                    maCallLogData.agent = "";
                }

                MAQueue queue = MobileAssitantCache.getInstance().getQueueByExten(NullUtil.checkNull(maCallLog.ERROR_MEMO));
                if(queue != null) {
                    maCallLogData.queue = NullUtil.checkNull(queue.DisplayName);
                }else {
                    maCallLogData.queue = "";
                }

                String status = "";
                String s = NullUtil.checkNull(maCallLog.STATUS);
                if("leak".equals(s)){
                    status = "IVR";
                }else if("dealing".equals(s)){
                    status = "已接听";
                }else if("notDeal".equals(s)){
                    status = "振铃未接听";
                }else if("queueLeak".equals(s)){
                    status = "排队放弃";
                }else if("voicemail".equals(s)){
                    status = "已留言";
                }else if("blackList".equals(s)){
                    status = "黑名单";
                }
                maCallLogData.status = status;

                String cls = "";
                if("dealing".equals(s) || "voicemail".equals(s)){
                    cls="success";
                }
                maCallLogData.statusClass=cls;


                maCallLogData.PROVINCE=maCallLog.PROVINCE;
                maCallLogData.DISTRICT=maCallLog.DISTRICT;
                maCallLogData.OFFERING_TIME=maCallLog.OFFERING_TIME;
                maCallLogData.BEGIN_TIME=maCallLog.BEGIN_TIME;
                maCallLogData.FILE_SERVER=maCallLog.FILE_SERVER;
                maCallLogData.RECORD_FILE_NAME=maCallLog.RECORD_FILE_NAME;

                if(maCallLog.INVESTIGATE != null) {
                    if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
                        HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
                        for(String key : optionMap.keySet()) {
                            if("满意度调查选项".equals(key)) {
                                List<Option> investigates = optionMap.get(key).options;
                                for(int m=0; m<investigates.size(); m++) {
                                    maCallLogData.INVESTIGATE = investigates.get(m).name;
                                    break;
                                }
                            }

                        }
                    }
                }

                maCallLogDatas.add(maCallLogData);

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return maCallLogDatas;
    }

    /**
     * 解析坐席
     * @param responseString
     * @return
     */
    public static List<MAAgent> getAgents(String responseString) {
        List<MAAgent> agents = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                agents = gson.fromJson(o1.toString(),
                        new TypeToken<List<MAAgent>>() {
                        }.getType());
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return agents;
    }

    public static HashMap<String, MAAgent> transformAgentData(List<MAAgent> agents) {
        HashMap<String, MAAgent> agentDatas = new HashMap<>();
        if(agents != null) {
            for(int i=0; i<agents.size(); i++) {
                agentDatas.put(agents.get(i)._id, agents.get(i));
            }
        }
        return agentDatas;
    }

    /**
     * 解析技能组
     * @param responseString
     * @return
     */
    public static List<MAQueue> getQueues(String responseString) {
        List<MAQueue> queues = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                queues = gson.fromJson(o1.toString(),
                        new TypeToken<List<MAQueue>>() {
                        }.getType());
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return queues;
    }

    public static HashMap<String, MAQueue> transformQueueData(List<MAQueue> queues) {
        HashMap<String, MAQueue> queueDatas = new HashMap<>();
        if(queues != null) {
            for(int i=0; i<queues.size(); i++) {
                queueDatas.put(queues.get(i).Exten, queues.get(i));
            }
        }
        return queueDatas;
    }

    /**
     * 解析option
     * @param responseString
     * @return
     */
    public static List<MAOption> getOptions(String responseString) {
        List<MAOption> options = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                options = gson.fromJson(o1.toString(),
                        new TypeToken<List<MAOption>>() {
                        }.getType());
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return options;
    }

    public static HashMap<String, MAOption> transformOptionData(List<MAOption> options) {
        HashMap<String, MAOption> optionDatas = new HashMap<>();
        if(options != null) {
            for(int i=0; i<options.size(); i++) {
                optionDatas.put(options.get(i).name, options.get(i));
            }
        }
        return optionDatas;
    }

    /**
     * 获取工单列表数据
     * @param responseString
     * @return
     */
    public static List<MABusiness> getBusiness(String responseString) {
        List<MABusiness> businesses = new ArrayList<>();
        List<MABusiness> busDatas = new ArrayList<>();
        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("Succeed")) {
                JSONArray o1 = o.getJSONArray("list");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                businesses = gson.fromJson(o1.toString(),
                        new TypeToken<List<MABusiness>>() {
                        }.getType());

                for (int i=0; i<businesses.size(); i++) {
                    MABusiness b = new MABusiness();
                    b._id = businesses.get(i)._id;
                    MAAgent agent = MobileAssitantCache.getInstance().getAgentById(NullUtil.checkNull(businesses.get(i).createUser));
                    if(agent != null) {
                        b.createUser = agent.displayName;
                    }else {
                        b.createUser = "";
                    }
                    MABusinessStep step = MobileAssitantCache.getInstance().getBusinessStep(NullUtil.checkNull(businesses.get(i).step));
                    if(step != null) {
                        b.step = step.name;
                    }else {
                        b.step = "";
                    }
                    MABusinessFlow flow = MobileAssitantCache.getInstance().getBusinessFlow(NullUtil.checkNull(businesses.get(i).flow));
                    if(flow != null) {
                        b.flow = flow.name;
                    }else {
                        b.flow = "";
                    }

                    b.name = businesses.get(i).name;
                    b.lastUpdateTime = businesses.get(i).lastUpdateTime;
                    if(businesses.get(i).master != null && "".equals(businesses.get(i).master)) {
                        MAAgent master = MobileAssitantCache.getInstance().getAgentById(NullUtil.checkNull(businesses.get(i).master));
                        if(master != null) {
                            b.master = master.displayName;
                        }else {
                            b.master = "";
                        }
                    }
                    busDatas.add(b);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return busDatas;
    }

    /**
     * 获取工单流程
     * @param responseString
     * @return
     */
    public static List<MABusinessFlow> getBusinessFlow(String responseString) {
        List<MABusinessFlow> businessFlows = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                businessFlows = gson.fromJson(o1.toString(),
                        new TypeToken<List<MABusinessFlow>>() {
                        }.getType());


            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return businessFlows;
    }

    public static HashMap<String, MABusinessFlow> transformBusinessFlowData(List<MABusinessFlow> businessFlows) {
        HashMap<String, MABusinessFlow> businessFlowsMap = new HashMap<>();
        if(businessFlows != null) {
            for(int i=0; i<businessFlows.size(); i++) {
                businessFlowsMap.put(businessFlows.get(i)._id, businessFlows.get(i));
            }
        }
        return businessFlowsMap;
    }

    /**
     * 获取工单步骤
     * @param responseString
     * @return
     */
    public static List<MABusinessStep> getBusinessStep(String responseString) {
        List<MABusinessStep> businessSteps = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                businessSteps = gson.fromJson(o1.toString(),
                        new TypeToken<List<MABusinessStep>>() {
                        }.getType());


            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return businessSteps;
    }

    public static HashMap<String, MABusinessStep> transformBusinessStepData(List<MABusinessStep> businessSteps) {
        HashMap<String, MABusinessStep> businessStepMap = new HashMap<>();
        if(businessSteps != null) {
            for(int i=0; i<businessSteps.size(); i++) {
                businessStepMap.put(businessSteps.get(i)._id, businessSteps.get(i));
            }
        }
        return businessStepMap;
    }

    /**
     * 获取工单字段
     * @param responseString
     * @return
     */
    public static List<MABusinessField> getBusinessField(String responseString) {
        List<MABusinessField> businessFields = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(responseString);
            if(o.getBoolean("success")) {
                JSONArray o1 = o.getJSONArray("data");

                Gson gson = new Gson();
                // TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
                businessFields = gson.fromJson(o1.toString(),
                        new TypeToken<List<MABusinessField>>() {
                        }.getType());


            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return businessFields;
    }

    public static HashMap<String, MABusinessField> transformBusinessFieldData(List<MABusinessField> businessFields) {
        HashMap<String, MABusinessField> businessStepMap = new HashMap<>();
        if(businessFields != null) {
            for(int i=0; i<businessFields.size(); i++) {
                businessStepMap.put(businessFields.get(i)._id, businessFields.get(i));
            }
        }
        return businessStepMap;
    }


}
