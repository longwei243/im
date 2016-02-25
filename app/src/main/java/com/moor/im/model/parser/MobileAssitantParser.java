package com.moor.im.model.parser;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.model.entity.MAAgent;
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
}
