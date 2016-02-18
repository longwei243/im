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
import com.moor.im.model.entity.MAAgent;
import com.moor.im.model.entity.MACallLog;
import com.moor.im.model.entity.MAQueue;
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
            MobileHttpManager.getAgentCache(user._id, new GetAgentResponseHandler());
        }
        return null;
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
