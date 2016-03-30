package com.moor.im.utils;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;
import com.moor.im.app.CacheKey;
import com.moor.im.app.MobileApplication;
import com.moor.im.db.dao.UserDao;
import com.moor.im.http.MobileHttpManager;
import com.moor.im.model.entity.MAAction;
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MABusinessField;
import com.moor.im.model.entity.MABusinessFlow;
import com.moor.im.model.entity.MABusinessStep;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MAOption;
import com.moor.im.model.entity.MAQueue;
import com.moor.im.model.entity.Option;
import com.moor.im.model.entity.User;
import com.moor.im.model.parser.HttpParser;
import com.moor.im.model.parser.MobileAssitantParser;
import com.moor.im.ui.activity.DiscussionActivity;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by longwei on 2016/2/18.
 */
public class MobileAssitantCache {

    private static  MobileAssitantCache instance;

    private User user;

    private MobileAssitantCache() {
        user = UserDao.getInstance().getUser();
    }

    public static MobileAssitantCache getInstance() {
        if(instance == null) {
            instance = new MobileAssitantCache();
        }
        return instance;
    }

    public MAAgent getAgentById(String id) {
        MAAgent agent;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) != null) {
            HashMap<String, MAAgent> agentMap = (HashMap<String, MAAgent>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent);
            agent = agentMap.get(id);
            return agent;
        }else {
//            MobileHttpManager.getAgentCache(user._id, new GetAgentResponseHandler());
        }
        return null;
    }

    public List<MAAgent> getAgents() {
        List<MAAgent> agents = new ArrayList<>();
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent) != null) {
            HashMap<String, MAAgent> agentMap = (HashMap<String, MAAgent>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAAgent);
            for (String key : agentMap.keySet()) {
                MAAgent agent = agentMap.get(key);
                agents.add(agent);
            }
            return agents;
        }
        return agents;
    }

    public MAQueue getQueueByExten(String id) {
        MAQueue queue;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue) != null) {
            HashMap<String, MAQueue> queueMap = (HashMap<String, MAQueue>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAQueue);
            queue = queueMap.get(id);
            return queue;
        }else {

        }
        return null;
    }

    public MABusinessStep getBusinessStep(String id) {
        MABusinessStep step;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) != null) {
            HashMap<String, MABusinessStep> stepMap = (HashMap<String, MABusinessStep>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep);
            step = stepMap.get(id);
            return step;
        }else {

        }
        return null;
    }

    public MAAction getBusinessStepAction(String stepId, String actionId) {
        MABusinessStep step;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep) != null) {
            HashMap<String, MABusinessStep> stepMap = (HashMap<String, MABusinessStep>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessStep);
            step = stepMap.get(stepId);
            if(step != null) {
                List<MAAction> actions = step.actions;
                if(actions != null) {
                    for(int i=0; i<actions.size(); i++) {
                        if(actions.get(i)._id.equals(actionId)) {
                            return actions.get(i);
                        }
                    }
                }
            }
        }else {

        }
        return null;
    }

    public MABusinessFlow getBusinessFlow(String id) {
        MABusinessFlow flow;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) != null) {
            HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
            flow = flowMap.get(id);
            return flow;
        }else {

        }
        return null;
    }

    public List<MABusinessFlow> getBusinessFlows() {
        List<MABusinessFlow> flows = new ArrayList<>();
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow) != null) {
            HashMap<String, MABusinessFlow> flowMap = (HashMap<String, MABusinessFlow>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessFlow);
            for (String key : flowMap.keySet()) {
                MABusinessFlow flow = flowMap.get(key);
                flows.add(flow);
            }
            return flows;
        }else {

        }
        return null;
    }


    public MABusinessField getBusinessField(String id) {
        MABusinessField field;
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessField) != null) {
            HashMap<String, MABusinessField> fieldMap = (HashMap<String, MABusinessField>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MABusinessField);
            field = fieldMap.get(id);
            return field;
        }else {

        }
        return null;
    }

    public MAOption getMAOption(String dicId) {
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
            HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
            for(String key : optionMap.keySet()) {
                if(optionMap.get(key)._id.equals(dicId)) {
                    return optionMap.get(key);
                }
            }
        }
        return null;
    }

    public String getDicById(String id) {
        if (MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption) != null) {
            HashMap<String, MAOption> optionMap = (HashMap<String, MAOption>) MobileApplication.cacheUtil.getAsObject(CacheKey.CACHE_MAOption);
            for(String key : optionMap.keySet()) {
                if(optionMap.get(key)._id.equals(id)) {
                    return optionMap.get(key).name;
                }
                List<Option> options = optionMap.get(key).options;
                if(options != null && options.size() > 0) {
                    String result = getDic(id, options);
                    if(!"".equals(result)) {
                        return result;
                    }
                }

            }
        }else {

        }
        return "";
    }

    private String getDic(String key, List<Option> options) {

        for(int i=0; i<options.size(); i++) {
            Option opt = options.get(i);
            if(key.equals(opt.key)) {
                return opt.name;
            }
            List<Option> opts = opt.options;
            if(opts != null && opts.size() > 0) {
                String result = getDic(key, opts);
                if(result != null && !"".equals(result)) {
                    return result;
                }
            }

        }
        return "";
    }

    class GetAgentResponseHandler extends TextHttpResponseHandler {
        @Override
        public void onFailure(int statusCode, Header[] headers,
                              String responseString, Throwable throwable) {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers,
                              String responseString) {
            try {
                JSONObject o = new JSONObject(responseString);
                if(o.getBoolean("success")) {
                    List<MAAgent> agents = MobileAssitantParser.getAgents(responseString);
                    HashMap<String, MAAgent> agentDatas = MobileAssitantParser.transformAgentData(agents);
                    MobileApplication.cacheUtil.put(CacheKey.CACHE_MAAgent, agentDatas);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
